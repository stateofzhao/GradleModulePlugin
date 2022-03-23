package com.zfun.funmodule.processplug;

import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

public interface IProcess {
    void beforeEvaluate(Project project);

    void afterEvaluate(Project project);

    void projectsEvaluated(Project project);

    void buildFinished(Project project,BuildResult buildResult);
}
