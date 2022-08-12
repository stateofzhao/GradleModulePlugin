package com.zfun.funmodule.util;

import org.gradle.api.Project;

/**
 * Created by zfun on 2021/12/14 12:10 PM
 */
public class Util {
    public static boolean isChild(Class<?> clazz,Class<?> parent){
        Class<?>[] interfacesArray = clazz.getInterfaces();//获取这个类的所以接口类数组
        for (Class<?> item : interfacesArray) {
            if (item == parent) { //判断是否有继承的接口
                return true;
            }
        }
        return false;
    }

    public static boolean isRootProject(Project project){
        if(null == project){
            return false;
        }
        return project.getRootProject().getPath().equals(project.getPath());
    }
}
