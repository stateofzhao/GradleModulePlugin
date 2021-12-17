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
暂时只支持V1签名。
采用zip文件添加commit信息的形式。
在根工程的`build.gradle`文件中配置：
```java
multiChannel{
    //buildType = 1   
    prefix = "zfun_" //多渠道包前缀
    subfix = "_test" //多渠道包后缀
    channelConfig{
        Full{
            childFlavors = ["huawei","360"] //多渠道描述
        }
    }
}
```
采用上述配置后，会有两个多渠道包，最终包的文件名为：
zfun_huawei_test.apk
zfun_360_test.apk
在项目中获取到的渠道信息分别为:
huawei
360

项目中获取渠道信息代码从下述文件中拿取：
src/main/java/com.zfun.funmodule/util/ZipUtil.java
