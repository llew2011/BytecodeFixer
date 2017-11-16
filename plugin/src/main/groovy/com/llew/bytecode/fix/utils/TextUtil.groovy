package com.llew.bytecode.fix.utils

/**
 * 文本辅助类
 * <br/><br/>
 *
 * @author llew
 * @date 2017/11/16
 */

public class TextUtil {

    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0) {
            return true
        } else {
            return false
        }
    }

}