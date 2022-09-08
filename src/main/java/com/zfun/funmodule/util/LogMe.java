package com.zfun.funmodule.util;

public class LogMe {
    public static boolean isDebug = false;

    public static String sDividerLeft = " ========== ";
    public static String sDividerRight = " ========== ";
    public static String sDivider = "==============================";


    public static void D(String msg) {
        if (isDebug) {
            System.out.println(msg);
        }
    }

    public static void D_Divider(String start,String elementName){
        D(divider(start,elementName));
    }

    public static void D_Divider(){
        D(sDivider);
    }

    public static void P(String msg) {
        System.out.println(msg);
    }

    public static String divider(String start, String elementName) {
        return start + sDividerLeft + elementName + sDividerRight;
    }
}
