package controllers;

import java.util.Iterator;

import play.mvc.Controller;
import play.mvc.Result;
import utils.ModelUtil;
import utils.SHA1;
import utils.UserUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

import db.MyDBHelper;

public class Application extends Controller {

	public static Result login() {
		// JsonNode json = request().body().asJson();
		return ok();
	}

	/**
	 * register a user, and create a db for him
	 * 
	 * @return
	 */
	public static Result registerUser() {
		JsonNode json = request().body().asJson();
		UserUtil.uid = SHA1.getSHA1String(json.toString());
		MyDBHelper helper = new MyDBHelper();
		// create a new db for a user
		helper.createDB();

		// store ontology in a new db
		OntModel model = ModelUtil.createModel(helper.getConnection());

		// OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		OntClass oUser = model.getOntClass(prefix + "User");
		Individual iUser = oUser.createIndividual(prefix + UserUtil.uid);
		Iterator<String> it = json.fieldNames();
		ModelUtil.addIndividualProperties(oUser, iUser, it, json);
		return ok();
	}
}
