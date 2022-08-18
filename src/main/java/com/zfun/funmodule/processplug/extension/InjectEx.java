package com.zfun.funmodule.processplug.extension;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.util.StringUtils;
import org.gradle.api.Project;

import java.util.Arrays;
import java.util.Map;

//build.gradle - injectCode = ["modulea":"gradle_code.txt"]
public class InjectEx extends BaseExtension {
    //injectCode 与 injectCodePath 互斥，优先使用 injectCode
    //group 1
    public Map<String,String> injectCode;

    //group 2
    public String injectCodePath;
    public String[] excludeProjectName;
    public InjectEx(Project project) {
        super(project);
    }

    @Override
    public boolean isEmpty() {
        return (null == injectCode || injectCode.size() == 0) && StringUtils.isEmpty(injectCodePath);
    }

    @Override
    public String toString() {
        return "InjectEx{" +
                "injectCode=" + injectCode +
                ", injectCodePath='" + injectCodePath + '\'' +
                ", excludeProjectName=" + Arrays.toString(excludeProjectName) +
                '}';
    }
}
