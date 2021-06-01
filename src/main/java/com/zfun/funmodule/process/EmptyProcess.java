package com.zfun.funmodule.process;

import com.zfun.funmodule.IProcess;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

public class EmptyProcess implements IProcess {
    @Override
    public void beforeEvaluate(Project project) {

    }

    @Override
    public void afterEvaluate(Project project) {

    }

    @Override
    public void buildStarted(Project project, Gradle gradle) {

    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {

    }
}
