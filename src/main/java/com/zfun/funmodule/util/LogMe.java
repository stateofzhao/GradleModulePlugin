package com.zfun.funmodule.util;

public class LogMe {
    public static boolean isDebug = false;

    public static void D(String msg) {
        if (isDebug) {
            System.out.println(msg);
        }
    }

    public static void P(String msg){
        System.out.println(msg);
    }
}
