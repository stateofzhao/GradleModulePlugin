package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.processplug.ProjectFileRestoreMgr;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import org.gradle.BuildResult;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

//apply plugin 更改为 com.android.application
public class AppProcess implements IProcess {
    boolean needRecoverBuildFile = false;
    private String oriBuildFile;

    @Override
    public void beforeEvaluate(Project project) {
        try {
            final File srcBuildGradle = project.getBuildFile();
            oriBuildFile = srcBuildGradle.getAbsolutePath();
            final String oriText = FileUtil.getText(srcBuildGradle);
            //1，更改library插件->application插件
            String replaceText = oriText.replaceAll("'com\\.android\\.library'", "'com.android.application'"+Constants.sComments + "：com.android.library -> com.android.application");
            replaceText = replaceText.replaceAll("\"com\\.android\\.library\"", "'com.android.application'"+Constants.sComments + "：com.android.library -> com.android.application");
            if(!oriText.equals(replaceText)){
                needRecoverBuildFile = true;
                ProjectFileRestoreMgr.saveFile(project,oriBuildFile);//将build.gradle复制走，用完了再还回来~
                LogMe.D("AppProcess - 变更library插件后：");
                LogMe.D(replaceText);
                LogMe.D("==========================");
            }
            if(needRecoverBuildFile){
                FileUtil.write(srcBuildGradle, replaceText);
            }
        } catch (IOException e) {
            throw new RuntimeException("AppProcess == 更改 "+project.getName() +" build.gradle文件：library插件->application插件 【失败】："+e.getMessage());
        }
    }

    @Override
    public void afterEvaluate(Project project) {
    }

    @Override
    public void projectsEvaluated(Project project) {

    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {
        //build.gradle的还原可以放到 afterEvaluate() 回调中，也是没问题的
        try {
            if (needRecoverBuildFile) {
                ProjectFileRestoreMgr.restoreFile(project,oriBuildFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("AppProcess == 还原 "+project.getName() +" build.gradle文件【失败】："+e.getMessage());
        }
    }
}
