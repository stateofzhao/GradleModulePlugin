package com.zfun.funmodule;

import java.util.Map;

public class InjectEx extends BaseExtension{
    public Map<String,String> injectCode;

    @Override
    public String toString() {
        return "InjectEx{" +
                "buildType=" + buildType +
                ", moduleName='" + moduleName + '\'' +
                ", injectCode=" + injectCode +
                '}';
    }
}
