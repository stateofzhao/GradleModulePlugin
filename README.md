# GradleMoudlePlugin
模块化开发自动管理插件

## 模块管理
在根工程的`build.gradle`中添加：

```java
buildscript {
    repositories {
        maven {
            url uri('./../../repo')
        }
        jcenter()
        google()
        dependencies {
            classpath 'com.zfun.funmodule:GradleMoudlePlugin:1.0.0'
        }
    }
}
apply plugin: 'com.zfun.funmodule'
  
funAppLib {
    mainAppName = "app"	//主（壳）工程的module名称
    runType = 1	//有三种类型
    buildType = 1 //releases时传递2，debug时传递1
    libName = ["modulea","moduleb"] //作为lib的module名称，注意如果没有界面那么可以不用在这里声明
}

funInject{
    injectCode = ["modulea":"gradle_code.txt"]//module名:要注入的gradle代码文件路径（相对项目根目录的路径）
}
```

**对runType的三种类型重点解释下：**

```java
//1 只有壳app可以安装运行，所有 lib 的 Manifest 中的 launchActivity 会在build时去掉launch属性，build完成后会重新还原此 Manifest。
//2 运行壳app时，不过滤 lib 的 Manifest 中的 launchActivity ，所有 lib 中的 launchActivity 都会显示到桌面上（Android默认的Manifest合并模式）。
//3 各个 lib 可以独立运行，壳app不能运行。
```

## 多渠道打包
V1签名打包采用的是 360的方案，原项目地址：https://github.com/seven456/MultiChannelPackageTool
V2签名打包采用的是 美团的方案，原项目地址：https://github.com/Meituan-Dianping/walle

在根工程的`build.gradle`文件中配置：
```java
multiChannel{
    //buildType = 1   
    prefix = "zfun_" //多渠道包前缀
    subfix = "_test" //多渠道包后缀
    channelConfig{
        Full{
            childFlavors = ["meituan","360mark"] //多渠道描述
            checkChannel = false //打包完成后是否读取下最终apk中的渠道信息和写入的渠道信息相等
            //以下配置只有V2签名才生效
            lowMemory = false //
            extraInfo = ["key1":"value1","key2":"value2"] //除渠道外的额外信息
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