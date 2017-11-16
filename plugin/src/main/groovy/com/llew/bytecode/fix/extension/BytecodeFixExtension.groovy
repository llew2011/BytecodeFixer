package com.llew.bytecode.fix.extension
/**
 * 字节码处理配置项
 * <p>
 * <br/><br/>
 *
 * @author llew
 * @date 2017/11/16
 */

public class BytecodeFixExtension {

    /**
     * 字节码修复器是否可用，默认可用
     */
    boolean enable = true

    /**
     * 是否开启日志功能，默认开启
     */
    boolean logEnable = true

    /**
     * 是否保留修复过的jar文件，默认保留
     */
    boolean keepFixedJarFile = true

    /**
     * 时候保留修复过的class文件，默认保留
     */
    boolean keepFixedClassFile = true

    /**
     * 构建字节码所依赖的第三方包绝对路径，默认包含了Android.jar文件
     */
    ArrayList<String> dependencies = new ArrayList<String>()

    /**
     * 配置文件集合，配置格式：className##methodName(param1,param2...paramN)##injectValue##injectLine
     */
    ArrayList<String> fixConfig = new ArrayList<>();

    boolean getEnable() {
        return enable
    }

    void setEnable(boolean enable) {
        this.enable = enable
    }

    boolean getLogEnable() {
        return logEnable
    }

    void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable
    }

    boolean getKeepFixedJarFile() {
        return keepFixedJarFile
    }

    void setKeepFixedJarFile(boolean keepFixedJarFile) {
        this.keepFixedJarFile = keepFixedJarFile
    }

    boolean getKeepFixedClassFile() {
        return keepFixedClassFile
    }

    void setKeepFixedClassFile(boolean keepFixedClassFile) {
        this.keepFixedClassFile = keepFixedClassFile
    }

    ArrayList<String> getDependencies() {
        return dependencies
    }

    void setDependencies(ArrayList<String> dependencies) {
        this.dependencies = dependencies
    }

    ArrayList<String> getFixConfig() {
        return fixConfig
    }

    void setFixConfig(ArrayList<String> fixConfig) {
        this.fixConfig = fixConfig
    }

}
