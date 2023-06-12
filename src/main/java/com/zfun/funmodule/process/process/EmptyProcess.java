package com.zfun.funmodule.process.process;

import com.zfun.funmodule.process.IProcess;
import org.gradle.BuildResult;
import org.gradle.api.Project;

public class EmptyProcess implements IProcess {
    @Override
    public void beforeEvaluate(Project project) {

    }

    @Override
    public void afterEvaluate(Project project) {

    }

    @Override
    public void projectsEvaluated(Project project) {

    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {

    }
}
