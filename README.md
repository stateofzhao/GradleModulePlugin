# GradleMoudlePlugin
根据扩展参数生成Process，根据Gradle和Project的生命周期来执行操作。
使用，在根build.gradle中apply插件：
```java
buildscript {
    repositories {
        maven {
            url uri('./../../repo')
        }
        dependencies {
            classpath 'com.zfun.funmodule:GradleMoudlePlugin:1.0.0'
        }
    }
}
apply plugin: 'com.zfun.funmodule'
```

## 模块管理Process
Android项目模块化开发时，自动管理子模块在 com.android.library 与 com.android.application 间切换。

在根工程的`build.gradle`中添加：

```java
//
funAppLib {
    mainAppName = "app"	//主（壳）工程的module名称
    runType = 1	//有两种类型
    libName = ["modulea","moduleb"] //作为lib的module名称，注意如果没有界面那么可以不用在这里声明
}
```
**对runType的两种类型重点解释下：**

```java
//1 只有壳app可以安装运行，所有 lib 的 Manifest 中的 launchActivity 会在build时去掉launch属性，build完成后会重新还原此 Manifest。
//2 各个 lib 可以独立运行，壳app不能运行。
```

## 多渠道打包Process
V1签名打包采用的是 360的方案，原项目地址：https://github.com/seven456/MultiChannelPackageTool
V2签名打包采用的是 美团的方案，原项目地址：https://github.com/Meituan-Dianping/walle

在主工程的`build.gradle`文件中配置：
```java
multiChannel{
    prefix = "zfun_" //多渠道包前缀
    subfix = "_test" //多渠道包后缀
    channelConfig{
        Full{ //对应于android的productFlavors
            childFlavors = ["meituan","360mark"] //多渠道的渠道信息
            checkChannel = true //打包完成后是否读取下最终apk中的渠道信息和写入的渠道信息相等
            //以下配置只有V2签名才生效
            lowMemory = false //
            extraInfo = ["key1":"value1","key2":"value2"] //除渠道信息外的额外信息
        }
    }
}
```
采用上述配置后，会有两个多渠道包，最终包的文件名为：
zfun_meituan_test.apk
zfun_360mark_test.apk
在项目中获取到的渠道信息分别为:
meituan
360mark
```
