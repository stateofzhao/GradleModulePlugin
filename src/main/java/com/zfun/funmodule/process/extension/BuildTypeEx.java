package com.zfun.funmodule.process.extension;

import com.zfun.funmodule.BaseExtension;
import org.gradle.api.Project;

/**
 * Created by zfun on 2022/3/22 10:06
 */
public class BuildTypeEx extends BaseExtension {
    public boolean debug;
    public BuildTypeEx(Project project) {
        super(project);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return "BuildTypeEx{" +
                "debug=" + debug +
                '}';
    }
}
