package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;
import org.gradle.api.Project;

public interface IProcessFactory<T extends BaseExtension> {
    IProcess createProcess(Project project, T extension);
}
