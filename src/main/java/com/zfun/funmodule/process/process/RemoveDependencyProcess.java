package com.zfun.funmodule.process.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.process.IProcess;
import com.zfun.funmodule.process.ProjectFileRestoreMgr;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import org.gradle.BuildResult;
import org.gradle.api.Project;

import java.io.File;

/**
 * 删除对变为application的lib工程的依赖；
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
            String replaceText = oriText;
            //移除对子模块的依赖
            for (String aLibName : libName) {
                String regex = Constants.sDefaultDependenciesRegexPre+aLibName+Constants.sDefaultDependenciesRegexEnd;
                replaceText = oriText.replaceAll(regex,Constants.sComments + "：删除了此行代码");
            }
            needRecoverBuildFile = !oriText.equals(replaceText);
            if(needRecoverBuildFile){
                LogMe.D_Divider(project.getName(),"【RemoveDependencyProcess】移除对lib的依赖");
                ProjectFileRestoreMgr.saveFile(project,oriBuildFile);
                FileUtil.write(srcBuildGradle, replaceText);
                FileUtil.copyRealUsedFile2Temp(srcBuildGradle,project);
            }
        } catch (Exception e) {
            throw new RuntimeException("【RemoveDependencyProcess】== 更改 "+project.getName() +" build.gradle 文件【失败】："+e.getLocalizedMessage());
        }
    }

    @Override
    public void afterEvaluate(Project project) {
        try {
            if(needRecoverBuildFile){
                ProjectFileRestoreMgr.restoreFile(project,oriBuildFile);
                LogMe.D_Divider(project.getName(),"【RemoveDependencyProcess】build.gradle 还原完成");
            }
        } catch (Exception e) {
            throw new RuntimeException("【RemoveDependencyProcess】== 还原 "+project.getName() +" build.gradle文件【失败】:"+e.getLocalizedMessage());
        }

    }

    @Override
    public void projectsEvaluated(Project project) {

    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {

    }
}
