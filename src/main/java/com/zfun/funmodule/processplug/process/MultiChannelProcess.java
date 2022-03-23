package com.zfun.funmodule.processplug.process;

import com.android.annotations.NonNull;
import com.android.apksig.ApkVerifier;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension;
import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.processplug.extension.ChannelExtension;
import com.zfun.funmodule.processplug.extension.MultiChannelEx;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.androidZipSinger.V1ChannelUtil;
import com.zfun.funmodule.util.androidZipSinger.read.ChannelInfo;
import com.zfun.funmodule.util.androidZipSinger.read.ChannelReader;
import com.zfun.funmodule.util.androidZipSinger.write.ChannelWriter;
import org.apache.commons.io.FilenameUtils;
import org.gradle.BuildResult;
import org.gradle.api.*;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 多渠道打包。渠道信息不能大于 32767 个字节<br/>
 * <p>
 * <p>
 * Created by zfun on 2021/12/10 4:30 PM
 */
public class MultiChannelProcess implements IProcess {

    @Override
    public void beforeEvaluate(Project project) {

    }

    @Override
    public void afterEvaluate(Project project) {

    }

    @Override
    public void projectsEvaluated(Project project) {
        AppPlugin androidAppPlug = project.getPlugins().findPlugin(AppPlugin.class);
        if (null == androidAppPlug) {//只有app插件才起作用
            return;
        }
        LogMe.D("MultiChannelProcess : " + project.getName());
        BaseAppModuleExtension androidEx = (BaseAppModuleExtension) project.getExtensions().findByName("android");
        if (null == androidEx) {
            return;
        }
        final DomainObjectSet<ApplicationVariant> applicationVariants = androidEx.getApplicationVariants();
        applicationVariants.all(new Action<ApplicationVariant>() {
            @Override
            public void execute(@NonNull ApplicationVariant applicationVariant) {
                final Object ex = project.getRootProject().getExtensions().findByName(Constants.sMultiChannelExName);
                if (!(ex instanceof MultiChannelEx)) {
                    return;
                }
                final MultiChannelEx multiChannelEx = (MultiChannelEx) ex;
                if (multiChannelEx.isEmpty()) {
                    return;
                }
                final NamedDomainObjectContainer<ChannelExtension> channelEx = multiChannelEx.getChannelConfig();
                channelEx.forEach(new Consumer<ChannelExtension>() {
                    @Override
                    public void accept(ChannelExtension channelExtension) {
                        //applicationVariant.getName()是组合后的名称，例如：FullRelease；
                        //applicationVariant.getFlavorName() 仅仅是 productFlavors 的名称；
                        if (applicationVariant.getFlavorName().equals(channelExtension.name)) {
                            LogMe.D("ApplicationVariant == 命中ChannelConfig-Name: " + applicationVariant.getName());
                            //给此变种添加多渠道打包任务
                            starCreateMultiChannelApk(project, applicationVariant, multiChannelEx, channelExtension);
                        }
                    }
                });
            }
        });
        /*//错误调用，因为此时还没有值，需要监听 applicationVariants 的回调来获取值。
        LogMe.D("ApplicationVariants : " + applicationVariants);
        for (ApplicationVariant applicationVariant : applicationVariants) {
            LogMe.D("ApplicationVariant : " + applicationVariant.getName());
        }*/
    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {

    }

    private void starCreateMultiChannelApk(@NonNull final Project project,
                                           @NonNull final ApplicationVariant applicationVariant,
                                           @NonNull final MultiChannelEx multiChannelEx,
                                           @NonNull final ChannelExtension channelEx) {
        applicationVariant.getAssemble().doLast(new Action<Task>() {//打包后进行多渠道配置
            @Override
            public void execute(@NonNull Task task) {
                LogMe.D("startMultiCreateApk getAssemble.doLast = " + task.getName());
                try {
                    final String prefix = multiChannelEx.prefix;
                    final String subfix = multiChannelEx.subfix;
                    final String[] channels = channelEx.childFlavors.toArray(new String[0]);

                    final File oriOutputFile = applicationVariant.getOutputs().stream().findFirst().get().getOutputFile();
                    final String apkName = FilenameUtils.removeExtension(oriOutputFile.getName());
                    final String apkPath = FilenameUtils.getFullPath(oriOutputFile.getAbsolutePath());

                    final boolean isV2Enable = applicationVariant.getSigningConfig().isV2SigningEnabled();
                    final boolean isV1Enable = applicationVariant.getSigningConfig().isV1SigningEnabled();
                    for (String aChannel : channels) {
                        LogMe.D("startMultiCreateApk work = " + aChannel);
                        final String finalOutFileFullPath = apkPath + prefix + aChannel + subfix + ".apk";
                        final File desOutFile = new File(finalOutFileFullPath);
                        if (desOutFile.exists()) {
                            desOutFile.delete();
                        }
                        FileUtil.copy(oriOutputFile, desOutFile);
                        if(isV2Enable){
                            boolean check = channelEx.checkChannel;
                            boolean useLowMemory = channelEx.lowMemory;
                            Map<String,String> extraInfo = channelEx.extraInfo;
                            optUseV2(desOutFile,aChannel,check,useLowMemory,extraInfo);
                        } else {
                            optUseV1(desOutFile,aChannel,"");
                        }
                        LogMe.D("******************************************************");
                    }
                } catch (Exception e) {
                    LogMe.D("startMultiCreateApk = Exception" + e);
                }
            }
        });
    }

    //进行zip文件的注释修改
    private void optUseV1(File apkPath, String channelStr, String passWord) throws Exception{
        V1ChannelUtil.writeCommit(apkPath, channelStr, passWord);
        String testReadChannel = V1ChannelUtil.getChannelId(apkPath.getAbsolutePath(), "", "unKnown");
        LogMe.D("startMultiCreateApk work = optUseV1 = 读取写入的渠道信息 = " + testReadChannel);
        if (!channelStr.equals(testReadChannel)) {
            throw new RuntimeException("V1多渠道打包失败 = 写入的的渠道为 :" + channelStr + "\r\n" + "读取到的渠道为：" + testReadChannel);
        }
    }
    //V2签名时打出的zip包根V1签名打出的zip包结构不一样，但是仍然支持安装到只支持V1签名的系统上，V2写入到zip包中的签名信息仍然有效，并可以被读取。
    //V2兼容V1签名，如果V2签名的apk安装到 7.0 以下版本的手机上时，V2签名块写入的信息仍然在，所以仍然可以读取到此处写入大zip文件中的信息。
    private void optUseV2(File apkPath, String channelStr, boolean check, boolean useLowMemory, Map<String,String> extraInfo) throws Exception{
        boolean isOk = checkV2Signature(apkPath);
        if (isOk) {
            LogMe.D("startMultiCreateApk work = optUseV2 = " + channelStr);
            ChannelWriter.put(apkPath, channelStr, extraInfo,useLowMemory);
            //校验下写入的是否与读取到的相同
            if(check){
                ChannelInfo channelInfo = ChannelReader.get(apkPath);
                String testReadChannel = null != channelInfo?channelInfo.getChannel():"";
                LogMe.D("startMultiCreateApk work = optUseV2 = 读取写入的渠道 = " + testReadChannel);
                if(!testReadChannel.equals(channelStr)){
                    throw new RuntimeException("V2多渠道打包失败 = 写入的的渠道为 :" + channelStr + "\r\n" + "读取到的渠道为：" + testReadChannel);
                }
            }
        } else {
            throw new RuntimeException("V2多渠道打包失败 = apk :" + apkPath + "  未找到V2签名块。");
        }
    }


    private boolean checkV2Signature(File apkFile) {
        try {
            ApkVerifier.Builder builder = new ApkVerifier.Builder(apkFile);
            ApkVerifier apkVerifier = builder.build();
            ApkVerifier.Result result = apkVerifier.verify();
            if (!result.isVerified() || !result.isVerifiedUsingV2Scheme()) {
                throw new GradleException("${apkFile} has no v2 signature in Apk Signing Block!");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
