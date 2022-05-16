在根工程的`build.gradle`中添加：

```java
buildscript {
    repositories {
        maven {
            url uri('./../../repo')
        }
        dependencies {
            classpath 'com.zfei.funmodule:GradleMoudlePlugin:0.0.4'
        }
    }
}
apply plugin: 'com.zfei.funmodule'//必须声明到根工程的build.gradle中

funAppLib {
    mainAppName = "app"	//主（壳）工程的module名称
    runType = 1	//有两种类型
    buildType = 1 //releases时传递2，debug时传递1
    libName = ["modulea","moduleb"] //作为lib的module名称，注意如果没有界面那么可以不用在这里声明
}
funInject{
     injectCode = ["modulea":"gradle_code.txt"]
}
```

**对runType的两种类型重点解释下：**

```
//1 只有壳app可以安装运行，所有 lib 的 Manifest 中的 launchActivity 会在build时去掉launch属性，build完成后会重新还原此 Manifest。
//2 各个 lib 可以独立运行，壳app不能运行。
```

