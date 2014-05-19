package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonUtil;
import utils.ModelUtil;
import utils.MyOntModel;
import utils.SHA1;
import utils.StringUtil;
import utils.UserUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

import db.MyDBManager;

public class Application extends Controller {

	public static Result index() {
		return ok("hello pdc");
	}

	/**
	 * user or developer login
	 * 
	 * @return
	 */
	public static Result login() {
		StringUtil.printStart("login");
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String uid = json.findPath("id").textValue();
		String pwd = json.findPath("password").textValue();
		Boolean isDev = json.findPath("isDev").asBoolean();

		MyDBManager manager = new MyDBManager();

		// how to operate database correctly in controllers?
		String sql = null;
		if (isDev) {
			sql = "SELECT * FROM dev WHERE dname=\'" + uid + "\' and dpwd=\'"
					+ pwd + "\'";
		} else {
			sql = "SELECT * FROM users WHERE uid=\'" + uid + "\' and pwd=\'"
					+ pwd + "\'";
		}

		if (manager.query(sql)) {
			ObjectNode on = Json.newObject();
			on.put("result", SHA1.getSHA1String(uid));

			StringUtil.printEnd("login");
			return ok(on);
		} else {
			StringUtil.printEnd("login");
			return ok(JsonUtil.getFalseJson());
		}
	}

