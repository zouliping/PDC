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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
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
	public static Result getProperties(String classname) {
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
	public static Result getRelation(String classname1, String classname2) {
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
			return badRequest(JsonUtil.getFalseJson());
		}

		QueryUtil.closeQE();

		ObjectNode on = Json.newObject();
		on.put("relation", relationValue);

		System.out.println(on);
		return ok(on);
	}

	/**
	 * add a class
	 * 
	 * @return
	 */
	public static Result add() {
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String classname = json.findPath("classname").textValue();

		if (classname == null) {
			return badRequest(JsonUtil.getFalseJson());
		}

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");

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

	/**
	 * add relation between a class and a class
	 * 
	 * @return
	 */
	public static Result addRelation() {
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String class1 = json.findPath("class1").textValue();
		String class2 = json.findPath("class2").textValue();
		String relation = json.findPath("relation").textValue();

		if (class1 == null || class2 == null || relation == null) {
			return badRequest(JsonUtil.getFalseJson());
		}

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");

		OntClass ontClass1 = model.getOntClass(prefix + class1);
		OntClass ontClass2 = model.getOntClass(prefix + class2);
		ObjectProperty op = model.createObjectProperty(prefix + relation);

		if (ontClass1 == null || ontClass2 == null) {
			return badRequest(JsonUtil.getFalseJson());
		}

		StatementImpl stmt = new StatementImpl(ontClass1, op, ontClass2);
		model.add(stmt);
		MyOntModel.getInstance().updateModel(model);

		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * get label
	 * 
	 * @param isclass
	 * @param name
	 * @return
	 */
	public static Result getLabel(String isclass, String name) {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		ArrayList<String> relationList = new ArrayList<String>();

		if ("true".equals(isclass) || "True".equals(isclass)) {
			OntClass oc = model.getOntClass(prefix + name);

			if (oc == null) {
				return badRequest(JsonUtil.getFalseJson());
			}

			for (ExtendedIterator<?> ei = oc.listLabels(null); ei.hasNext();) {
				String tmp = ei.next().toString();
				if (tmp.contains("^^")) {
					tmp = tmp.substring(0, tmp.indexOf("^^"));
				}
				relationList.add(tmp);
			}

			return ok(JsonUtil.addList2Json("label", relationList));
		} else if ("false".equals(isclass) || "False".equals(isclass)) {
			OntProperty op = model.getOntProperty(prefix + name);

			if (op == null) {
				return badRequest(JsonUtil.getFalseJson());
			}

			for (ExtendedIterator<?> ei = op.listLabels(null); ei.hasNext();) {
				String tmp = (String) ei.next().toString();
				if (tmp.contains("^^")) {
					tmp = tmp.substring(0, tmp.indexOf("^^"));
				}
				relationList.add(tmp);
			}

			return ok(JsonUtil.addList2Json("label", relationList));
		} else {
			return badRequest(JsonUtil.getFalseJson());
		}
	}

	/**
	 * add label
	 * 
	 * @return
	 */
	public static Result addLabel() {
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		Boolean isClass = json.path("isClass").asBoolean();
		String name = json.findPath("name").textValue();

		JsonNode array = json.findPath("label");
		ArrayList<String> labelList = new ArrayList<String>();
		if (array.isArray()) {
			for (Iterator<JsonNode> it = array.elements(); it.hasNext();) {
				labelList.add(it.next().textValue());
			}
		}

		int len = labelList.size();
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");

		if (isClass) {
			OntClass oc = model.getOntClass(prefix + name);
			if (oc == null) {
				return badRequest(JsonUtil.getFalseJson());
			}

			if (len == 0) {
				return badRequest(JsonUtil.getFalseJson());
			} else if (len == 1) {
				oc.setLabel(labelList.get(0), null);
			} else {
				oc.setLabel(labelList.get(0), null);
				for (int i = 1; i < len; i++) {
					oc.addLabel(labelList.get(i), null);
				}
			}
		} else {
			OntProperty op = model.getOntProperty(prefix + name);
			if (op == null) {
				return badRequest(JsonUtil.getFalseJson());
			}

			if (len == 0) {
				return badRequest(JsonUtil.getFalseJson());
			} else if (len == 1) {
				op.setLabel(labelList.get(0), null);
			} else {
				op.setLabel(labelList.get(0), null);
				for (int i = 1; i < len; i++) {
					op.addLabel(labelList.get(i), null);
				}
			}
		}
		return ok(JsonUtil.getTrueJson());
	}
}
