package com.llew.bytecode.fix.utils

import java.text.SimpleDateFormat

/**
 * <br/><br/>
 *
 * @author llew
 * @date 2017/11/16
 */

public class Logger {

    private static final int SPACE = 50
    public static boolean enable = false

    public static void e(String log) {
        if (enable && !TextUtil.isEmpty(log)) {
            System.err.println("【Tag = BytecodeFix || Time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()) + " || Message = " + log + "】")
        }
    }

    private static String generateSpace(String log) {
        StringBuffer sb = new StringBuffer()
        def length = SPACE
        if (!TextUtil.isEmpty(log)) {
            length = SPACE - log.length()
        }
        for (int i = 0; i < length; i ++) {
            sb.append(" ")
        }
        return sb.toString()
    }

}
