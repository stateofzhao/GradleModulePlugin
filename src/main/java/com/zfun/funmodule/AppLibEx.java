package com.zfun.funmodule;

import java.util.Arrays;

public class AppLibEx extends BaseExtension {
    public String mainAppName;//壳app的module名称
    public String[] libName;
    public int runType;//运行模式

    @Override
    public String toString() {
        return "ModulesEx{" +
                "buildType=" + buildType +
                ", moduleName='" + moduleName + '\'' +
                ", mainAppName='" + mainAppName + '\'' +
                ", libName=" + Arrays.toString(libName) +
                ", runType=" + runType +
                '}';
    }
}
