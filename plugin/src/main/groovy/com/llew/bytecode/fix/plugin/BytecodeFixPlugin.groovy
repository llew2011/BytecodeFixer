package com.llew.bytecode.fix.plugin

import com.android.build.gradle.AppExtension
import com.llew.bytecode.fix.extension.BytecodeFixExtension
import com.llew.bytecode.fix.transform.BytecodeFixTransform
import com.llew.bytecode.fix.utils.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * 字节码修复核心插件
 * <br/><br/>
 *
 * @author llew
 * @date 2017/11/16
 */
public class BytecodeFixPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def android = project.extensions.findByType(AppExtension.class)
        def versionName = android.defaultConfig.versionName

        project.extensions.create("bytecodeFixConfig", BytecodeFixExtension)
        BytecodeFixExtension extension = project.bytecodeFixConfig

        Logger.enable = extension.logEnable
        android.registerTransform(new BytecodeFixTransform(project, versionName, extension))
    }
}
