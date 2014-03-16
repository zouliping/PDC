package utils;

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
		model = getModelFromDB("pdc");
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
	private OntModel getModelFromDB(String name) {
		ModelMaker maker = ModelFactory.createModelRDBMaker(helper
				.getConnection());
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
	private OntModelSpec getModelSpec(ModelMaker maker) {
		OntModelSpec spec = new OntModelSpec(
				OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		spec.setImportModelMaker(maker);
		return spec;
	}

	/**
	 * update model, and save in db
	 * 
	 * @param newModel
	 * @return
	 */
	public OntModel updateModel(OntModel newModel) {
		ModelMaker maker = ModelFactory.createModelRDBMaker(helper
				.getConnection());
		model = ModelFactory.createOntologyModel(getModelSpec(maker), newModel);
		return model;
	}
}
