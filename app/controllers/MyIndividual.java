package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonUtil;
import utils.ModelUtil;
import utils.MyOntModel;
import utils.PrivacyInterpreter;
import utils.QueryUtil;
import utils.UserUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import db.MyDBManager;

public class MyIndividual extends Controller {

	/**
	 * add a individual. client post a json, for example
	 * {"classname":"Writing","individualname"
	 * :"test","uid":"123456","booktitle":"titleTest"}
	 * 
	 * @return
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result update() {
		JsonNode json = request().body().asJson();
		System.out.println(json);

		String classname = json.findPath("classname").textValue();
		String individualname = json.findPath("individualname").textValue();
		String token = json.findPath("uid").textValue();

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");

		Long a = System.currentTimeMillis();
		OntClass oc = model.getOntClass(prefix + classname);
		Long b = System.currentTimeMillis();
		System.out.println("getontclass time " + (b - a));
		if (oc == null) {
			return ok(JsonUtil.getFalseJson());
		}

		Individual i = model.getIndividual(prefix + individualname);
		if (i == null) {
			System.out.println("create indiv");
			i = oc.createIndividual(prefix + individualname);
			// set user label
			i.addLabel(token, null);
		}

		// confirm whether user is correct
		if (!new MyDBManager().confirmUser(token)) {
			return ok(JsonUtil.getFalseJson());
		}

		String old_location;
		String new_location;

		Iterator<String> it;
		ArrayList<String> newList = new ArrayList<String>();
		for (it = json.fieldNames(); it.hasNext();) {
			String pro = it.next();
			if ("classname".equals(pro) || "individualname".equals(pro)
					|| "uid".equals(pro)) {
			} else {
				if (("User".equals(classname))
						&& ("current_location".equals(pro))) {
					new_location = json.findPath(pro).textValue();
					Individual individual = model.getIndividual(prefix + token);
					OntProperty op = model.getOntProperty(prefix + pro);
					if (individual.getPropertyValue(op) != null) {
						old_location = individual.getPropertyValue(op)
								.toString();
						if (new_location != null && old_location != null
								&& !(new_location.equals(old_location))) {
							System.out.println("send notification");
							UserUtil.sendNotification(old_location,
									new_location, token);
						}
					}

				}
				newList.add(pro);
			}
		}
		if (newList.size() > 0) {
			ModelUtil.addIndividualProperties(oc, i, newList, json);
		}

		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * add relation
	 * 
	 * @return
	 */
	public static Result addRelation() {
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());

		String id1 = json.findPath("id1").textValue();
		String id2 = json.findPath("id2").textValue();
		String token = json.findPath("uid").textValue();
		String classname1 = ModelUtil.getClassname(id1);
		String classname2 = ModelUtil.getClassname(id2);

		// confirm whether user is correct
		if (!new MyDBManager().confirmUser(token)) {
			return ok(JsonUtil.getFalseJson());
		}

		System.out.println(classname1 + "---" + classname2);

		if (classname1 == null || classname2 == null) {
			return ok(JsonUtil.getFalseJson());
		}

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		String relation = "";

		// add follow between user and user
		if (UserUtil.userClassname.equals(classname1)
				&& UserUtil.userClassname.equals(classname2)) {
			relation = prefix + "follow";
		} else {
			// get relation between cls1 and cls2
			relation = ModelUtil.getRelation(classname1, classname2);
			if (relation == null) {
				relation = ModelUtil.getRelation(classname2, classname1);
				String tmp = id1;
				id1 = id2;
				id2 = tmp;
			}
		}
		System.out.println(relation);
		Individual i1 = model.getIndividual(prefix + id1);
		Individual i2 = model.getIndividual(prefix + id2);

		ObjectProperty op = model.getObjectProperty(relation);

		if (op == null) {
			return ok(JsonUtil.getFalseJson());
		}

		StatementImpl stmt = new StatementImpl(i1, op, i2);
		model.add(stmt);

