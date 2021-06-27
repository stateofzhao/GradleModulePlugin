package com.zfun.funmodule;

import org.gradle.api.Project;

public interface IProcessFactory<T extends BaseExtension> {
    IProcess createProcess(Project project, T extension);
}
