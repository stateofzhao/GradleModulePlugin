package com.zfun.funmodule.processplug.process;

import com.android.apksig.ApkVerifier;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension;
import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.processplug.extension.ChannelExtension;
import com.zfun.funmodule.processplug.extension.MultiChannelEx;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.FilenameUtils;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.androidZipSinger.V1ChannelUtil;
import com.zfun.funmodule.util.androidZipSinger.ChannelInfo;
import com.zfun.funmodule.util.androidZipSinger.read.ChannelReader;
import com.zfun.funmodule.util.androidZipSinger.write.ChannelWriter;
import org.gradle.BuildResult;
import org.gradle.api.*;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 多渠道打包。渠道信息不能大于 32767 个字节。<p/>
 * *****************************************<br/>
 * 【AGP签名选择】<br/>
 * 如果minSDK版本低于 android7.0 SDK版本，AGP默认同时进行V1和V2签名，以保证在android7.0以下版本设备上可以正常安装。<p/>
 * 【V1和V2签名验证】<br/>
 * V2签名兼容V1签名（不会破坏V1签名），在7.0以下设备上可以正常读取V1签名信息；在>=7.0的设备上，会首先查找apk是否有V2签名块，如果找到了就开始进行V2签名验证，如果未找到就回退到V1签名认证。<p/>
 * 【多渠道信息写入】<br/>
 * 1，开启了V2签名：将签名信息写入到V2签名块中预留的区域。即使将apk安装到7.0以下设备上，也能够顺利读取到V2块中写入的信息，因为V2签名是兼容V1签名的，所以V2签名即使更改了apk的文件信息也不会破坏apk包和V1签名信息。<br/>
 * 2，未开启V2签名：由于apk中没有V2块，所以不能写入到V2块中的预留区域，采用美团方案，将签名信息写入到zip包的注释信息中。<br/>
 * 疑惑：<br/>
 * 一，zip文件是可以随便插入块而不破坏其结构的吗？在V2出来前，其它的多渠道打包方案大概思路有两种方案：<br/>
 * 1，解包apk后写入信息，然后再重新打包。<br/>
 * 2，向apk的注释区域写入信息。<br/>
 * 既然google可以在不破坏apk（zip）的前提下向zip文件中插入"自定义块"，那么其他人就不可以吗？为什么就没有向zip包中插入自定义块的方案呢？
 * 二，是不是在V2签名时仍然采用修改zip的注释块的方式写入渠道信息？
 * 我们知道一旦V2签名后就不能更改apk文件的任何信息了，那么我们是否可以在v2签名前来修改apk的注释信息来写入渠道信息，然后再进行v2签名，这样是不是也可以？待验证。
 * *****************************************<br/>
 * <p/>
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
            public void execute(ApplicationVariant applicationVariant) {
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
                        //applicationVariant.getFlavorName() 仅仅是 productFlavors 的名称，例如：Full；
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

    private void starCreateMultiChannelApk(final Project project,
                                           final ApplicationVariant applicationVariant,
                                           final MultiChannelEx multiChannelEx,
                                           final ChannelExtension channelEx) {
        applicationVariant.getAssemble().doLast(new Action<Task>() {//打包后进行多渠道配置
            @Override
            public void execute(Task task) {
                LogMe.D("************************多渠道打包Process-Start******************************");
                LogMe.D("startMultiCreateApk getAssemble.doLast = " + task.getName());
                try {
                    final String prefix = multiChannelEx.prefix;
                    final String subfix = multiChannelEx.subfix;
                    final String[] channels = channelEx.childFlavors.toArray(new String[0]);

                    final File oriOutputFile = applicationVariant.getOutputs().stream().findFirst().get().getOutputFile();
                    final String apkName = FilenameUtils.removeExtension(oriOutputFile.getName());
                    final String apkPath = FilenameUtils.getFullPath(oriOutputFile.getAbsolutePath());

                    /*final boolean isV2Enable = applicationVariant.getSigningConfig().isV2SigningEnabled();
                    final boolean isV1Enable = applicationVariant.getSigningConfig().isV1SigningEnabled();*/

                    final ApkVerifier.Result signerResult = signerVerify(oriOutputFile);
                    if(null == signerResult){
                        LogMe.D("startMultiCreateApk fail = 原始apk包获取签名结果失败："+oriOutputFile.getAbsolutePath());
                        return;
                    }

                    int sucCount = 0;
                    for (String aChannel : channels) {
                        LogMe.D("startMultiCreateApk work = " + aChannel);
                        final String finalOutFileFullPath = apkPath + prefix + aChannel + subfix + ".apk";
                        final File desOutFile = new File(finalOutFileFullPath);
                        if (desOutFile.exists()) {
                            desOutFile.delete();
                        }
                        boolean isOk = false;
                        FileUtil.copy(oriOutputFile, desOutFile);
                        final Map<String, String> extraInfo = channelEx.extraInfo;
                        if (signerResult.isVerifiedUsingV2Scheme()) {
                            LogMe.D("startMultiCreateApk signerResult = isVerifiedUsingV2Scheme");
                            boolean check = channelEx.checkChannel;
                            boolean useLowMemory = channelEx.lowMemory;
                            try {
                                isOk = optUseV2(desOutFile, aChannel, check, useLowMemory, extraInfo);
                            } catch (Exception ignore) {
                            }
                        } else if (signerResult.isVerifiedUsingV1Scheme()) {
                            LogMe.D("startMultiCreateApk signerResult = isVerifiedUsingV1Scheme");
                            isOk = optUseV1(desOutFile, aChannel, extraInfo, "");
                        }

                        LogMe.D("BuildType is Debug == " + applicationVariant.getBuildType().isDebuggable());
                        if (applicationVariant.getBuildType().isDebuggable()) {
                            if (isOk) {//打的debug包，需要自动刷到手机上
                                //debug模式下，要将写入渠道信息的包重命名为gradle打出来的包，防止无法自动安装到手机上
                                oriOutputFile.delete();
                                desOutFile.renameTo(oriOutputFile);
                                isOk = false;//不要删除原始路径上的apk包，因为写入渠道的包重命名为原始路径apk包了
                            }
                        }
                        if (isOk) {
                            sucCount += 1;
                        } else {
                            try {
                                desOutFile.delete();
                            } catch (Exception ignore) {
                            }
                        }
                    }
                    if(sucCount == channels.length){
                        oriOutputFile.delete();
                    }
                } catch (Exception e) {
                    LogMe.D("startMultiCreateApk = Exception" + e);

                }
                LogMe.D("*************************多渠道打包Process-End*****************************");
            }
        });
    }

    //进行zip文件的注释修改
    private boolean optUseV1(File apkPath, String channelStr, Map<String,String> extraInfo,String passWord) throws Exception{
        //build str
        final Map<String,String> newData = new HashMap<>();
        if(null != extraInfo && !extraInfo.isEmpty()){
            newData.putAll(extraInfo);
        }
        newData.put(Constants.sChannelKey,channelStr);
        final JSONObject jsonObject = new JSONObject(newData);
        V1ChannelUtil.writeCommit(apkPath, jsonObject.toString(), passWord);
        String testReadChannel = V1ChannelUtil.getChannelId(apkPath.getAbsolutePath(), "", "unKnown");
        LogMe.D("startMultiCreateApk work = optUseV1 = 读取写入的渠道信息 = " + testReadChannel);
        if (!channelStr.contains(testReadChannel)) {
            throw new RuntimeException("V1多渠道打包失败 = 写入的的渠道为 :" + channelStr + "\r\n" + "读取到的渠道为：" + testReadChannel);
        }
        return true;
    }
    private boolean optUseV2(File apkPath, String channelStr, boolean check, boolean useLowMemory, Map<String,String> extraInfo) throws Exception{
        LogMe.D("startMultiCreateApk work = optUseV2 = " + channelStr);
        ChannelWriter.put(apkPath, channelStr, extraInfo, useLowMemory);
        //校验下写入的是否与读取到的相同
        if (check) {
            ChannelInfo channelInfo = ChannelReader.get(apkPath);
            String testReadChannel = null != channelInfo ? channelInfo.getChannel() : "";
            LogMe.D("startMultiCreateApk work = optUseV2 = 读取写入的渠道 = " + testReadChannel);
            if (!testReadChannel.equals(channelStr)) {
                throw new RuntimeException("V2多渠道打包失败 = 写入的的渠道为 :" + channelStr + "\r\n" + "读取到的渠道为：" + testReadChannel);
            }
            return true;
        }
        return true;
    }

    private ApkVerifier.Result signerVerify(File apkFile) {
        try {
            ApkVerifier.Builder builder = new ApkVerifier.Builder(apkFile);
            ApkVerifier apkVerifier = builder.build();
            return apkVerifier.verify();
        } catch (Exception e) {
            if (LogMe.isDebug) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
