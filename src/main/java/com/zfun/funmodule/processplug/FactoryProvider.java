package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.processplug.extension.AppLibEx;
import com.zfun.funmodule.processplug.extension.InjectEx;
import com.zfun.funmodule.processplug.extension.MultiChannelEx;
import org.gradle.api.Project;

public class FactoryProvider {
    IProcessFactory<? extends BaseExtension> createFactory(Project project, BaseExtension extension) {
        if(extension instanceof AppLibEx){
            return new AppLibFactory();
        }
        if(extension instanceof InjectEx){
            return new InjectFactory();
        }
        if(extension instanceof MultiChannelEx){
            return new MultiChannelFactory();
        }
        return null;
    }
}
