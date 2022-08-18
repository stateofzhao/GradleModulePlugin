package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.extension.AppLibEx;
import com.zfun.funmodule.processplug.process.*;
import org.gradle.api.Project;

public class AppLibFactory implements IProcessFactory<AppLibEx> {

    public IProcess createProcess(Project project, AppLibEx appLibEx) {
        if (project.getRootProject() == project) {
            return new ConfigBuildFileProcess();
        }
        if(null == appLibEx){
            return new EmptyProcess();
        }

        if (null == appLibEx.libName) {
            return new EmptyProcess();
        }
        final String projectName = project.getName();
        final String runType = appLibEx.runType;
        final String mainAppName = appLibEx.packageProjectName;
        //针对mainApp进行处理
        if (mainAppName.equals(projectName)) {
            if (Constants.sRunTypeModule.equalsIgnoreCase(runType)) {
                return new RemoveDependencyProcess(appLibEx.libName);
            }
            return new EmptyProcess();
        }

        if (!isInLibName(project,appLibEx)) {
            return new EmptyProcess();
        }

        if (Constants.sRunTypeApp.equalsIgnoreCase(runType)) {
            return new LibProcess();
        }
        /*else if (Constants.sRunTypeModule.equalsIgnoreCase(runType)) {
            return new AppProcess();
        }*/

        return new EmptyProcess();
    }

    private boolean isInLibName(Project project,BaseExtension extension) {
        AppLibEx appLibEx = (AppLibEx) extension;
        if (null == appLibEx.libName) {
            return false;
        }
        String myName = project.getName();
        for (String aLibName : appLibEx.libName) {
            if (aLibName.equals(myName)) {
                return true;
            }
        }
        return false;
    }
}
