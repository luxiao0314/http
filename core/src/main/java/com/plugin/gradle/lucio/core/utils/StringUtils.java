package com.plugin.gradle.lucio.core.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Description
 * @Author luxiao
 * @Date 2019-07-23 11:40
 * @Version
 */
public class StringUtils {
    /**
     * MD5 encrypt做过处理，取的是中间16位。
     */
    public static String md5(String str) {
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(str.getBytes(Charset.defaultCharset()));
            byte[] arrayOfByte = localMessageDigest.digest();
            StringBuilder stringBuffer = new StringBuilder();
            for (byte anArrayOfByte : arrayOfByte) {
                int j = 0xFF & anArrayOfByte;
                if (j < 16)
                    stringBuffer.append("0");
                stringBuffer.append(Integer.toHexString(j));
            }
            return stringBuffer.toString().toLowerCase().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
