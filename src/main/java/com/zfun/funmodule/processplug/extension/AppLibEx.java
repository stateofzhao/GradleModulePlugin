package com.zfun.funmodule.processplug.extension;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.util.StringUtils;
import org.gradle.api.Project;

import java.util.Arrays;

public class AppLibEx extends BaseExtension {
    public String mainAppName;//壳app的module名称
    public String[] libName;
    public int runType;//运行模式

    public AppLibEx(Project project) {
        super(project);
    }

    @Override
    public boolean isEmpty() {
        boolean isLibNameEmpty = false;
        if(null == libName || libName.length ==0){
            isLibNameEmpty = true;
        }else {
            for(String aName:libName){
                if(!StringUtils.isEmpty(aName)){
                    break;
                }
            }
        }
        return isLibNameEmpty && StringUtils.isEmpty(mainAppName) && 0 == runType;
    }

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
