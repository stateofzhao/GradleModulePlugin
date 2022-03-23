package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;
import org.gradle.api.Project;

public interface IProcessFactory<T extends BaseExtension> {
    /**
     * @param project 子Project
     * @param extension 配置在根build.gradle中的参数
     * */
    IProcess createProcess(Project project, T extension);
}
