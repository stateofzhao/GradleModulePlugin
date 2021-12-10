package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;

import java.util.Map;

public class InjectEx extends BaseExtension {
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
