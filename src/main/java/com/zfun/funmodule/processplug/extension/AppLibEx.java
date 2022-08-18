package com.zfun.funmodule.processplug.extension;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.util.StringUtils;
import org.gradle.api.Project;

import java.util.Arrays;

public class AppLibEx extends BaseExtension {
    public String packageProjectName;//壳app的module名称
    public String[] libName;
    public String runType;//运行模式

    public AppLibEx(Project project) {
        super(project);
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(packageProjectName) || StringUtils.isEmpty(runType);
    }

    @Override
    public String toString() {
        return "AppLibEx{" +
                "packageProjectName='" + packageProjectName + '\'' +
                ", libName=" + Arrays.toString(libName) +
                ", runType=" + runType +
                '}';
    }
}
