package controllers;

import java.util.Iterator;

import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonUtil;
import utils.ModelUtil;
import utils.UserUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

import db.MyDBHelper;
import db.MyDBManager;

public class Application extends Controller {

	/**
	 * user login
	 * 
	 * @return
	 */
	public static Result login() {
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		UserUtil.uid = json.findPath("id").textValue();
		String pwd = json.findPath("password").textValue();

		MyDBManager manager = new MyDBManager();

		if (manager.query("user", "uid", UserUtil.uid, pwd)) {
			return ok(JsonUtil.getTrueJson());
		} else {
			return badRequest(JsonUtil.getFalseJson());
		}
	}

	/**
	 * register a user, and create a db for him
	 * 
	 * @return
	 */
	public static Result registerUser() {
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		UserUtil.uid = json.findPath("id").textValue();
		String pwd = json.findPath("password").textValue();

		MyDBManager manager = new MyDBManager();
		manager.insertIntoTable("user", "uid", UserUtil.uid, pwd);
		// create a new db for a user
		manager.createDB();

		MyDBHelper helper = new MyDBHelper();

		// store ontology in a new db
		OntModel model = ModelUtil.createModel(helper.getConnection());

		// create a user, store in ontology
		String prefix = model.getNsPrefixURI("");
		OntClass oUser = model.getOntClass(prefix + UserUtil.userClassname);
		Individual iUser = oUser.createIndividual(prefix + UserUtil.uid);
		Iterator<String> it = json.fieldNames();
		ModelUtil.addIndividualProperties(oUser, iUser, it, json);
		return ok(JsonUtil.getTrueJson());
	}
}
