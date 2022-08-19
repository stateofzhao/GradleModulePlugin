package com.zfun.funmodule.util;

import com.zfun.funmodule.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zfun on 2021/12/13 4:13 PM
 */
public class StringUtils {

    public static boolean isEmpty(String str) {
        return null == str || str.trim().length() == 0;
    }

    public static boolean isEquals(String s1, String s2) {
        if (null == s1) {
            return null == s2;
        }
        return s1.equals(s2);
    }

    /**
     * @param regex              正则表达式
     * @param regexLineStartWord 在正则表达式匹配到字符串中，根据此值来再次匹配到行
     * @param oriText            要匹配的字符串
     * @param replaceText        将匹配到的字符串替换为此字符串，如果为空则会注释掉匹配到的行
     */
    public static String editGradleText(final String regex, final String regexLineStartWord, final String oriText, String replaceText) {
        final Matcher matcher = Pattern.compile(regex).matcher(oriText);
        final boolean isFind = matcher.find();
        if (isFind) {
            final int matchedStartIndex = matcher.end();
            final String preTemp = oriText.substring(0, matchedStartIndex);
            final String regexText = oriText.substring(matcher.start(), matcher.end());
            final String regexLine = preTemp.substring(preTemp.lastIndexOf(regexLineStartWord));
            final String last = oriText.substring(matchedStartIndex);
            final String pre = preTemp.substring(0, preTemp.lastIndexOf(regexLineStartWord));
            final String result;
            if (isEmpty(replaceText)) {
                result = pre + "//" + regexLine + Constants.sComments + "：注释掉此行" + last;
            } else {
                result = pre + replaceText + last;
            }
            LogMe.D("==editGradleText regexText：== " + regexText);
            LogMe.D("==editGradleText pre：== " + pre);
            LogMe.D("==editGradleText regexLine：== " + regexLine);
            LogMe.D("==editGradleText last：== " + last);
            return result;
        }
        return oriText;
    }
}
