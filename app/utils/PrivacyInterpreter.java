package utils;

import java.util.ArrayList;
import java.util.List;

import models.Rules;
import models.RulesFriends;
import models.RulesServices;

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

		// if user uses portal, he can check all his data
		if (isMe() && UserUtil.sid.equals(sid)) {
			return ModelUtil.getPropertyList(classname);
		}

		String get_user = SHA1.getSHA1String(uname);
		// first, get public pro
		Rules rules = Rules.find.where().eq("uid", get_user)
				.eq("classname", classname).eq("level", 0).findUnique();
		Boolean allpro = false;

		if (rules != null) {
			allpro = rules.allpro;
			// user set public rules
			if (allpro) {
				return ModelUtil.getPropertyList(classname);
			} else {
				list_public_pro = rules.getPro();
			}
		} else {
			list_public_pro = new ArrayList<String>();
		}

		System.out.println("public pro");
		for (String pro : list_public_pro) {
			System.out.println(pro);
		}

		if (isMe()) {
			// is user
			// to find out whether user sets the service privacy
			List<Rules> list_rules = Rules.find.where().eq("uid", uid)
					.eq("classname", classname).eq("level", 1).findList();
			for (Rules tmp : list_rules) {
				RulesServices rs = RulesServices.find.where()
						.eq("rid", tmp.rid).eq("sid", sid).findUnique();

				if (rs != null) {
					Rules r = Rules.find.byId(tmp.rid);

					ArrayList<String> list_tmp = r.getPro();
					list_public_pro.removeAll(list_tmp);
					list_public_pro.addAll(list_tmp);
				}
			}

			System.out.println("is user pro");
			for (String pro : list_public_pro) {
				System.out.println(pro);
			}

		} else {
			// is not user
			// to find out whether user set the friend privacy
			List<Rules> list_rules = Rules.find.where().eq("uid", get_user)
					.eq("classname", classname).eq("level", 2).findList();
			for (Rules tmp : list_rules) {
				RulesFriends rf = RulesFriends.find.where().eq("rid", tmp.rid)
						.eq("fid", uid).findUnique();

				if (rf != null) {
					Rules r = Rules.find.byId(tmp.rid);
					ArrayList<String> list_tmp = r.getPro();
					list_public_pro.removeAll(list_tmp);
					list_public_pro.addAll(list_tmp);
				}
			}

			System.out.println("is not user pro");
			for (String pro : list_public_pro) {
				System.out.println(pro);
			}

		}
		return list_public_pro;
	}
}
