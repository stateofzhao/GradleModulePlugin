package com.zfun.funmodule.processplug.extension;

import com.zfun.funmodule.BaseExtension;
import org.gradle.api.Project;

/**
 * Created by zfun on 2022/3/22 10:06
 */
public class DebugEx extends BaseExtension {
    //Constants.BUILD_DEBUG；Constants.BUILD_RELEASE
    public int buildType;
    public DebugEx(Project project) {
        super(project);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return "DebugEx{" +
                "buildType=" + buildType +
                '}';
    }
}
