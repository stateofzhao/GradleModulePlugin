package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import java.io.File;

public class RemoveDependencyProcess implements IProcess {
    final String[] libName;
    boolean needRecover = false;
    private File oriFile;
    private File savedFile;

    public RemoveDependencyProcess(String[] libName) {
        this.libName = libName;
    }

    @Override
    public void beforeEvaluate(Project project) {
        try {
            if(null == libName){
                return;
            }
            //将build.gradle复制走，用完了再还回来~
            final String moduleName = project.getName();
            File desParentDir = new File(project.getRootDir() + "/" + Constants.sBuildTempFile+"/" + moduleName + "_build.gradle" );
            if (!desParentDir.exists()) {
                desParentDir.mkdirs();
            }
            final File srcBuildGradle = project.getBuildFile();
            final File desBuildGradle = new File(desParentDir.getAbsolutePath() , Constants.sBuildGradleName);
            oriFile = srcBuildGradle;
            savedFile = desBuildGradle;
            if (desBuildGradle.exists()) {
                desBuildGradle.delete();
            }
            FileUtil.copy(srcBuildGradle, desBuildGradle);

            final String oriText = FileUtil.getText(project.getBuildFile());
            String replaceText = oriText;
            for (String aLibName : libName) {
                String regex = Constants.sDefaultDependenciesRegexPre+aLibName+Constants.sDefaultDependenciesRegexEnd;
                replaceText = replaceText.replaceAll(regex,"");
                LogMe.D("正则表达式："+regex);
            }
            needRecover = !oriText.equals(replaceText);
            if(needRecover){
                FileUtil.write(project.getBuildFile(), replaceText);
            }
            LogMe.D("修改完app-build.gradle后的内容：");
            LogMe.D(replaceText);
        } catch (Exception e) {
            LogMe.D("RemoveDependencyProcess-beforeEvaluate- Exception："+e.getLocalizedMessage());
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
        try {
            if(needRecover){
                if(oriFile.exists()){
                    oriFile.delete();
                }
                FileUtil.copy(savedFile, oriFile);
            }
        } catch (Exception e) {
            LogMe.D("RemoveDependencyProcess-buildFinished- Exception："+e.getLocalizedMessage());
        }
    }
}
