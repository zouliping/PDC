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
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.AllDifferent;
import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DataRange;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.FunctionalProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.InverseFunctionalProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.Profile;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SymmetricProperty;
import com.hp.hpl.jena.ontology.TransitiveProperty;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
		Long a = System.currentTimeMillis();
		ArrayList<String> proList = ModelUtil.getPropertyList(classname);
		Long b = System.currentTimeMillis();
		System.out.println("----" + (b - a));

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
		Long a = System.currentTimeMillis();
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
		Long b = System.currentTimeMillis();
		System.out.println("----" + (b - a));

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
