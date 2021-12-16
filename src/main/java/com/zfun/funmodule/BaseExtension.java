package com.zfun.funmodule;

import org.gradle.api.Project;

public abstract class BaseExtension {
    public int buildType;
    public String moduleName;

    public BaseExtension(Project project){

    }

    /**
     * project.getExtensions().create(String,Extension.class)
     * 在gradle中，一旦给Project创建了Extension，那么无论用户是否在build.gradle中声明Extension，始终可以获取到Extension。
     * 是否用户在build.gradle文件中声明了：true声明了；false没有声明。<br/>
     *
     * @return true表示没有值，此时不会生产任何Process;
     * */
    public abstract boolean isEmpty();
}
