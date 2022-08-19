# GradleModulePlugin
根据扩展参数生成Process，根据Gradle和Project的生命周期来执行操作。
目前支持的功能：
- 模块管理
- `build.gradle`代码注入
- 多渠道打包

## 引入到项目
在根工程的`build.gradle`中添加：

```java
buildscript {
    repositories {
        maven { 
            url 'https://s01.oss.sonatype.org/content/repositories/snapshots' 
        }
        dependencies {
            classpath 'io.github.stateofzhao:GradleMoudlePlugin:1.1.7-SNAPSHOT'
        }
    }
}
apply plugin: 'com.zfei.funmodule'//必须声明到根工程的build.gradle中

funBuildType{
     debug = true //true可以看到本插件的打印日志，false不可看到
}
```

## 模块管理
进行组件化开发时，在开发各个组件时可以完全按照 `com.android.application`的方式来进行，并且可以按照`com.android.library`方式来被其他组件引用。
本插件能够自动将各个组件切换为 `com.android.library` 模式。
在根工程的`build.gradle`文件中配置：
```java
funAppLib {
    packageProjectName = 'app'	//主（壳）工程的module名称
    runType = 'app'	//有两种类型：'app'、'module'
    libName = ["modulea","moduleb"] //作为lib的module名称；可选，如果不声明，则取项目所有子工程
}
```
**对runType的两种类型重点解释下：**

```
//app: 只有壳app可以安装运行，所有 lib 的 Manifest 中的 launchActivity 会在build时去掉launch属性，build完成后会重新还原此 Manifest。
//module: 各个 lib 可以独立运行，壳app不能运行。
```

## `build.gradle`代码注入
向指定子项目的build.gradle文件中自动注入代码
```java
funInject{
     injectCode = ["modulea":"xxxx.txt"] //xxxx.txt：相对于根工程的全路径
     //或者使用下面配置，来给 funAppLib{libName} 指定的所有子工程的build.gradle注入代码
     //injectCodePath = 'xxx.txt'//xxxx.txt：相对于根工程的全路径
     //excludeProjectName = ['xxxa','xxxb'] //排除注入代码的工程名   
}
```

## 多渠道打包
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
`zfun_meituan_test.apk`
`zfun_360mark_test.apk`

在项目中获取到的渠道信息分别为:
`meituan`
`360mark`
