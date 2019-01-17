package com.llew.bytecode.fix.bean

import com.llew.bytecode.fix.utils.Logger
import com.llew.bytecode.fix.utils.TextUtil

import static com.llew.bytecode.fix.injector.BytecodeFixInjector.CLASS
import static com.llew.bytecode.fix.injector.BytecodeFixInjector.JAVA
/**
 * <br/><br/>
 *
 * @author llew
 * @date 2019/1/15
 */

public class BytecodeFixConfig {

    private Collection<ClassInfo> classInfoList


    Collection<ClassInfo> getClassInfoList() {
        return classInfoList
    }

    void setClassInfoList(Collection<ClassInfo> list) {
        this.classInfoList = list
    }

    public static class ClassInfo {

        private String            className
        private List<MethodInfo>  methodInfoList

        void setClassName(String className) {
            this.className = className
        }

        void setMethodInfoList(List<MethodInfo> list) {
            this.methodInfoList = list
        }

        String getClassName() {
            return className
        }

        List<MethodInfo> getMethodInfoList() {
            return methodInfoList
        }

        @Override
        String toString() {
            StringBuilder sb = new StringBuilder()
            sb.append(className)
            if (null != methodInfoList && !methodInfoList.isEmpty()) {
                methodInfoList.each {
                    sb.append(it.toString()).append("\n")
                }
            }
            return sb.toString()
        }
    }

    public static class MethodInfo {

        private String          methodName
        private List<String>    methodArgs
        private String          injectValue
        private int             injectLine

        String getMethodName() {
            return methodName
        }

        List<String> getMethodArgs() {
            return methodArgs
        }

        int getInjectLine() {
            return injectLine
        }

        String getInjectValue() {
            return injectValue
        }

        @Override
        String toString() {
            StringBuilder sb = new StringBuilder()
            sb.append(methodName).append("(")
            if (null != methodArgs && methodArgs.size() > 0) {
                methodArgs.each {
                    sb.append(it).append(",")
                }
                sb = sb.deleteCharAt(sb.length() - 1)
            }
            sb.append(")").append(injectValue).append("##").append(injectLine)
            return sb.toString()
        }
    }

    /**
     * parse fixConfig<br/>
     *
     * com.tencent.av.sdk.NetworkHelp##getAPInfo(android.content.Context)##if(Boolean.TRUE.booleanValue()){$1 = null;System.out.println("i have hooked this method !!!");}##0
     */
    public static BytecodeFixConfig parse(List<String> fixConfigs) {
        BytecodeFixConfig bytecodeFixConfig = new BytecodeFixConfig()
        Map<String, ClassInfo> classInfoMap = new HashMap<>()
        if (null != fixConfigs) {
            fixConfigs.each { config ->
                if (!TextUtil.isEmpty(config.trim())) {
                    def configs = config.trim().split("##")
                    if (null != configs && configs.length > 0) {
                        if (configs.length < 3) {
                            throw new IllegalArgumentException("fixConfig invalid !!!")
                        }

                        def _className   = configs[0].trim()
                        def _methodName  = configs[1].trim()
                        def _injectValue = configs[2].trim()
                        def _injectLine  = 0
                        if (4 == configs.length) {
                            try {
                                _injectLine  = Integer.parseInt(configs[3])
                            } catch (Exception e) {
                                throw new IllegalArgumentException("inject line invalid !!!", e)
                            }
                        }

                        if (TextUtil.isEmpty(_className)) {
                            throw new IllegalArgumentException("className invalid !!!")
                        }

                        if (TextUtil.isEmpty(_methodName)) {
                            throw new IllegalArgumentException("methodName invalid !!!")
                        }

                        if (TextUtil.isEmpty(_injectValue)) {
                            throw new IllegalArgumentException("inject value invalid !!!")
                        }

                        def methodParams = new ArrayList<String>()

                        if (_methodName.contains("(") && _methodName.contains(")")) {
                            def tempMethodName = _methodName
                            _methodName = tempMethodName.substring(0, tempMethodName.indexOf("(")).trim()
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

                        if (_className.endsWith(JAVA)) {
                            _className = _className.substring(0, _className.length() - JAVA.length()) + CLASS
                        }

                        if (!_className.endsWith(CLASS)) {
                            _className += CLASS
                        }

                        ClassInfo cachedClassInfo = classInfoMap.get(_className)
                        if (null == cachedClassInfo) {
                            cachedClassInfo = new ClassInfo()
                            cachedClassInfo.className = _className
                            classInfoMap.put(_className, cachedClassInfo)
                        }

                        List<MethodInfo> cachedMethodInfoList = cachedClassInfo.methodInfoList
                        if (null == cachedMethodInfoList) {
                            cachedMethodInfoList = new ArrayList<>()
                            cachedClassInfo.methodInfoList = cachedMethodInfoList
                        }

                        MethodInfo newMethodInfo = new MethodInfo()
                        newMethodInfo.methodName  = _methodName
                        newMethodInfo.methodArgs  = methodParams
                        newMethodInfo.injectValue = _injectValue
                        newMethodInfo.injectLine  = _injectLine

                        cachedMethodInfoList.add(newMethodInfo)
                    }
                } else {
                    Logger.e("fix config item is null or empty !!!")
                }
            }
        }
        bytecodeFixConfig.setClassInfoList(classInfoMap.values())
        return bytecodeFixConfig
    }
}
