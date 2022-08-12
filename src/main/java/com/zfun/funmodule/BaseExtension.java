package com.zfun.funmodule;

import org.gradle.api.Project;

public abstract class BaseExtension {
    public String extensionName;
    public BaseExtension(Project project){

    }

    /**
     * project.getExtensions().create(String,Extension.class) <br>
     * 在 Gradle 中，一旦给 Project 创建了 Extension，那么无论用户是否在 build.gradle 文件中声明 Extension，都可以获取到 Extension（里面的值都为默认值）。<br>
     *
     * @return 用户是否在 build.gradle 文件中定义了 Extension 的值：true声明了；false没有声明。如果返回 true，不会生产任何Process;
     * */
    public abstract boolean isEmpty();
}
