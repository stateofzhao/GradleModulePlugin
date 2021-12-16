package com.zfun.funmodule.processplug;

import com.zfun.funmodule.processplug.extension.InjectEx;
import com.zfun.funmodule.processplug.process.EmptyProcess;
import com.zfun.funmodule.processplug.process.InjectionProcess;
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
