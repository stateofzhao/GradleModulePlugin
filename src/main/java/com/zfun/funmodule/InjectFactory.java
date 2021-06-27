package com.zfun.funmodule;

import com.zfun.funmodule.process.EmptyProcess;
import com.zfun.funmodule.process.InjectionProcess;
import org.gradle.api.Project;

public class InjectFactory implements IProcessFactory<InjectEx> {
    @Override
    public IProcess createProcess(Project project, InjectEx extension) {
        if (null == extension || null == extension.injectCode || extension.injectCode.size() == 0) {
            return new EmptyProcess();
        }
        return new InjectionProcess(extension);
    }
}
