package com.zfun.funmodule;

import org.gradle.api.Project;

public class FactoryProvider {
    IProcessFactory<? extends BaseExtension> createFactory(Project project, BaseExtension extension) {
        if(extension instanceof AppLibEx){
            return new AppLibFactory();
        }
        if(extension instanceof InjectEx){
            return new InjectFactory();
        }
        return null;
    }
}
