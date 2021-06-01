package com.zfun.funmodule;

import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

public interface IProcess {
    void beforeEvaluate(Project project);

    void afterEvaluate(Project project);

    void buildStarted(Project project,Gradle gradle);

    void buildFinished(Project project,BuildResult buildResult);
}
