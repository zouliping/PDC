package utils;

import java.util.ArrayList;

import db.MyDBManager;

public class PrivacyInterpreter {

	private String uid;
	private String uname;
	private String sid;
	private String classname;

	public PrivacyInterpreter() {
		super();
	}

	public PrivacyInterpreter(String uid, String uname, String sid,
			String classname) {
		super();
		this.uid = uid;
		this.uname = uname;
		this.sid = sid;
		this.classname = classname;
	}

	/**
	 * check whether is user himself
	 * 
	 * @return
	 */
	public Boolean isMe() {
		if (uid.equals(SHA1.getSHA1String(uname))) {
			return true;
		} else {
			return false;
		}
	}

	public ArrayList<String> checkRules() {
		ArrayList<String> list_public_pro;
		// first, get public pro
		MyDBManager manager = new MyDBManager();
		Boolean allpro = manager
				.isAllPro("SELECT allpro FROM rules where uid = \'" + uid
						+ "\' and classname = \'" + classname
						+ "\' and level = 0");

		if (allpro != null) {
			// user set public rules
			if (allpro) {
				return ModelUtil.getPropertyList(classname);
			} else {
				list_public_pro = manager
						.getProList("SELECT pro FROM rules where uid = \'"
								+ uid + "\' and classname = \'" + classname
								+ "\' and level = 0");
			}
		} else {
			list_public_pro = new ArrayList<String>();
		}

		if (isMe()) {
			// is user
			// to find out whether user sets the service privacy
			ArrayList<String> ridList = manager
					.getList("SELECT rid FROM rules WHERE uid = \'" + uid
							+ "\' and classname = \'" + classname
							+ "\' and level = 1");
			for (String rid : ridList) {
				if (manager.query("SELECT * FROM rules_services where rid = \'"
						+ rid + "\' and sid = \'" + sid + "\'")) {
					list_public_pro.addAll(manager
							.getProList("SELECT pro FROM rules where rid = \'"
									+ rid + "\'"));
				}
			}

		} else {
			// is not user
			// to find out whether user set the friend privacy
			ArrayList<String> ridList = manager
					.getList("SELECT rid FROM rules WHERE uid = \'" + uid
							+ "\' and classname = \'" + classname
							+ "\' and level = 2");
			for (String rid : ridList) {
				if (manager.query("SELECT * FROM rules_friends where rid = \'"
						+ rid + "\' and fid = \'" + uname + "\'")) {
					list_public_pro.addAll(manager
							.getProList("SELECT pro FROM rules where rid = \'"
									+ rid + "\'"));
				}
			}

		}
		return list_public_pro;
	}
}
