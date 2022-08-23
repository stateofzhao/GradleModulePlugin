package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;
import org.gradle.api.Project;

public interface IProcessFactory<T extends BaseExtension> {
    /**
     * @param project 子Project
     * @param extension 配置在根build.gradle中的参数
     *
     * @return 根据 project 与 扩展参数 返回此需要处理此Project的{@link IProcess}
     * */
    IProcess createProcess(Project project, T extension);
}
