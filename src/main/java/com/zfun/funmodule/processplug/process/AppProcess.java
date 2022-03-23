package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.util.FileUtil;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import java.io.IOException;

//apply plugin 更改为 com.android.application
public class AppProcess implements IProcess {
    boolean needRecover = false;

    @Override
    public void beforeEvaluate(Project project) {
        try {
            needRecover = FileUtil.replaceFileText(project.getBuildFile(), "com\\.android\\.library", "com.android.application");
        } catch (IOException e) {
            //
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
            if(needRecover){
                FileUtil.replaceFileText(project.getBuildFile(), "com\\.android\\.application", "com.android.library");
            }
        } catch (IOException e) {
            //
        }
    }
}
