package com.llew.bytecode.fix.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.llew.bytecode.fix.extension.BytecodeFixExtension
import com.llew.bytecode.fix.injector.BytecodeFixInjector
import com.llew.bytecode.fix.utils.Logger
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

/**
 * 字节码类处理器
 * <br/><br/>
 *
 * @author llew
 * @date 2017/11/16
 */

public class BytecodeFixTransform extends Transform {

    private static final String DEFAULT_NAME = "BytecodeFixTransform"

    private BytecodeFixExtension mExtension;

    BytecodeFixTransform(Project project, String versionName, BytecodeFixExtension extension) {
        this.mExtension = extension
        Logger.enable = extension.logEnable
        BytecodeFixInjector.init(project, versionName, mExtension)
    }

    @Override
    public String getName() {
        return DEFAULT_NAME
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    public boolean isIncremental() {
        return false
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        if (null == transformInvocation) {
            throw new IllegalArgumentException("transformInvocation is null !!!")
        }
        Collection<TransformInput> inputs = transformInvocation.inputs
        if (null == inputs) {
            throw new IllegalArgumentException("TransformInput is null !!!")
        }

        TransformOutputProvider outputProvider = transformInvocation.outputProvider;

        if (null == outputProvider) {
            throw new IllegalArgumentException("TransformInput is null !!!")
        }

        inputs.each {
            it.directoryInputs.each { dirInput ->
                appendClassPath(dirInput.file)
            }
            it.jarInputs.each { jarInput ->
                appendClassPath(jarInput.file)
            }
        }

        for (TransformInput input : inputs) {

            if (null == input) continue;

            for (DirectoryInput directoryInput : input.directoryInputs) {

                if (directoryInput) {

                    if (null != directoryInput.file && directoryInput.file.exists()) {

                        // ClassInjector.injector.inject(directoryInput.file.absolutePath, mPackageName.replaceAll("\\.", File.separator));

                        File dest = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                        FileUtils.copyDirectory(directoryInput.file, dest);
                    }
                }
            }

            for (JarInput jarInput : input.jarInputs) {
                if (jarInput) {
                    if (jarInput.file && jarInput.file.exists()) {
                        String jarName = jarInput.name;
                        String md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath);

                        if (jarName.endsWith(".jar")) {
                            jarName = jarName.substring(0, jarName.length() - 4);
                        }

                        // 在这里jar文件进行动态修复
                        File injectedJarFile = null
                        if (null != mExtension && mExtension.enable) {
                            injectedJarFile = BytecodeFixInjector.injector.inject(jarInput.file)
                        }

                        File dest = outputProvider.getContentLocation(DigestUtils.md5Hex(jarName + md5Name), jarInput.contentTypes, jarInput.scopes, Format.JAR);

                        if (dest) {
                            if (dest.parentFile) {
                                if (!dest.parentFile.exists()) {
                                    dest.parentFile.mkdirs();
                                }
                            }

                            if (!dest.exists()) {
                                dest.createNewFile();
                            }

                            if (null != injectedJarFile && injectedJarFile.exists()) {
                                FileUtils.copyFile(injectedJarFile, dest)
                                Logger.e(jarInput.file.name + " has successful hooked !!!")
                                if (null != mExtension && !mExtension.keepFixedJarFile) {
                                    injectedJarFile.delete()
                                }
                            } else {
                                FileUtils.copyFile(jarInput.file, dest)
                            }
                        }
                    }
                }
            }
        }
    }

    private void appendClassPath(File file) {
        if (null != mExtension && mExtension.enable) {
            BytecodeFixInjector.injector.appendClassPath(file)
        }
    }
}
