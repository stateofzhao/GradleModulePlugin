package com.zfun.funmodule.process.extension;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.util.StringUtils;
import org.gradle.api.Project;

import java.util.Arrays;

public class AppLibEx extends BaseExtension {
    public String appProjectName;//壳app的module名称
    public String[] libName;
    public String runType;//运行模式

    public AppLibEx(Project project) {
        super(project);
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(appProjectName) || StringUtils.isEmpty(runType);
    }

    @Override
    public String toString() {
        return "AppLibEx{" +
                "appProjectName='" + appProjectName + '\'' +
                ", libName=" + Arrays.toString(libName) +
                ", runType=" + runType +
                '}';
    }
}
