package controllers;

import java.util.ArrayList;
import java.util.Iterator;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonUtil;
import utils.ModelUtil;
import utils.MyOntModel;
import utils.QueryUtil;
import utils.UserUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class MyOntology extends Controller {

	private static OntModel model;

	/**
	 * request for all onto classes
	 * 
	 * @return
	 */
	public static Result all() {
		ArrayList<String> nameList = new ArrayList<String>();
		model = MyOntModel.getInstance().getModel();

		for (ExtendedIterator<OntClass> i = model.listNamedClasses(); i
				.hasNext();) {
			OntClass oc = (OntClass) i.next();

			if (oc != null) {
				nameList.add(oc.getLocalName());
			}
		}

		return ok(JsonUtil.addList2Json("classes", nameList));
	}

	/**
	 * request for properties
	 * 
	 * @param classname
	 * @return
	 */
	public static Result getProperties(String classname, String uid) {
		UserUtil.uid = uid;
		ArrayList<String> proList = ModelUtil.getPropertyList(classname);

		if (proList == null) {
			return badRequest(JsonUtil.getFalseJson());
		} else {
			return ok(JsonUtil.addList2Json(classname, proList));
		}
	}

	/**
	 * request for relation
	 * 
	 * @param classname1
	 * @param classname2
	 * @return
	 */
	public static Result getRelation(String classname1, String classname2,
			String uid) {
		UserUtil.uid = uid;
		model = MyOntModel.getInstance().getModel();
		String defaultPrefix = model.getNsPrefixURI("");
		String rdfsPrefix = model.getNsPrefixURI("rdfs");
		String owlPrefix = model.getNsPrefixURI("owl");

		// Create a new query
		String queryString = "PREFIX default: <" + defaultPrefix + ">\n"
				+ "PREFIX rdfs: <" + rdfsPrefix + ">\n" + "PREFIX owl: <"
				+ owlPrefix + ">\n" + "SELECT ?relation\n"
				+ "WHERE { ?relation rdfs:domain default:" + classname1
				+ ".?relation rdfs:range default:" + classname2 + "}";

		ResultSet results = QueryUtil.doQuery(model, queryString);

		// Get property value
		String relationValue;
		if (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			relationValue = result.get("relation").toString()
					.substring(defaultPrefix.length());
		} else {
			relationValue = null;
		}

		QueryUtil.closeQE();

		ObjectNode on = Json.newObject();
		on.put("relation", relationValue);

		return ok(on);
	}

	/**
	 * add a class
	 * 
	 * @return
	 */
	public static Result add() {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");

		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String classname = json.findPath("classname").textValue();

		if (classname == null) {
			return badRequest(JsonUtil.getFalseJson());
		}

		OntClass oc = model.createClass(prefix + classname);
		JsonNode array = json.findPath("attr");
		OntProperty op;

		if (array.isArray()) {
			for (Iterator<JsonNode> it = array.elements(); it.hasNext();) {
				JsonNode attr = it.next();
				op = model.createOntProperty(prefix + attr.textValue());
				// attach a property to a class
				op.addDomain(oc);
			}
			MyOntModel.getInstance().updateModel(model);
		}
		return ok(JsonUtil.getTrueJson());
	}
}
