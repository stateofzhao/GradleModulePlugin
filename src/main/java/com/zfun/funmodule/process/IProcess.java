package com.zfun.funmodule.process;

import org.gradle.BuildResult;
import org.gradle.api.Project;

public interface IProcess {
    void beforeEvaluate(Project project);

    void afterEvaluate(Project project);

    void projectsEvaluated(Project project);

    void buildFinished(Project project,BuildResult buildResult);
}
