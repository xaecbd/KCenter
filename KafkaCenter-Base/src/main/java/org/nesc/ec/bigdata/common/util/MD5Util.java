package org.nesc.ec.bigdata.common.util;

import org.nesc.ec.bigdata.common.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

public class MD5Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(MD5Util.class);
    /***
     * MD5加密
     * 使用JDK自带 MessageDigest
     * @return String
     */
    public static String string2MD5(String inStr){
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance(Constant.ENCRYPTION.MD5);
        }catch (Exception e){
            LOGGER.warn("", e);
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16) {
                hexValue.append(Constant.NUM.ZERO);
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static boolean passwordIsTrue(String inputPassword,String md5DB) {
        String md5 = string2MD5(inputPassword);
        return md5DB.equals(md5);
    }
}
