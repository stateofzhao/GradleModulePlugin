package com.zfun.funmodule.util;

public class LogMe {
    public static boolean isDebug = false;

    public static String sDividerLeft = " ========== ";
    public static String sDividerRight = " ========== ";
    public static String sDivider = "==============================";


    public static void D(String msg) {
        D(msg,true);
    }

    public static void D(String msg, boolean newLine) {
        if (isDebug) {
            if (newLine) {
                System.out.println(msg);
            }else {
                System.out.print(msg);
            }
        }
    }

    public static void D_Divider(String start, String elementName) {
        D(divider(start, elementName));
    }

    public static void D_Divider() {
        D(sDivider);
    }

    public static void P(String msg) {
        System.out.println(msg);
    }

    public static String divider(String start, String elementName) {
        return start + sDividerLeft + elementName + sDividerRight;
    }
}
