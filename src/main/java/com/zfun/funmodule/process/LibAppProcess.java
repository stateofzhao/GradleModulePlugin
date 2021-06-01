package com.zfun.funmodule.process;

import com.zfun.funmodule.IProcess;
import com.zfun.funmodule.util.FileUtil;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import java.io.IOException;

//apply plugin 更改为 com.android.library，但是保留Manifest中的launcherActivity
public class LibAppProcess implements IProcess {
    boolean needRecover = false;
    @Override
    public void beforeEvaluate(Project project) {
        try {
            needRecover = FileUtil.replaceFileText(project.getBuildFile(), "com\\.android\\.application", "com.android.library");
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void afterEvaluate(Project project) {
    }

    @Override
    public void buildStarted(Project project, Gradle gradle) {
    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {
        //build.gradle的还原可以放到 afterEvaluate() 回调中，也是没问题的
        try {
            if(needRecover){
                FileUtil.replaceFileText(project.getBuildFile(), "com\\.android\\.library", "com.android.application");
            }
        } catch (IOException e) {
            //
        }
    }
}
