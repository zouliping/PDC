package controllers;

import java.util.ArrayList;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonUtil;
import utils.ModelUtil;
import utils.MyOntModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;

public class MyIndividual extends Controller {

	/**
	 * add a individual. client post a json, for example
	 * {"classname":"Writing","individualname":"test","booktitle":"titleTest"}
	 * 
	 * @return
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result addIndividual() {
		JsonNode json = request().body().asJson();

		String classname = json.findPath("classname").textValue();
		String individualname = json.findPath("individualname").textValue();

		OntClass oc = ModelUtil.getExistedClass(classname);
		if (oc == null || individualname == null) {
			return badRequest(JsonUtil.getFalseJson());
		}

		Individual i = oc.createIndividual(individualname);
		ArrayList<String> proList = ModelUtil.getPropertyList(classname, oc);
		OntModel model = MyOntModel.getInstance().getModel();
		String pre = model.getNsPrefixURI("");
		OntProperty op;

		for (String tmp : proList) {
			op = model.getOntProperty(pre + tmp);
			i.addProperty(op, pre + json.findPath(tmp).textValue());
		}

		MyOntModel.getInstance().updateModel(model);

		return ok(JsonUtil.getTrueJson());
	}
}
