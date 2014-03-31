package utils;

import java.util.Random;

import cn.jpush.api.JPushClient;
import cn.jpush.api.MessageResult;

public class UserUtil {

	public final static String userClassname = "User";
	public final static String userLabel = "uid";
	public final static String masterSecret = "eab9c1c35310e34b4973ab89";
	public final static String appKey = "c8a93696166e0d270c045407";

	public static Boolean sendNotification(String old_location,
			String new_location, String uid) {
		JPushClient client = new JPushClient(masterSecret, appKey);
		System.out.println("no--" + getSendNo() + "---" + uid + "");
		MessageResult result = client.sendNotificationWithAlias(getSendNo(),
				uid, "PDC", "I moved from " + old_location + " to "
						+ new_location + ".");
		System.out.println(result.getErrcode() + "");
		if (result.getErrcode() == 0) {
			return true;
		} else
			return false;
	}

	public static int getSendNo() {
		Random random = new Random();
		return Math.abs(random.nextInt());
	}
}
