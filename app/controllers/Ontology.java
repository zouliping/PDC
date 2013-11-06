package controllers;

import java.util.ArrayList;
import java.util.Iterator;

import play.mvc.Controller;
import play.mvc.Result;
import utils.MyOntModel;
import utils.MyUtils;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

public class Ontology extends Controller {

	private static OntModel model;

	/**
	 * request for all onto classes
	 * 
	 * @return
	 */
	public static Result all() {
		ArrayList<String> nameList = new ArrayList<String>();
		model = MyOntModel.getInstance().getModel();

		for (Iterator<?> i = model.listNamedClasses(); i.hasNext();) {
			OntClass oc = (OntClass) i.next();

			if (oc != null) {
				nameList.add(oc.getLocalName());
			}
		}

		return ok(MyUtils.addList2Json("classes", nameList));
	}

	/**
	 * request for properties
	 * 
	 * @param classname
	 * @return
	 */
	public static Result getProperties(String classname) {
		model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		OntClass oc = model.getOntClass(prefix + classname);
		ArrayList<String> proList = new ArrayList<String>();

		if (oc == null)
			return badRequest(MyUtils.getFalseJson());
		else {
			for (Iterator<?> i = oc.listDeclaredProperties(); i.hasNext();) {
				OntProperty op = (OntProperty) i.next();

				if (op != null) {
					proList.add(op.getLocalName());
				}
			}

			return ok(MyUtils.addList2Json(classname, proList));
		}
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