	/**
	 * register a user, and insert a row in table users
	 * 
	 * @return
	 */
	public static Result registerUser() {
		StringUtil.printStart("register user");
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String uid = json.findPath("u_id").textValue();
		String pwd = json.findPath("u_password").textValue();
		// Boolean isDev = json.findPath("isDev").asBoolean();
		Boolean isDev = false; // do not use developer registration, it is
								// unsafe.

		MyDBManager manager = new MyDBManager();

		if (isDev) {
			manager.insertIntoDevTable(uid, pwd, SHA1.getSHA1String(uid));
		} else {
			manager.insertIntoTable("users", "uid", uid, pwd,
					SHA1.getSHA1String(uid));

			OntModel model = MyOntModel.getInstance().getModel();

			// create a user, store in ontology
			String prefix = model.getNsPrefixURI("");
			OntClass oUser = model.getOntClass(prefix + UserUtil.userClassname);
			Individual iUser = oUser.createIndividual(prefix + uid);
			iUser.addLabel(SHA1.getSHA1String(uid), null);
			Iterator<String> it = json.fieldNames();
			ModelUtil.addIndividualProperties(oUser, iUser, it, json);
		}

		StringUtil.printEnd("register user");
		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * register a service. It is uncompleted.
	 * 
	 * @return
	 */
	public static Result registerApp() {
		StringUtil.printStart("register service");
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String sname = json.findPath("name").textValue();
		String did = json.findPath("did").textValue();

		// confirm whether dev is correct
		if (!new MyDBManager().confirmDev(did)) {
			StringUtil.printEnd("register service");
			return ok(JsonUtil.getFalseJson());
		}

		// insert a row into service table
		new MyDBManager().insertIntoServiceTable(sname,
				SHA1.getSHA1String(sname));

		StringUtil.printEnd("register service");
		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * set privacy rules. 0 means public, 1 means service-privacy, 2 means
	 * friend-privacy
	 * 
	 * @return
	 */
	public static Result setRules() {
		StringUtil.printStart("set rules");
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());

		String uid = json.findPath("uid").textValue();
		MyDBManager manager = new MyDBManager();

		// confirm whether user is correct
		if (!manager.confirmUser(uid)) {
			StringUtil.printEnd("set rules");
			return ok(JsonUtil.getFalseJson());
		}

		String classname = json.findPath("classname").textValue();
		Boolean allpro = json.findPath("allpro").asBoolean();
		Integer level = json.findPath("level").asInt();
		JsonNode array = json.findPath("pro");
		ArrayList<String> proList = new ArrayList<String>();

		// if set some properties
		if (!allpro) {
			if (array.isArray()) {
				for (Iterator<JsonNode> it = array.elements(); it.hasNext();) {
					proList.add(it.next().textValue());
				}
			}
		}

		// get sid, if user set service-privacy
		ArrayList<String> gsidList = new ArrayList<String>();
		ArrayList<String> gfidList = new ArrayList<String>();
		if (level == 1) {
			JsonNode sarray = json.findPath("sid");
			if (sarray.isArray()) {
				for (Iterator<JsonNode> it = sarray.elements(); it.hasNext();) {
					gsidList.add(it.next().textValue());
				}
			}
		}

		// get fid, if user set friend-privacy
		if (level == 2) {
			JsonNode farray = json.findPath("fid");
			if (farray.isArray()) {
				for (Iterator<JsonNode> it = farray.elements(); it.hasNext();) {
					gfidList.add(it.next().textValue());
				}
			}
		}

		// if the rule is existed, modify the properties
		if (manager
				.query("SELECT * FROM rules WHERE uid=\'" + uid
						+ "\' and classname=\'" + classname + "\' and level = "
						+ level)) {
			if (level == 0) {
				// level = 0: modify properties directly
				manager.update(allpro,
						proList.toArray(new String[proList.size()]), level,
						classname, uid);
			} else if (level == 1) {
				// level = 1: if user set a existed service, delete it; else,
				// insert it into rules table
				ArrayList<String> ridList = manager
						.getList("SELECT rid FROM rules WHERE uid = \'" + uid
								+ "\' and classname = \'" + classname
								+ "\' and level = " + level);
				HashMap<String, String> sidMap = new HashMap<String, String>();
				for (String rid : ridList) {
					ArrayList<String> tmp = manager
							.getList("SELECT sid FROM rules_services where rid = \'"
									+ rid + "\'");
					HashMap<String, String> tmpMap = new HashMap<String, String>();
					for (String tsid : tmp) {
						tmpMap.put(tsid, rid);
					}
					sidMap.putAll(tmpMap);
				}

				// delete existed rules
				for (String es : gsidList) {
					if (sidMap.containsKey(es)) {
						manager.delete("DELETE FROM rules_services WHERE sid = \'"
								+ es
								+ "\' and rid = \'"
								+ sidMap.get(es)
								+ "\'");
					}
				}

				// add new rules into table rules
				String irid = manager.insertIntoRules(uid, classname, allpro,
						proList.toArray(new String[proList.size()]), level);

				// add new rules into table rules_services
				for (String sid : gsidList) {
					manager.insertIntoRulesServices(irid, sid);
				}

			} else if (level == 2) {
				// level = 2: if user set a existed friend, delete it; else,
				// insert it into rules table
				ArrayList<String> ridList = manager
						.getList("SELECT rid FROM rules WHERE uid = \'" + uid
								+ "\' and classname = \'" + classname
								+ "\' and level = " + level);
				HashMap<String, String> fidMap = new HashMap<String, String>();
				for (String rid : ridList) {
					ArrayList<String> tmp = manager
							.getList("SELECT fid FROM rules_friends where rid = \'"
									+ rid + "\'");
					HashMap<String, String> tmpMap = new HashMap<String, String>();
					for (String tfid : tmp) {
						tmpMap.put(tfid, rid);
					}
					fidMap.putAll(tmpMap);
				}

				// delete existed rules
				for (String ef : gfidList) {
					if (fidMap.containsKey(ef)) {
						manager.delete("DELETE FROM rules_friends WHERE fid = \'"
								+ ef
								+ "\' and rid = \'"
								+ fidMap.get(ef)
								+ "\'");
					}
				}

				// add new rules into table rules
				String irid = manager.insertIntoRules(uid, classname, allpro,
						proList.toArray(new String[proList.size()]), level);

				// add new rules into table rules_friends
				for (String fid : gfidList) {
					manager.insertIntoRulesFriends(irid, fid);
				}
			}
		} else {
			// user set a new rule
			manager.insertIntoRules(uid, classname, allpro,
					proList.toArray(new String[proList.size()]), level);
		}

		StringUtil.printEnd("set rules");
		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * get owl file
	 * 
	 * @return
	 */
	public static Result getOwlFile() {
		StringUtil.printStart("get file");
		OntModel model = MyOntModel.getInstance().getModel();
		String fileName = "owl/pdc_ontology_" + System.currentTimeMillis()
				+ ".rdf";
		File file = new File(fileName);

		try {
			FileOutputStream fos = new FileOutputStream(file);
			model.write(fos);
			fos.close();

			StringUtil.printEnd("get file");
			return ok(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringUtil.printEnd("get file");
		return ok(JsonUtil.getFalseJson());
	}
}
