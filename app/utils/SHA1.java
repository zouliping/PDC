package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {

	private static final String SHA1_STR = "SHA-1";
	private static char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static MessageDigest md = null;

	/**
	 * Given a byte, return its representation as a 2-char hex string.
	 * 
	 * @param b
	 * @return
	 */
	public static String byteToHexString(byte b) {
		// 8 bits. To get the low 4 bits, AND with 1111 (15)
		int low = ((b & 15) + hexChars.length) % hexChars.length;
		// To get the high 4 bits, shift right 4 bits.
		int high = ((b >> 4) + hexChars.length) % hexChars.length;
		return new String(new char[] { hexChars[high], hexChars[low] });
	}

	public static String byteArrayToHexString(byte[] byteArray) {
		StringBuilder sb = new StringBuilder();
		for (byte b : byteArray) {
			sb.append(byteToHexString(b));
		}
		return sb.toString();
	}

	public static synchronized byte[] getSHA1(String s) {
		try {
			md = MessageDigest.getInstance(SHA1_STR);
		} catch (NoSuchAlgorithmException ne) {
			throw new RuntimeException(ne);
		}
		byte byteArray[] = s.getBytes();
		byte digest[];

		md.update(byteArray);
		digest = md.digest();
		return digest;
	}

	public static synchronized String getSHA1String(String s) {
		byte digest[] = getSHA1(s);
		return byteArrayToHexString(digest);
	}
}
