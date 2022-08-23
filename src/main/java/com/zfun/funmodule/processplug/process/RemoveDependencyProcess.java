package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.processplug.ProjectFileRestoreMgr;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.StringUtils;
import org.gradle.BuildResult;
import org.gradle.api.Project;

import java.io.File;

/**
 * 1，更改application插件为lib插件；
 * 2，删除对lib工程的依赖；
 * 3，删除build.gradle中的android{applicationId}；
 * */
public class RemoveDependencyProcess implements IProcess {
    final String[] libName;
    boolean needRecoverBuildFile = false;
    private String oriBuildFile;

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

            final File srcBuildGradle = project.getBuildFile();
            oriBuildFile = srcBuildGradle.getAbsolutePath();

            final String oriText = FileUtil.getText(srcBuildGradle);
            //1，更改application插件->lib插件
            String replaceText = oriText.replaceAll("'com\\.android\\.application'", "'com.android.library'"+Constants.sComments + "：com.android.application -> com.android.library");
            replaceText = replaceText.replaceAll("\"com\\.android\\.application\"", "'com.android.library'"+Constants.sComments + "：com.android.application -> com.android.library");
            //2，移除applicationId
            replaceText = StringUtils.editGradleText(Constants.sDefaultApplicationIdRegexPre,"applicationId",replaceText,null);
            //3.移除对子模块的依赖
            for (String aLibName : libName) {
                String regex = Constants.sDefaultDependenciesRegexPre+aLibName+Constants.sDefaultDependenciesRegexEnd;
                replaceText = replaceText.replaceAll(regex,Constants.sComments + "：删除了此行代码");
            }
            needRecoverBuildFile = !oriText.equals(replaceText);
            if(needRecoverBuildFile){
                LogMe.D("【RemoveDependencyProcess】移除application插件、applicationId和对lib的依赖后：");
                LogMe.D(replaceText);
                LogMe.D("==========================");
                ProjectFileRestoreMgr.saveFile(project,oriBuildFile);
                FileUtil.write(srcBuildGradle, replaceText);
            }
        } catch (Exception e) {
            throw new RuntimeException("RemoveDependencyProcess == 更改 "+project.getName() +" build.gradle文件【失败】："+e.getLocalizedMessage());
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
            if(needRecoverBuildFile){
                ProjectFileRestoreMgr.restoreFile(project,oriBuildFile);
            }
        } catch (Exception e) {
            throw new RuntimeException("RemoveDependencyProcess == 还原 "+project.getName() +" build.gradle文件【失败】:"+e.getLocalizedMessage());
        }
    }
}
