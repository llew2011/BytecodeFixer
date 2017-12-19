package com.llew.bytecode.fix.injector

import com.llew.bytecode.fix.extension.BytecodeFixExtension
import com.llew.bytecode.fix.utils.FileUtils
import com.llew.bytecode.fix.utils.Logger
import com.llew.bytecode.fix.utils.TextUtil
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

import java.util.jar.JarFile
import java.util.zip.ZipFile
/**
 * 字节码注入器
 * <p>
 * <br/><br/>
 *
 * @author llew
 * @date 2017/11/16
 */

public class BytecodeFixInjector {

    private static final String INJECTOR  = "injector"
    private static final String JAVA      = ".java"
    private static final String CLASS     = ".class"
    private static final String JAR       = ".jar"

    private static ClassPool sClassPool
    private static BytecodeFixInjector sInjector

    private Project mProject
    private String mVersionName
    private BytecodeFixExtension mExtension

    private BytecodeFixInjector(Project project, String versionName, BytecodeFixExtension extension) {
        this.mProject = project
        this.mVersionName = versionName
        this.mExtension = extension
        appendDefaultClassPath()
    }

    public static void init(Project project, String versionName, BytecodeFixExtension extension) {
        sClassPool = ClassPool.default
        sInjector = new BytecodeFixInjector(project, versionName, extension)
    }

    public static BytecodeFixInjector getInjector() {
        if (null == sInjector) {
            throw new IllegalAccessException("init() hasn't been called !!!")
        }
        return sInjector
    }

