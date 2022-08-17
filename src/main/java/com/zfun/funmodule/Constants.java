package com.zfun.funmodule;

public class Constants {
    public static final String sComments = "//【io.github.stateofzhao插件修改，编译后如果出现此修改，则需要手动还原回去】";
    public static final String sChannelKey = "channel";
    public static final String sBuildTempFile = ".idea";
    public static final String sManifestName = "AndroidManifest.xml";
    public static final String sManifestDir = "/src/main/";
    public static final String sManifestPath = sManifestDir + sManifestName;
    public static final String sDefaultDependenciesRegexPre = "[a-zA-Z]+\\s+project+\\s*[(]+\\s*['|\"]+:";
    public static final String sDefaultDependenciesRegexEnd = "['|\"]+\\s*[)]";
    public static final String sDefaultApplicationIdRegexPre = "android[ \\n\\t]*\\{[\\s\\S]*defaultConfig[ \\n\\t]*[\\s\\S]*applicationId[ \\t\\n]*['\\\"][a-zA-Z0-9\\._]+['\\\"]";
    public static final String sBuildGradleName = "build.gradle";

    public static final String sAppLibExtensionName = "funAppLib";
    public static final String sInjectExtensionName = "funInject";
    public static final String sMultiChannelExName = "multiChannel";
    public static final String sDebugExtensionName = "funBuildType";
    public static final String sDefaultAppName = "app";
    public static final String sRunTypeApp = "app";//只有壳app可以安装运行，所有lib的launchActivity都会被去掉launch属性
    public static final String sRunTypeModule = "module";//各个module各自可以独立运行，壳app不能运行
    //fixme 这个有问题暂时屏蔽
    //public static final String sRunTypeAll = "all";//运行壳app时，会将所有可以独立运行的module显示出来（相当于将module的launchActivity合并到壳app的manifest中）
}
