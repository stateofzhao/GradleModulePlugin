package com.zfun.funmodule;

public class Constants {
    public static final String sBuildTempFile = ".idea";
    public static final String sManifestName = "AndroidManifest.xml";
    public static final String sManifestDir = "/src/main/";
    public static final String sManifestPath = sManifestDir + sManifestName;
    public static final String sDefaultDependenciesRegexPre = "[a-zA-Z]+\\s+project+\\s*[(]+\\s*['|\"]+:";
    public static final String sDefaultDependenciesRegexEnd = "['|\"]+\\s*[)]";
    public static final String sBuildGradleName = "build.gradle";

    public static final String sAppLibExtensionName = "funAppLib";
    public static final String sInjectExtensionName = "funInject";
    public static final String sMultiChannelExName = "multiChannel";
    public static final String sDebugExtensionName = "debug";
    public static final String sDefaultAppName = "app";
    public static final int sRunTypeApp = 1;//只有壳app可以安装运行，所有lib的launchActivity都会被去掉launch属性
    public static final int sRunTypeAll = 2;//运行壳app时，会将所有可以独立运行的module显示出来（相当于将module的launchActivity合并到壳app的manifest中）
    public static final int sRunTypeModule = 3;//各个module各自可以独立运行，壳app不能运行

    public final static int BUILD_DEBUG = 1;
    public final static int BUILD_RELEASE = 2;
}
