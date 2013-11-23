package controllers;

import java.util.Iterator;

import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonUtil;
import utils.ModelUtil;
import utils.MyOntModel;
import utils.SHA1;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

public class Application extends Controller {

	public static Result login() {
		JsonNode json = request().body().asJson();
		return ok();
	}

	public static Result registerUser() {
		JsonNode json = request().body().asJson();
		String uid = SHA1.getSHA1String(json.toString());
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		OntClass oUser = model.getOntClass(prefix + "User");
		Individual iUser = oUser.createIndividual(prefix + uid);
		Iterator<String> it = json.fieldNames();
		ModelUtil.addIndividualProperties(oUser, iUser, it, json);
		return ok();
	}
}
