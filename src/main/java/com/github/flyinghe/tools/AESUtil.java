package com.github.flyinghe.tools;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 本类用于AES加密和解密
 *
 * @author Flying
 */
public class AESUtil {
    public static final Integer SHORT = 128;
    public static final Integer MIDDLE = 192;
    public static final Integer LONG = 256;

    /**
     * 获取指定keysize的AES密钥,通过AESUtils类的静态常量指定
     *
     * @return 返回密钥的字节数组, 失败返回null
     */
    public static byte[] getKey(Integer length) {
        if (!AESUtil.SHORT.equals(length) && !AESUtil.MIDDLE.equals(length) && !AESUtil.LONG.equals(length)) {
            throw new RuntimeException("指定的KeySize不正确");
        }
        SecretKey key = null;
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(length);
            key = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return key.getEncoded();
    }

    /**
     * 将一个字符串按指定key进行AES加密并返回字节数组
     *
     * @param str 需要加密的字符串
     * @param key 指定key
     * @return 返回字符串被加密后的字节数组，失败返回null
     */
    public static byte[] encypt(String str, byte[] key) {
        byte[] b = null;
        try {
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            b = cipher.doFinal(str.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return b;
    }

    /**
     * 将一个字符串按指定key进行AES解密并返回
     *
     * @param b   需要解密的字符串
     * @param key 指定key
     * @return 返回解密后的字符串，失败返回null
     */
    public static String decypt(byte[] b, byte[] key) {
        String _str = null;
        try {
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            _str = new String(cipher.doFinal(b));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return _str;
    }
}
