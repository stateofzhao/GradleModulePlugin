package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.processplug.IProcess;
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
    public void buildFinished(Project project, BuildResult buildResult) {

    }
}
