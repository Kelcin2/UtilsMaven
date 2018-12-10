package com.github.flyinghe.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 本类用于数据的加密或者解密常用方法
 * 
 * @author Flying
 * 
 */
public class PasswordUtils {

	/**
	 * 获取经过MD5加密后的字符串，该加密算法不可逆
	 * 
	 * @param password
	 *            需要加密的密码
	 * @return 返回加密后的字符串,失败返回null
	 */
	public static String getMD5Digest(String password) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		md.update(password.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i : digest) {
			if (i < 0) {
				i += 256;
			}
			if (i < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(i));
		}
		return sb.toString();
	}

	/**
	 * 将一个字节数组转化成十六进制字符串并返回
	 * 
	 * @param b
	 *            字节数组
	 * @return 返回一个由十六进制字符组成的字符串
	 */
	public static String bytesToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		String hex;
		for (int i = 0; i < b.length; i++) {
			hex = Integer.toHexString(b[i] & 0xff);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

}
