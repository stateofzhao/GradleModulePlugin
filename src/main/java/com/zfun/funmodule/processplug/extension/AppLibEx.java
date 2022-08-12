package com.zfun.funmodule.processplug.extension;

import com.zfun.funmodule.BaseExtension;
import org.gradle.api.Project;

import java.util.Arrays;

public class AppLibEx extends BaseExtension {
    public String mainAppName;//壳app的module名称
    public String[] libName;
    public String runType;//运行模式

    public AppLibEx(Project project) {
        super(project);
    }

    @Override
    public boolean isEmpty() {
        return null == runType || runType.length() == 0;
    }

    @Override
    public String toString() {
        return "AppLibEx{" +
                "mainAppName='" + mainAppName + '\'' +
                ", libName=" + Arrays.toString(libName) +
                ", runType=" + runType +
                '}';
    }
}
