package com.zfun.funmodule.processplug.extension;

import com.zfun.funmodule.BaseExtension;
import org.gradle.api.Project;

import java.util.Map;

//build.gradle - injectCode = ["modulea":"gradle_code.txt"]
public class InjectEx extends BaseExtension {
    public Map<String,String> injectCode;
    public InjectEx(Project project) {
        super(project);
    }

    @Override
    public boolean isEmpty() {
        return null == injectCode || injectCode.size() == 0;
    }

    @Override
    public String toString() {
        return "InjectEx{" +
                "injectCode=" + injectCode +
                '}';
    }
}
