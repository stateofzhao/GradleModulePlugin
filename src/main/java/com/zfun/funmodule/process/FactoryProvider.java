package com.zfun.funmodule.process;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.process.extension.AppLibEx;
import com.zfun.funmodule.process.extension.InjectEx;
import com.zfun.funmodule.process.extension.MultiChannelEx;
import org.gradle.api.Project;

public class FactoryProvider {
    public IProcessFactory<? extends BaseExtension> createFactory(Project project, BaseExtension extension) {
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
