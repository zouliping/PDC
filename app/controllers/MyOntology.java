package controllers;

import java.util.ArrayList;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonUtil;
import utils.ModelUtil;
import utils.MyOntModel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
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

		for (ExtendedIterator i = model.listNamedClasses(); i.hasNext();) {
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

		// Create the query
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Get property value
		String relationValue;
		if (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			relationValue = result.get("relation").toString()
					.substring(defaultPrefix.length());
		} else {
			relationValue = null;
		}

		// Important - free up resources used running the query
		qe.close();

		ObjectNode on = Json.newObject();
		on.put("relation", relationValue);

		return ok(on);
	}

}
