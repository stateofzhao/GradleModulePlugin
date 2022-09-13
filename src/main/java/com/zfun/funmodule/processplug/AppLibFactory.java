package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.extension.AppLibEx;
import com.zfun.funmodule.processplug.process.*;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppLibFactory implements IProcessFactory<AppLibEx> {

    public List<IProcess> createProcess(Project project, AppLibEx appLibEx) {
        if(null == appLibEx){
            return Collections.emptyList();
        }
        if (null == appLibEx.libName) {
            return Collections.emptyList();
        }
        final List<IProcess> processList = new ArrayList<>();
        final String projectName = project.getName();
        final String runType = appLibEx.runType;
        final String mainAppName = appLibEx.appProjectName;
        //针对mainApp进行处理
        if (mainAppName.equals(projectName)) {
            if (Constants.sRunTypeModule.equalsIgnoreCase(runType)||Constants.sRunTypeModule_Deprecated.equalsIgnoreCase(runType)) {
                processList.add(new RemoveDependencyProcess(appLibEx.libName));
//                processList.add(new LibProcess());
            }
            return processList;
        }

        if (!isInLibName(project,appLibEx)) {
            return Collections.emptyList();
        }

        if (Constants.sRunTypeApp.equalsIgnoreCase(runType) || Constants.sRunTypeApp_Deprecated.equalsIgnoreCase(runType)) {
            processList.add(new LibProcess());
            return processList;
        }
        return Collections.emptyList();
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