    public synchronized File inject(File jar) {
        File destFile = null

        if (null == mExtension) {
            Logger.e("can't find bytecodeFixConfig in your app build.gradle !!!")
            return destFile
        }

        if (null == jar) {
            Logger.e("jar File is null before injecting !!!")
            return destFile
        }

        if (!jar.exists()) {
            Logger.e(jar.name + " not exits !!!")
            return destFile
        }

        try {
            ZipFile zipFile = new ZipFile(jar)
            zipFile.close()
            zipFile = null
        } catch (Exception e) {
            Logger.e(jar.name + " not a valid jar file !!!")
            return destFile
        }

        def jarName = jar.name.substring(0, jar.name.length() - JAR.length())
        def baseDir = new StringBuilder().append(mProject.projectDir.absolutePath)
                .append(File.separator).append(INJECTOR)
                .append(File.separator).append(mVersionName)
                .append(File.separator).append(jarName).toString()

        File rootFile = new File(baseDir)
        FileUtils.clearFile(rootFile)
        rootFile.mkdirs()

        File unzipDir = new File(rootFile, "classes")
        File jarDir   = new File(rootFile, "jar")

        JarFile jarFile = new JarFile(jar)
        mExtension.fixConfig.each { config ->
            if (!TextUtil.isEmpty(config.trim())) {
                // com.tencent.av.sdk.NetworkHelp##getAPInfo(android.content.Context)##if(Boolean.TRUE.booleanValue()){$1 = null;System.out.println("i have hooked this method !!!");}##0
                def configs = config.trim().split("##")
                if (null != configs && configs.length > 0) {
                    if (configs.length < 3) {
                        throw new IllegalArgumentException("参数配置有问题")
                    }

                    def className   = configs[0].trim()
                    def methodName  = configs[1].trim()
                    def injectValue = configs[2].trim()
                    def injectLine  = 0
                    if (4 == configs.length) {
                        try {
                            injectLine  = Integer.parseInt(configs[3])
                        } catch (Exception e) {
                            throw new IllegalArgumentException("行数配置有问题")
                        }
                    }

                    if (TextUtil.isEmpty(className)) {
                        Logger.e("className invalid !!!")
                        return
                    }

                    if (TextUtil.isEmpty(methodName)) {
                        Logger.e("methodName invalid !!!")
                        return
                    }

                    if (TextUtil.isEmpty(injectValue)) {
                        Logger.e("inject value invalid !!!")
                        return
                    }

                    def methodParams = new ArrayList<String>()

                    if (methodName.contains("(") && methodName.contains(")")) {
                        def tempMethodName = methodName
                        methodName = tempMethodName.substring(0, tempMethodName.indexOf("(")).trim()
                        def params = tempMethodName.substring(tempMethodName.indexOf("(") + 1, tempMethodName.indexOf(")")).trim()
                        if (!TextUtil.isEmpty(params)) {
                            if (params.contains(",")) {
                                params = params.split(",")
                                if (null != params && params.length > 0) {
                                    params.each { p ->
                                        methodParams.add(p.trim())
                                    }
                                }
                            } else {
                                methodParams.add(params)
                            }
                        }
                    }

                    if (className.endsWith(JAVA)) {
                        className = className.substring(0, className.length() - JAVA.length()) + CLASS
                    }

                    if (!className.endsWith(CLASS)) {
                        className += CLASS
                    }

                    def contain = FileUtils.containsClass(jarFile, className)

                    if (contain) {
                        // 1、判断是否进行过解压缩操作
                        if (!FileUtils.hasFiles(unzipDir)) {
                            FileUtils.unzipJarFile(jarFile, unzipDir)
                        }

                        // 2、开始注入文件，需要注意的是，appendClassPath后边跟的根目录，没有后缀，className后完整类路径，也没有后缀
                        sClassPool.appendClassPath(unzipDir.absolutePath)

                        // 3、开始注入，去除.class后缀
                        if (className.endsWith(CLASS)) {
                            className = className.substring(0, className.length() - CLASS.length())
                        }

                        CtClass ctClass = sClassPool.getCtClass(className)

                        if (!ctClass.isInterface()) {
                            CtMethod ctMethod
                            if (methodParams.isEmpty()) {
                                ctMethod = ctClass.getDeclaredMethod(methodName)
                            } else {
                                CtClass[] params = new CtClass[methodParams.size()]
                                for (int i = 0; i < methodParams.size(); i++) {
                                    String param = methodParams.get(i)
                                    params[i] = sClassPool.getCtClass(param)
                                }
                                ctMethod = ctClass.getDeclaredMethod(methodName, params)
                            }

                            if ("{}".equals(injectValue)) {
                                CtClass exceptionType = sClassPool.get("java.lang.Throwable")
                                String returnValue = "{\$e.printStackTrace();return null;}"
                                CtClass returnType = ctMethod.getReturnType()
                                if (CtClass.booleanType == returnType) {
                                    returnValue = "{\$e.printStackTrace();return false;}"
                                } else if (CtClass.voidType == returnType) {
                                    returnValue = "{\$e.printStackTrace();return;}"
                                } else if (CtClass.byteType == returnType || CtClass.shortType == returnType || CtClass.charType == returnType || CtClass.intType == returnType || CtClass.floatType == returnType || CtClass.doubleType == returnType || CtClass.longType == returnType) {
                                    returnValue = "{\$e.printStackTrace();return 0;}"
                                } else {
                                    returnValue = "{\$e.printStackTrace();return null;}"
                                }
                                ctMethod.addCatch(returnValue, exceptionType)
                            } else {
                                if (injectLine > 0) {
                                    ctMethod.insertAt(injectLine, injectValue)
                                } else if (injectLine == 0) {
                                    ctMethod.insertBefore(injectValue)
                                } else {
                                    if (!injectValue.startsWith("{")) {
                                        injectValue = "{" + injectValue
                                    }
                                    if (!injectValue.endsWith("}")) {
                                        injectValue = injectValue + "}"
                                    }
                                    ctMethod.setBody(injectValue)
                                }
                            }

                            ctClass.writeFile(unzipDir.absolutePath)
                            ctClass.detach()
                        } else {
                            Logger.e(className + " is interface and can't inject code ！！！")
                        }
                    }
                }
            }
        }

        // 4、循环体结束，判断classes文件夹下是否有文件
        if (FileUtils.hasFiles(unzipDir)) {

            destFile = new File(jarDir, jar.name)
            FileUtils.clearFile(destFile)
            FileUtils.zipJarFile(unzipDir, destFile)

            if (null != mExtension && !mExtension.keepFixedClassFile) {
                FileUtils.clearFile(unzipDir)
            }
        } else {
            FileUtils.clearFile(rootFile)
        }

        jarFile.close()

        return destFile
    }

    private void appendDefaultClassPath() {
        if (null == mProject) return
        def androidJar = new StringBuffer().append(mProject.android.getSdkDirectory())
                .append(File.separator).append("platforms")
                .append(File.separator).append(mProject.android.compileSdkVersion)
                .append(File.separator).append("android.jar").toString()

        File file = new File(androidJar);
        if (!file.exists()) {
            androidJar = new StringBuffer().append(mProject.rootDir.absolutePath)
                    .append(File.separator).append("local.properties").toString()

            Properties properties = new Properties()
            properties.load(new File(androidJar).newDataInputStream())

            def sdkDir = properties.getProperty("sdk.dir")

            androidJar = new StringBuffer().append(sdkDir)
                    .append(File.separator).append("platforms")
                    .append(File.separator).append(mProject.android.compileSdkVersion)
                    .append(File.separator).append("android.jar").toString()

            file = new File(androidJar)
        }

        if (file.exists()) {
            sClassPool.appendClassPath(androidJar);
        } else {
            Logger.e("couldn't find android.jar file !!!")
        }
    }

    public void appendClassPath(File path) {
        if (null != path) {
            if (path.directory) {
                sClassPool.appendPathList(path.absolutePath)
            } else {
                sClassPool.appendClassPath(path.absolutePath)
            }
        }
    }
}
