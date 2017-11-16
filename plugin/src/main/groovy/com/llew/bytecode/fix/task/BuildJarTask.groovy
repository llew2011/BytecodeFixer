package com.llew.bytecode.fix.task

import org.gradle.api.tasks.bundling.Jar;

/**
 * 构建Jar包的任务器
 * <br/><br/>
 *
 * @author llew
 * @date 2017/11/16
 */

public class BuildJarTask extends Jar {

    BuildJarTask() {
        group = "BytecodeFixBuildJarTask"
    }
}
