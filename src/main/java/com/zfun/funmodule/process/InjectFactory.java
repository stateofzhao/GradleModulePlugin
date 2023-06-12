package com.zfun.funmodule.process;

import com.zfun.funmodule.process.extension.InjectEx;
import com.zfun.funmodule.process.process.InjectionProcess;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InjectFactory implements IProcessFactory<InjectEx> {
    @Override
    public List<IProcess> createProcess(Project project, InjectEx extension) {
        if (null == extension || extension.isEmpty()) {
            return Collections.emptyList();
        }
        final List<IProcess> processList = new ArrayList<>();
        processList.add(new InjectionProcess(extension));
        return processList;
    }
}
