package com.zfun.funmodule.processplug.process;

import com.android.annotations.NonNull;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension;
import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.processplug.extension.ChannelExtension;
import com.zfun.funmodule.processplug.extension.MultiChannelEx;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.ZipUtil;
import org.apache.commons.io.FilenameUtils;
import org.gradle.BuildResult;
import org.gradle.api.*;
import org.gradle.api.invocation.Gradle;

import java.io.File;
import java.util.function.Consumer;

/**
 * 多渠道打包。渠道信息不能大于 32767 个字节<br/>
 * <p>
 * <p>
 * Created by zfun on 2021/12/10 4:30 PM
 */
public class MultiChannelV1Process implements IProcess {

    @Override
    public void beforeEvaluate(Project project) {

    }

    @Override
    public void afterEvaluate(Project project) {
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
                            startMultiCreateApk(project, applicationVariant, multiChannelEx, channelExtension);
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
    public void buildStarted(Project project, Gradle gradle) {

    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {

    }

    private void startMultiCreateApk(@NonNull final Project project,
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
                    final String apkPath = FilenameUtils.getFullPathNoEndSeparator(oriOutputFile.getAbsolutePath());
                    for (String aChannel : channels) {
                        LogMe.D("startMultiCreateApk work = " + aChannel);
                        final String finalOutFileFullPath = apkPath + "/" + prefix + aChannel + subfix + ".apk";
                        final File desOutFile = new File(finalOutFileFullPath);
                        if(desOutFile.exists()){
                            desOutFile.delete();
                        }
                        FileUtil.copy(oriOutputFile,desOutFile);
                        //进行zip文件的注释修改
                        ZipUtil.writeCommit(desOutFile, aChannel, "");
                        String testReadChannel = ZipUtil.getChannelId(finalOutFileFullPath, "", "unKnown");
                        LogMe.D("startMultiCreateApk 读取写入的渠道信息 = "+testReadChannel);
                        if(!aChannel.equals(testReadChannel)){
                            throw new RuntimeException("多渠道打包失败 = 写入的的渠道为 :"+aChannel+"\r\n"+"读取到的渠道为："+testReadChannel);
                        }
                    }
                } catch (Exception e) {
                    LogMe.D("startMultiCreateApk = Exception" + e);
                }
            }
        });
    }
}
