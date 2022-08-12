在根工程的`build.gradle`中添加：

```java
buildscript {
    repositories {
        maven { 
            url 'https://s01.oss.sonatype.org/content/repositories/snapshots' 
        }
        dependencies {
            classpath 'io.github.stateofzhao:GradleMoudlePlugin:1.1.6-SNAPSHOT'
        }
    }
}
apply plugin: 'com.zfei.funmodule'//必须声明到根工程的build.gradle中

funAppLib {
    mainAppName = 'app'	//主（壳）工程的module名称
    runType = 'app'	//有两种类型：'app'、'module'
    libName = ["modulea","moduleb"] //作为lib的module名称；可选，如果不声明，则取项目所有子工程
}
funBuildType{
    debug = true //true可以看到本插件的打印日志，false不可看到
}
funInject{
     injectCode = ["modulea":"gradle_code.txt"]
}
```

**对runType的两种类型重点解释下：**

```
//app: 只有壳app可以安装运行，所有 lib 的 Manifest 中的 launchActivity 会在build时去掉launch属性，build完成后会重新还原此 Manifest。
//module: 各个 lib 可以独立运行，壳app不能运行。
```

