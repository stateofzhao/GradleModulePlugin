package com.zfun.funmodule;

import com.zfun.funmodule.process.*;
import org.gradle.api.Project;

public class ProcessFactory {

    public IProcess createProcess(Project project, BaseExtension extension) {
        if (project.getRootProject() == project) {
            return new ConfigBuildFileProcess();
        }

        if (extension instanceof ModulesEx) {
            ModulesEx modulesEx = (ModulesEx) extension;
            if (null == modulesEx.libName) {
                return new EmptyProcess();
            }
            final int runType = modulesEx.runType;
            final String mainAppName = modulesEx.mainAppName;
            if (mainAppName.equals(extension.moduleName)) {
                return new EmptyProcess();
            }

            if(!isInLibName(extension)){
                return new EmptyProcess();
            }

            if (Constants.sRunTypeApp == runType) {
                return new LibProcess();
            } else if (Constants.sRunTypeAll == runType) {
                return new LibAppProcess();
            } else if (Constants.sRunTypeModule == runType) {
                return new AppProcess();
            }
        }
        return new EmptyProcess();
    }

    private boolean isInLibName(BaseExtension extension) {
        ModulesEx modulesEx = (ModulesEx) extension;
        if (null == modulesEx.libName) {
            return false;
        }
        String myName = extension.moduleName;
        for (String aLibName : modulesEx.libName) {
            if(aLibName.equals(myName)){
                return true;
            }
        }
        return false;
    }
}
