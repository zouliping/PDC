package utils;

import java.util.Random;

import models.Dev;
import models.Users;
import cn.jpush.api.JPushClient;
import cn.jpush.api.MessageResult;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class UserUtil {

	public final static String userClassname = "User";
	public final static String userLabel = "uid";
	public final static String masterSecret = "eab9c1c35310e34b4973ab89";
	public final static String appKey = "c8a93696166e0d270c045407";
	public final static String sid = "portal";

	/**
	 * send notification to pdc for android
	 * 
	 * @param old_location
	 * @param new_location
	 * @param uid
	 */
	public static void sendNotification(String old_location,
			String new_location, String uid) {
		JPushClient client = new JPushClient(masterSecret, appKey);
		MessageResult result = client.sendNotificationWithAlias(getSendNo(),
				uid, "PDC", "I moved from " + old_location + " to "
						+ new_location + ".");
		System.out.println("send location change. result:"
				+ result.getErrcode());
	}

	/**
	 * get random number
	 * 
	 * @return
	 */
	public static int getSendNo() {
		Random random = new Random();
		return Math.abs(random.nextInt());
	}

	/**
	 * send data change to proxy
	 * 
	 * @param title
	 * @param data
	 * @param uid
	 */
	public static void sendNotificationToU(String title, ObjectNode data,
			String uid) {
		JPushClient client = new JPushClient("5bb053079480cab0cebba021",
				"cfce04873e352b0af05d7bc9");
		MessageResult result = client.sendNotificationWithAlias(getSendNo(),
				uid, title, data.toString());
		System.out.println("send data change. result:" + result.getErrcode()
				+ "");
	}

	/**
	 * confirm user
	 * 
	 * @param token
	 * @return
	 */
	public static Boolean confirmUser(String token) {
		if (Users.find.where().eq("token", token).findUnique() != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * confirm dev
	 * 
	 * @param token
	 * @return
	 */
	public static Boolean confirmDev(String token) {
		if (Dev.find.where().eq("dtoken", token).findUnique() != null) {
			return true;
		} else {
			return false;
		}
	}
}
