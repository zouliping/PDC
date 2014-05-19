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
import utils.StringUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import db.MyDBManager;

public class MyOntology extends Controller {

	private static OntModel model;

	/**
	 * request for all onto classes
	 * 
	 * @return
	 */
	public static Result all() {
		StringUtil.printStart("get all classes");
		ArrayList<String> nameList = new ArrayList<String>();
		model = MyOntModel.getInstance().getModel();

		for (ExtendedIterator<OntClass> i = model.listNamedClasses(); i
				.hasNext();) {
			OntClass oc = (OntClass) i.next();

			if (null != oc) {
				if (!oc.toString().startsWith(StringUtil.CLASSNAME_PREFIX)) {
					nameList.add(oc.getLocalName());
				}
			}
		}

		StringUtil.printEnd("get all classes");
		return ok(JsonUtil.addList2Json("classes", nameList));
	}

	/**
	 * request for properties
	 * 
	 * @param classname
	 * @return
	 */
	public static Result getProperties(String classname) {
		StringUtil.printStart("get a class' properties");
		ArrayList<String> proList = ModelUtil.getPropertyList(classname);

		if (proList == null) {
			StringUtil.printEnd("get a class' properties");
			return ok(JsonUtil.getFalseJson());
		} else {
			ObjectNode on = JsonUtil.addList2Json(classname, proList);
			System.out.println(on);
			StringUtil.printEnd("get a class' properties");
			return ok(on);
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
		StringUtil.printStart("get a relation (ontology)");
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

		// Get relation value, a class and a class only can have one relation
		String relationValue;
		if (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			relationValue = result.get("relation").toString()
					.substring(defaultPrefix.length());
		} else {
			relationValue = null;
			StringUtil.printEnd("get a relation (ontology)");
			return ok(JsonUtil.getFalseJson());
		}

		QueryUtil.closeQE();

		ObjectNode on = Json.newObject();
		on.put("relation", relationValue);

		System.out.println(on);
		StringUtil.printEnd("get a relation (ontology)");
		return ok(on);
	}

	/**
	 * add a class
	 * 
	 * @return
	 */
	public static Result add() {
		StringUtil.printStart("add a class (ontology)");
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String classname = json.findPath("classname").textValue();
		String token = json.findPath("did").textValue();

		if (classname == null) {
			StringUtil.printEnd("add a class (ontology)");
			return ok(JsonUtil.getFalseJson());
		}

		// confirm whether dev is correct
		if (!new MyDBManager().confirmDev(token)) {
			StringUtil.printEnd("add a class (ontology)");
			return ok(JsonUtil.getFalseJson());
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

		StringUtil.printEnd("add a class (ontology)");
		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * add relation between a class and a class
	 * 
	 * @return
	 */
	public static Result addRelation() {
		StringUtil.printStart("add a relation (ontology)");
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String class1 = json.findPath("class1").textValue();
		String class2 = json.findPath("class2").textValue();
		String relation = json.findPath("relation").textValue();
		String token = json.findPath("did").textValue();

		if (class1 == null || class2 == null || relation == null) {
			StringUtil.printEnd("add a relation (ontology)");
			return ok(JsonUtil.getFalseJson());
		}

		// confirm whether dev is correct
		if (!new MyDBManager().confirmDev(token)) {
			StringUtil.printEnd("add a relation (ontology)");
			return ok(JsonUtil.getFalseJson());
		}

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");

		OntClass ontClass1 = model.getOntClass(prefix + class1);
		OntClass ontClass2 = model.getOntClass(prefix + class2);
		ObjectProperty op = model.createObjectProperty(prefix + relation);

		if (ontClass1 == null || ontClass2 == null) {
			StringUtil.printEnd("add a relation (ontology)");
			return ok(JsonUtil.getFalseJson());
		}
		// in ontology, add relation need to add objectProperty and set domain
		// and range for it.
		op.setDomain(ontClass1);
		op.setRange(ontClass2);

		MyOntModel.getInstance().updateModel(model);

		StringUtil.printEnd("add a relation (ontology)");
		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * get label. true means class label, false means property label.
	 * 
	 * @param isclass
	 * @param name
	 * @return
	 */
	public static Result getLabel(String isclass, String name) {
		StringUtil.printStart("get labels (ontology)");
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		ArrayList<String> labelList = new ArrayList<String>();

		// class label
		if ("true".equals(isclass) || "TRUE".equals(isclass)) {
			OntClass oc = model.getOntClass(prefix + name);

			if (null == oc) {
				StringUtil.printEnd("get labels (ontology)");
				return ok(JsonUtil.getFalseJson());
			}

			for (ExtendedIterator<?> ei = oc.listLabels(null); ei.hasNext();) {
				String tmp = ei.next().toString();
				if (tmp.contains("^^")) {
					tmp = tmp.substring(0, tmp.indexOf("^^"));
				}
				labelList.add(tmp);
			}

			StringUtil.printEnd("get labels (ontology)");
			return ok(JsonUtil.addList2Json("label", labelList));
		} else if ("false".equals(isclass) || "FALSE".equals(isclass)) {
			// property label
			OntProperty op = model.getOntProperty(prefix + name);

			if (op == null) {
				StringUtil.printEnd("get labels (ontology)");
				return ok(JsonUtil.getFalseJson());
			}

			for (ExtendedIterator<?> ei = op.listLabels(null); ei.hasNext();) {
				String tmp = (String) ei.next().toString();
				if (tmp.contains("^^")) {
					tmp = tmp.substring(0, tmp.indexOf("^^"));
				}
				labelList.add(tmp);
			}

			StringUtil.printEnd("get labels (ontology)");
			return ok(JsonUtil.addList2Json("label", labelList));
		} else {
			StringUtil.printEnd("get labels (ontology)");
			return ok(JsonUtil.getFalseJson());
		}
	}

	/**
	 * add label
	 * 
	 * @return
	 */
	public static Result addLabel() {
		StringUtil.printStart("add labels (ontology)");
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		Boolean isClass = json.path("isClass").asBoolean();
		String name = json.findPath("name").textValue();
		String token = json.findPath("did").textValue();

		// confirm whether dev is correct
		if (!new MyDBManager().confirmDev(token)) {
			StringUtil.printEnd("add labels (ontology)");
			return ok(JsonUtil.getFalseJson());
		}

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
				StringUtil.printEnd("add labels (ontology)");
				return ok(JsonUtil.getFalseJson());
			}

			if (len == 0) {
				StringUtil.printEnd("add labels (ontology)");
				return ok(JsonUtil.getFalseJson());
			} else if (len == 1) {
				// set label to delete the existed labels
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
				StringUtil.printEnd("add labels (ontology)");
				return ok(JsonUtil.getFalseJson());
			}

			if (len == 0) {
				StringUtil.printEnd("add labels (ontology)");
				return ok(JsonUtil.getFalseJson());
			} else if (len == 1) {
				op.setLabel(labelList.get(0), null);
			} else {
				op.setLabel(labelList.get(0), null);
				for (int i = 1; i < len; i++) {
					op.addLabel(labelList.get(i), null);
				}
			}
		}

		StringUtil.printEnd("add labels (ontology)");
		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * get related classes
	 * 
	 * @param classname
	 * @return
	 */
	public static Result getRelatedClasses(String classname) {
		StringUtil.printStart("get related classes");
		OntModel model = MyOntModel.getInstance().getModel();

		// Get prefixes
		String defaultPrefix = model.getNsPrefixURI("");
		String rdfsPrefix = model.getNsPrefixURI("rdfs");
		String owlPrefix = model.getNsPrefixURI("owl");

		OntClass oct = model.getOntClass(defaultPrefix + classname);
		if (classname == null || oct == null) {
			StringUtil.printEnd("get related classes");
			return ok(JsonUtil.getFalseJson());
		}

		// Create a new query to find classes which have labels
		String queryString = "PREFIX default: <" + defaultPrefix + ">\n"
				+ "PREFIX rdfs: <" + rdfsPrefix + ">\n" + "PREFIX owl: <"
				+ owlPrefix + ">\n" + "SELECT ?class\n"
				+ "WHERE { ?relation rdfs:domain default:" + classname
				+ ". \n ?relation rdfs:range ?class .}";

		ResultSet resultSet = QueryUtil.doQuery(model, queryString);

		ObjectNode re = Json.newObject();
		Boolean haveResult = false;

		while (resultSet.hasNext()) {
			QuerySolution result = resultSet.nextSolution();
			String name = result.get("class").toString();

			// find out the classes
			if (name.contains(defaultPrefix)) {
				haveResult = true;
				OntClass oc = model.getOntClass(name);
				ArrayList<String> proList = ModelUtil.getPropertyList(oc);

				ArrayNode an = re.arrayNode();

				for (String tmp : proList) {
					an.add(tmp);
				}

				re.putArray(name.substring(defaultPrefix.length())).addAll(an);
			}
		}

		QueryUtil.closeQE();

		if (haveResult) {
			System.out.println(re);
			StringUtil.printEnd("get related classes");
			return ok(re);
		} else {
			StringUtil.printEnd("get related classes");
			// if a class does not have related classes, return "null"
			return ok(StringUtil.STRING_NULL);
		}

	}
}
