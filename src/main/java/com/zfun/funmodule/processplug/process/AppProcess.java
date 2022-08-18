package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import org.gradle.BuildResult;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

//apply plugin 更改为 com.android.application
public class AppProcess implements IProcess {
    boolean needRecoverBuildFile = false;
    private File oriBuildFile;
    private File savedBuildFile;

    @Override
    public void beforeEvaluate(Project project) {
        try {
            //将build.gradle复制走，用完了再还回来~
            final String moduleName = project.getName();
            File desParentDir = new File(project.getRootDir() + "/" + Constants.sBuildTempFile+"/" + moduleName + "_build.gradle" );
            if (!desParentDir.exists()) {
                desParentDir.mkdirs();
            }
            final File srcBuildGradle = project.getBuildFile();
            final File desBuildGradle = new File(desParentDir.getAbsolutePath() , Constants.sBuildGradleName);
            oriBuildFile = srcBuildGradle;
            savedBuildFile = desBuildGradle;
            if (desBuildGradle.exists()) {
                desBuildGradle.delete();
            }
            FileUtil.copy(srcBuildGradle, desBuildGradle);

            final String oriText = FileUtil.getText(project.getBuildFile());
            //1，更改library插件->application插件
            String replaceText = oriText.replaceAll("'com\\.android\\.library'", "'com.android.application'"+Constants.sComments + "：com.android.library -> com.android.application");
            replaceText = replaceText.replaceAll("\"com\\.android\\.library\"", "'com.android.application'"+Constants.sComments + "：com.android.library -> com.android.application");
            if(!oriText.equals(replaceText)){
                needRecoverBuildFile = true;
                LogMe.D("AppProcess - 变更library插件后：");
                LogMe.D(replaceText);
                LogMe.D("==========================");
            }
            if(needRecoverBuildFile){
                FileUtil.write(project.getBuildFile(), replaceText);
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
                if(oriBuildFile.exists()){
                    oriBuildFile.delete();
                }
                FileUtil.copy(savedBuildFile, oriBuildFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("AppProcess == 还原 "+project.getName() +" build.gradle文件【失败】："+e.getMessage());
        }
    }
}
