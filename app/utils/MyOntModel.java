package utils;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import db.MyDBHelper;

public class MyOntModel {

	private MyDBHelper helper;
	private OntModel model;

	private MyOntModel() {
		helper = new MyDBHelper();
		model = getModelFromDB(helper.getConnection(), "pdc");
	}

	private static class MyOntModelHolder {
		private static MyOntModel instance = new MyOntModel();
	}

	public static MyOntModel getInstance() {
		return MyOntModelHolder.instance;
	}

	public OntModel getModel() {
		return model;
	}

	/**
	 * get OntModel from db
	 * 
	 * @param con
	 * @param name
	 * @return
	 */
	private static OntModel getModelFromDB(IDBConnection con, String name) {
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
	private static OntModelSpec getModelSpec(ModelMaker maker) {
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		spec.setImportModelMaker(maker);
		return spec;
	}
}