		MyOntModel.getInstance().updateModel(model);

		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * remove a relation between a individual and a individual
	 * 
	 * @return
	 */
	public static Result removeRelation() {
		JsonNode json = request().body().asJson();
		System.out.println(json);
		String indivi1 = json.findPath("indivi1").textValue();
		String indivi2 = json.findPath("indivi2").textValue();
		String relation = json.findPath("relation").textValue();
		String token = json.findPath("uid").textValue();

		// confirm whether user is correct
		if (!new MyDBManager().confirmUser(token)) {
			return ok(JsonUtil.getFalseJson());
		}

		if (indivi1 == null || indivi2 == null || relation == null) {
			return ok(JsonUtil.getFalseJson());
		}

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");

		Individual i1 = model.getIndividual(prefix + indivi1);
		Individual i2 = model.getIndividual(prefix + indivi2);
		OntProperty op = model.getOntProperty(prefix + relation);

		if (i1 == null || i2 == null || op == null) {
			return ok(JsonUtil.getFalseJson());
		}

		StatementImpl stmt = new StatementImpl(i1, op, i2);
		model.remove(stmt);

		MyOntModel.getInstance().updateModel(model);

		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * get a class's individuals
	 * 
	 * @param classname
	 * @return
	 */
	public static Result get(String classname, String uid, String uname,
			String sid) {
		ArrayList<String> list_privacy_pro = new PrivacyInterpreter(uid, uname,
				sid, classname).checkRules();

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		OntClass oc = model.getOntClass(prefix + classname);
		ObjectNode on = Json.newObject();

		if (oc == null) {
			return ok(JsonUtil.getFalseJson());
		}

		// confirm whether user is correct
		if (!new MyDBManager().confirmUser(uid)) {
			return ok(JsonUtil.getFalseJson());
		}

		// get user's followers
		if (UserUtil.userClassname.equals(classname)) {
			List<String> followers = ModelUtil.getFollowers(uid);
			for (String tmp : followers) {
				// System.out.println(tmp);
				Individual iFollower = model.getIndividual(tmp);
				ObjectNode proNode = Json.newObject();
				for (StmtIterator si = iFollower.listProperties(); si.hasNext();) {
					StatementImpl sti = (StatementImpl) si.next();
					if (list_privacy_pro.contains(sti.getPredicate()
							.getLocalName())) {
						proNode.put(sti.getPredicate().getLocalName(), sti
								.getObject().toString());
					}
				}
				on.put(iFollower.getLocalName(), proNode);
			}

		} else {

			// get class's individuals
			for (ExtendedIterator<?> i = oc.listInstances(); i.hasNext();) {
				Individual individual = (Individual) i.next();
				System.out.println(individual.getLocalName());
				if (ModelUtil.isUserIndiv(individual, uid)) {
					ObjectNode proNode = Json.newObject();
					for (StmtIterator si = individual.listProperties(); si
							.hasNext();) {
						StatementImpl sti = (StatementImpl) si.next();
						if (list_privacy_pro.contains(sti.getPredicate()
								.getLocalName())) {
							proNode.put(sti.getPredicate().getLocalName(), sti
									.getObject().toString());
						}
					}
					on.put(individual.getLocalName(), proNode);
				}
			}
		}
		System.out.println(on);
		return ok(on);
	}

	/**
	 * get a individual's properties
	 * 
	 * @param individualname
	 * @return
	 */
	public static Result getProperties(String individualname, String proname,
			String uid) {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		Individual individual = model.getIndividual(prefix + individualname);

		// confirm whether user is correct
		if (!new MyDBManager().confirmUser(uid)) {
			return ok(JsonUtil.getFalseJson());
		}

		if (individual == null) {
			return ok(JsonUtil.getFalseJson());
		}

		ObjectNode on = Json.newObject();
		if (proname == null) {
			for (StmtIterator si = individual.listProperties(); si.hasNext();) {
				StatementImpl sti = (StatementImpl) si.next();
				on.put(sti.getPredicate().getLocalName(), sti.getObject()
						.toString());
			}
		} else {
			OntProperty op = model.getOntProperty(prefix + proname);
			if (individual.getPropertyValue(op) == null) {
				return ok(JsonUtil.getFalseJson());
			}
			on.put(proname, individual.getPropertyValue(op).toString());
		}
		System.out.println(on);
		return ok(on);
	}

	/**
	 * get individuals by labels
	 * 
	 * @return
	 */
	public static Result getIndivByLabel() {
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());

		String uid = json.findPath("uid").textValue();

		// confirm whether user is correct
		if (!new MyDBManager().confirmUser(uid)) {
			return ok(JsonUtil.getFalseJson());
		}

		OntModel model = MyOntModel.getInstance().getModel();

		// Get prefixes
		String defaultPrefix = model.getNsPrefixURI("");
		String rdfsPrefix = model.getNsPrefixURI("rdfs");
		String owlPrefix = model.getNsPrefixURI("owl");

		// Create a new query to find classes which have labels
		String queryString = "PREFIX default: <" + defaultPrefix + ">\n"
				+ "PREFIX rdfs: <" + rdfsPrefix + ">\n" + "PREFIX owl: <"
				+ owlPrefix + ">\n" + "SELECT ?tmp\n" + "WHERE { ";

		JsonNode array = json.findPath("label");
		if (array.isArray()) {
			for (Iterator<JsonNode> it = array.elements(); it.hasNext();) {
				JsonNode label = it.next();
				queryString += "?tmp rdfs:label \"" + label.textValue()
						+ "\"^^<http://www.w3.org/2001/XMLSchema#string>.";
			}
			queryString += "}";
		}

		// System.out.println(queryString);
		ResultSet resultSet = QueryUtil.doQuery(model, queryString);

		Boolean isClass = json.path("isClass").asBoolean();
		ObjectNode re = Json.newObject();

		while (resultSet.hasNext()) {
			QuerySolution result = resultSet.nextSolution();
			String name = result.get("tmp").toString();
			System.out.println(name);

			// deal with class label
			if (isClass) {
				OntClass oc = model.getOntClass(name);
				for (ExtendedIterator<?> ei = oc.listInstances(); ei.hasNext();) {
					Individual i = (Individual) ei.next();
					System.out.println(i.getLocalName());
					if (ModelUtil.isUserIndiv(i, uid)) {
						ObjectNode tmp = Json.newObject();

						for (StmtIterator st = i.listProperties(); st.hasNext();) {
							StatementImpl sti = (StatementImpl) st.next();
							tmp.put(sti.getPredicate().getLocalName(), sti
									.getObject().toString());
							// System.out.println(sti.getPredicate()
							// .getLocalName()
							// + "___"
							// + sti.getObject().toString());
						}
						re.put(i.getLocalName(), tmp);
					}
				}
				// deal with property label
			} else {
				ArrayList<OntClass> classList = new ArrayList<OntClass>();
				OntProperty op = model.getOntProperty(name);
				ExtendedIterator<?> ei = op.listDomain();

				// to get all classes which have the same property
				if (ei.hasNext()) {
					OntClass oc = (OntClass) ei.next();
					if (oc.isUnionClass()) {
						UnionClass unionClass = oc.asUnionClass();
						ei = unionClass.listOperands();
						while (ei.hasNext()) {
							OntClass itemClass = (OntClass) ei.next();
							classList.add(itemClass);
						}
					} else {
						classList.add(model.getOntClass(op.getDomain()
								.toString()));
					}
				}

				for (OntClass tmpClass : classList) {
					for (ExtendedIterator<?> ei2 = tmpClass.listInstances(); ei2
							.hasNext();) {
						Individual individual = (Individual) ei2.next();
						if (ModelUtil.isUserIndiv(individual, uid)) {
							ObjectNode onNode = Json.newObject();
							if ((individual.getPropertyValue(op)) != null) {
								onNode.put(name, individual
										.getPropertyValue(op).toString());
							}
							re.put(individual.getLocalName(), onNode);
						}
					}
				}
			}
		}
		QueryUtil.closeQE();
		System.out.println(re);
		return ok(re);
	}

	/**
	 * remove the individual
	 * 
	 * @param name
	 * @return
	 */
	public static Result remove() {
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String indivname = json.findPath("indivname").textValue();
		String proname = json.findPath("proname").textValue();
		String token = json.findPath("uid").textValue();

		// confirm whether user is correct
		if (!new MyDBManager().confirmUser(token)) {
			return ok(JsonUtil.getFalseJson());
		}

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		Individual i = model.getIndividual(prefix + indivname);

		if (i == null) {
			return ok(JsonUtil.getFalseJson());
		}
		if (proname == null) {
			System.out.println("before remove");
			i.remove();
			System.out.println("after remove");
		} else {
			OntProperty op = model.getOntProperty(prefix + proname);
			if (op != null) {
				i.removeProperty(op, i.getPropertyValue(op));
			} else {
				return ok(JsonUtil.getFalseJson());
			}
		}
		// update the model in db
		MyOntModel.getInstance().updateModel(model);
		System.out.println(JsonUtil.getTrueJson().toString());
		return ok(JsonUtil.getTrueJson());
	}
}
