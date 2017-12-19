### BytecodeFixer简介
> BytecodeFixer 是基于[Javassist](http://jboss-javassist.github.io/javassist/)开发的一款轻量级的字节码修复插件，它可以有效的修复第三方Jar包中出现的一些bug，例如：NullPointerException，NumberFormatException，IndexOutOfBoundsException等。它利用[Gradle](http://tools.android.com/tech-docs/new-build-system/transform-api)1.5.0版本后的[Transform API](http://google.github.io/android-gradle-dsl/javadoc/) 在项目打包时动态的对class文件进行修复。

> **提示：**BytecodeFixer插件不会增加APP的方法数

### BytecodeFixer使用
- **引入插件** ：在`根项目`的build.gradle添加如下配置：
```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.3'
        // 添加如下依赖
        classpath 'com.llew.bytecode.fix.gradle:BytecodeFixer:1.0.6'
    }
}
```
- **使用插件** ：在`主项目`的build.gradle`末尾`添加如下配置：
```gradle
apply plugin: 'com.llew.bytecode.fix'
bytecodeFixConfig {
    enable = true
    logEnable = true
    keepFixedJarFile = true
    keepFixedClassFile = true
    dependencies = [
	    'jar1 absolutePath',
	    'jar2 absolutePath'
	    ]
    fixConfig = [
           'className##methodName#injectValue##injectLine'
           ]
}
```
- **配置说明**
 - `enable`  true | false
  -- BytecodeFixer插件是否可用
 - `logEnable`  true | false
 -- 是否允许打印日志
 - `keepFixedJarFile` true | false
 -- 是否保存修复过的Jar文件
 - `keepFixedClassFile` true | false
 -- 是否保存修复过的class文件
 - `dependencies`
 -- 依赖的第三方Jar文件或class文件的绝对路径
 - `fixConfig`注入配置，格式为A##B##C##D
    -- A：表示待修复的类名，例如：com.tencent.av.sdk.NetworkHelp
    -- B：表示待修复的方法名，例如：getAPInfo(android.content.Context)
    -- C：表示修复内容，例如：$1 = null;System.out.println("I have hooked this method by BytecodeFixer plugin !!!");
    -- D：表示把修复内容插入在方法的哪一行，`D > 0` 表示插在具体的行数，`D == 0`表示插在方法的最开始处，`D < 0`表示替换方法的全部内容
- **使用案例：**
```gradle
apply plugin: 'com.llew.bytecode.fix'
bytecodeFixConfig {
    logEnable = true
    keepFixedJarFile = true
    keepFixedClassFile = true
    dependencies = ['/Users/llew/Desktop/Android/Android_SDK_Eclipse/extras/android/support/v4/android-support-v4.jar']
    fixConfig = [
            'com.tencent.av.sdk.NetworkHelp##getAPInfo(android.content.Context)##$1 = null;System.out.println("I have hooked this method by BytecodeFixer plugin !!!");##0',
            'com.tencent.av.sdk.NetworkHelp##getMobileAPInfo(android.content.Context,int)##if(Boolean.TRUE.booleanValue()){$1 = null;System.out.println("i have hooked this method by BytecodeFixer !!!");}return new com.tencent.av.sdk.NetworkHelp.APInfo();##-1',
            'com.tencent.av.camera.CameraCaptureSettings##initSettings()##{}##0'
    ]
}
```
### BytecodeFix博客
- **博客地址：**[http://blog.csdn.net/llew2011/article/details/78540911](http://blog.csdn.net/llew2011/article/details/78540911)，我在博客中讲解了该插件的一些细节