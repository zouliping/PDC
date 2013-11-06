package controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import db.MyDBHelper;

public class Ontology extends Controller {

	private static MyDBHelper helper = new MyDBHelper();

	/**
	 * request for all onto classes
	 * 
	 * @return
	 */
	public static Result all() throws SQLException, ClassNotFoundException {
		ArrayList<String> nameList = new ArrayList<String>();
		OntModel model = getModelFromDB(helper.getConnection(), "pdc");

		for (Iterator<?> i = model.listNamedClasses(); i.hasNext();) {
			OntClass oc = (OntClass) i.next();

			if (oc != null) {
				nameList.add(oc.getLocalName());
			}
		}

		helper.closeConnection();

		ObjectNode result = Json.newObject();
		result.putPOJO("classes", nameList);
		return ok(result);
	}

	/**
	 * request for properties
	 * 
	 * @param className
	 * @return
	 */
	public static Result getProperties(String className){
		OntModel model = getModelFromDB(helper.getConnection(), "pdc");
		OntClass oc = model.getOntClass(className);
		if(oc == null)
			return badRequest(className + " does not exist");
		else{
//			OntProperty op = oc.get
		}
		return null;
	}

	/**
	 * get OntModel from db
	 * 
	 * @param con
	 * @param name
	 * @return
	 */
	public static OntModel getModelFromDB(IDBConnection con, String name) {
		ModelMaker maker = ModelFactory.createModelRDBMaker(con);
		Model model = maker.getModel(name);
		OntModel newmodel = ModelFactory.createOntologyModel(
				getModelSpec(maker), model);
		return newmodel;
	}

	/**
	 * get model spec
	 * 
	 * @param maker
	 * @return
	 */
	public static OntModelSpec getModelSpec(ModelMaker maker) {
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		spec.setImportModelMaker(maker);
		return spec;
	}
}
