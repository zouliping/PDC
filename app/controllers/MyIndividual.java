package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonUtil;
import utils.ModelUtil;
import utils.MyOntModel;
import utils.PrivacyInterpreter;
import utils.QueryUtil;
import utils.SHA1;
import utils.StringUtil;
import utils.UserUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Instance API. Including: Update Individual, Get Individuals, Get Individual's
 * Properties, Add Relation, Remove Relation, Remove A Individual, Get
 * Individuals By Labels. 7
 * 
 * @author zouliping
 * 
 */
public class MyIndividual extends Controller {

	/**
	 * add a individual
	 * 
	 * @return
	 */
	public static Result update() {
		StringUtil.printStart(StringUtil.UPDATE_INDIVIDUAL);
		JsonNode json = request().body().asJson();
		Logger.info(json.toString());

		String classname = json.findPath("classname").textValue();
		String individualname = json.findPath("individualname").textValue();
		String token = json.findPath("uid").textValue();

		if (classname == null || individualname == null || token == null) {
			return ok(JsonUtil.getFalseJson(1006, StringUtil.UPDATE_INDIVIDUAL));
		}

		// confirm whether user is correct
		if (!UserUtil.confirmUser(token)) {
			return ok(JsonUtil.getFalseJson(1004, StringUtil.UPDATE_INDIVIDUAL));
		}

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");

		Long a = System.currentTimeMillis();
		OntClass oc = model.getOntClass(prefix + classname);
		Long b = System.currentTimeMillis();
		Logger.info("get ont class time " + (b - a));

		if (oc == null) {
			return ok(JsonUtil.getFalseJson(1007, StringUtil.UPDATE_INDIVIDUAL));
		}

		Individual i = model.getIndividual(prefix + individualname);
		String datachange = null;
		if (i == null) {
			i = oc.createIndividual(prefix + individualname);
			// set user label
			i.addLabel(token, null);
			datachange = StringUtil.NEW_DATA + classname;
		} else {
			datachange = StringUtil.UPDATE_DATA + classname;
		}

		String old_location;
		String new_location;

		Iterator<String> it;
		ArrayList<String> newList = new ArrayList<String>();
		for (it = json.fieldNames(); it.hasNext();) {
			String pro = it.next();
			if ("classname".equals(pro) || "individualname".equals(pro)
					|| "uid".equals(pro)) {
				it.remove();
			} else {
				if ((UserUtil.userClassname.equals(classname))
						&& ("u_current_location".equals(pro))) {
					new_location = json.findPath(pro).textValue();
					Individual individual = model.getIndividual(prefix + token);
					OntProperty op = model.getOntProperty(prefix + pro);
					if (individual.getPropertyValue(op) != null) {
						old_location = StringUtil.removeSpecialChar(individual
								.getPropertyValue(op).toString());
						if (new_location != null && old_location != null
								&& !(new_location.equals(old_location))) {
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

		ObjectNode on = Json.newObject();

		on.put("datachange", datachange);
		on.put("indivname", individualname);

		Logger.info(on.toString());
		UserUtil.sendNotificationToU(datachange, on, token);

		return ok(JsonUtil.getTrueJson(StringUtil.UPDATE_INDIVIDUAL));
	}

	/**
	 * add relation
	 * 
	 * @return
	 */
	public static Result addRelation() {
		StringUtil.printStart(StringUtil.ADD_INDIVIDUAL_RELATION);
		JsonNode json = request().body().asJson();
		Logger.info(json.toString());

		String id1 = json.findPath("id1").textValue();
		String id2 = json.findPath("id2").textValue();
		String token = json.findPath("uid").textValue();

		if (id1 == null || id2 == null || token == null) {
			return ok(JsonUtil.getFalseJson(1006,
					StringUtil.ADD_INDIVIDUAL_RELATION));
		}

		// if id is not existed individual name, classname will be null
		String classname1 = ModelUtil.getClassname(id1);
		String classname2 = ModelUtil.getClassname(id2);

		// confirm whether user is correct
		if (!UserUtil.confirmUser(token)) {
			return ok(JsonUtil.getFalseJson(1004,
					StringUtil.ADD_INDIVIDUAL_RELATION));
		}

		if (classname1 == null || classname2 == null) {
			return ok(JsonUtil.getFalseJson(1007,
					StringUtil.ADD_INDIVIDUAL_RELATION));
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
		Individual i1 = model.getIndividual(prefix + id1);
		Individual i2 = model.getIndividual(prefix + id2);

		if (relation == null) {
			return ok(JsonUtil.getFalseJson(1007,
					StringUtil.ADD_INDIVIDUAL_RELATION));
		}

		ObjectProperty op = model.getObjectProperty(relation);

		if (op == null) {
			return ok(JsonUtil.getFalseJson(1007,
					StringUtil.ADD_INDIVIDUAL_RELATION));
		}

		// in individual, add relation just need to add a statement.
		StatementImpl stmt = new StatementImpl(i1, op, i2);
		model.add(stmt);

		model.commit();
		// MyOntModel.getInstance().updateModel(model);

		return ok(JsonUtil.getTrueJson(StringUtil.ADD_INDIVIDUAL_RELATION));
	}

	/**
	 * remove a relation between a individual and a individual
	 * 
	 * @return
	 */
	public static Result removeRelation() {
		StringUtil.printStart(StringUtil.REMOVE_INDIVIDUAL_RELATION);
		JsonNode json = request().body().asJson();
		Logger.info(json.toString());
		String indivi1 = json.findPath("indiv1").textValue();
		String indivi2 = json.findPath("indiv2").textValue();
		String relation = json.findPath("relation").textValue();
		String token = json.findPath("uid").textValue();

		if (token == null || indivi1 == null || indivi2 == null
				|| relation == null) {
			return ok(JsonUtil.getFalseJson(1006,
					StringUtil.REMOVE_INDIVIDUAL_RELATION));
		}

		// confirm whether user is correct
		if (!UserUtil.confirmUser(token)) {
			return ok(JsonUtil.getFalseJson(1004,
					StringUtil.REMOVE_INDIVIDUAL_RELATION));
		}

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");

		Individual i1 = model.getIndividual(prefix + indivi1);
		Individual i2 = model.getIndividual(prefix + indivi2);
		OntProperty op = model.getOntProperty(prefix + relation);

		if (i1 == null || i2 == null || op == null) {
			return ok(JsonUtil.getFalseJson(1007,
					StringUtil.REMOVE_INDIVIDUAL_RELATION));
		}

		StatementImpl stmt = new StatementImpl(i1, op, i2);
		if (model.contains(stmt)) {
			model.remove(stmt);
		} else {
			stmt = new StatementImpl(i2, op, i1);
			if (model.contains(stmt)) {
				model.remove(stmt);
			} else {
				// if exchange the order, it does not contain the statement,
				// return false
				return ok(JsonUtil.getFalseJson(1007,
						StringUtil.REMOVE_INDIVIDUAL_RELATION));
			}
		}

		model.commit();
		// MyOntModel.getInstance().updateModel(model);

		return ok(JsonUtil.getTrueJson(StringUtil.REMOVE_INDIVIDUAL_RELATION));
	}

	/**
	 * get a class's individuals
	 * 
	 * @param classname
	 * @return
	 */
	public static Result get(String classname, String uid, String uname,
			String sid, Integer page, Integer num) {
		StringUtil.printStart(StringUtil.GET_INDIVIDUALS);
		ArrayList<String> list_privacy_pro = new PrivacyInterpreter(uid, uname,
				sid, classname).checkRules();
		Logger.info(classname + " public pro " + list_privacy_pro.size());
		// for (String tmp : list_privacy_pro) {
		// System.out.println(tmp);
		// }

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		OntClass oc = model.getOntClass(prefix + classname);
		ObjectNode on = Json.newObject();

		if (oc == null) {
			return ok(JsonUtil.getFalseJson(1007, StringUtil.GET_INDIVIDUALS));
		}

		// confirm whether user is correct
		if (!UserUtil.confirmUser(uid)) {
			return ok(JsonUtil.getFalseJson(1004, StringUtil.GET_INDIVIDUALS));
		}

		if (page < 0 || num < 0) {
			return ok(JsonUtil.getFalseJson(1007, StringUtil.GET_INDIVIDUALS));
		}

		String get_user = SHA1.getSHA1String(uname);

		// get user's followers
		if (UserUtil.userClassname.equals(classname)) {
			List<String> followers = ModelUtil.getFollowers(get_user);

			Integer indiv_size = followers.size();
			Integer end = (page + 1) * num;
			Integer start = page * num;
			if (end > indiv_size) {
				end = indiv_size;
			}

			for (int i = start; i < end; i++) {
				Individual iFollower = model.getIndividual(followers.get(i));
				ObjectNode proNode = Json.newObject();
				for (StmtIterator si = iFollower.listProperties(); si.hasNext();) {
					StatementImpl sti = (StatementImpl) si.next();
					if (list_privacy_pro.contains(sti.getPredicate()
							.getLocalName())) {
						// if the property is object property, it may have many
						// values, add a
						// "+";else add a
						// "-"
						ArrayNode an = on.arrayNode();
						if (sti.getObject().toString().startsWith(prefix)) {
							ObjectProperty op = model.getObjectProperty(sti
									.getPredicate().toString());
							for (NodeIterator ni = iFollower
									.listPropertyValues(op); ni.hasNext();) {
								an.add(ni.next().toString());
							}
							proNode.putArray(
									"+" + sti.getPredicate().getLocalName())
									.addAll(an);
						} else {
							proNode.put(
									"-" + sti.getPredicate().getLocalName(),
									sti.getObject().toString());
						}
					}
				}
				on.put(iFollower.getLocalName(), proNode);
			}

		} else {
			// get class's individuals
			List<?> tmp_list = oc.listInstances().toList();
			Integer total = tmp_list.size();
			Integer remove_num = 0;
			Integer indiv_size = 0;

			// from end to start
			for (int i = total - 1; i > -1; i--) {
				Individual individual = (Individual) tmp_list.get(i);

				if (!ModelUtil.isUserIndiv(individual, get_user)) {
					tmp_list.remove(i);
					remove_num++;
				}
			}

			indiv_size = total - remove_num;
			Integer end = (page + 1) * num;
			Integer start = page * num;

			if (start > indiv_size) {
				return ok(JsonUtil.getFalseJson(1007,
						StringUtil.GET_INDIVIDUALS));
			}

			if (end > indiv_size) {
				end = indiv_size;
			}

			for (int i = start; i < end; i++) {
				Individual individual = (Individual) tmp_list.get(i);
				ObjectNode proNode = Json.newObject();
				for (StmtIterator si = individual.listProperties(); si
						.hasNext();) {
					StatementImpl sti = (StatementImpl) si.next();
					if (list_privacy_pro.contains(sti.getPredicate()
							.getLocalName())) {
						// if the property is object property, add a
						// "+";else add a
						// "-"
						ArrayNode an = on.arrayNode();
						if (sti.getObject().toString().startsWith(prefix)) {
							ObjectProperty op = model.getObjectProperty(sti
									.getPredicate().toString());
							for (NodeIterator ni = individual
									.listPropertyValues(op); ni.hasNext();) {
								an.add(ni.next().toString());
							}
							proNode.putArray(
									"+" + sti.getPredicate().getLocalName())
									.addAll(an);
						} else {
							proNode.put(
									"-" + sti.getPredicate().getLocalName(),
									sti.getObject().toString());
						}
					}
				}
				on.put(individual.getLocalName(), proNode);
			}
		}

		Logger.info(on.toString());
		StringUtil.printEnd(StringUtil.GET_INDIVIDUALS);
		return ok(on);
	}

	/**
	 * get a individual's properties
	 * 
	 * @param individualname
	 * @return
	 */
	public static Result getProperties(String individualname, String proname,
			String uid, String uname, String sid) {
		StringUtil.printStart(StringUtil.GET_INDIVIDUAL_PROPERTY);
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		Individual individual = model.getIndividual(prefix + individualname);

		// confirm whether user is correct
		if (!UserUtil.confirmUser(uid)) {
			return ok(JsonUtil.getFalseJson(1004,
					StringUtil.GET_INDIVIDUAL_PROPERTY));
		}

		if (individual == null) {
			return ok(JsonUtil.getFalseJson(1007,
					StringUtil.GET_INDIVIDUAL_PROPERTY));
		}

		ArrayList<String> list_privacy_pro = new PrivacyInterpreter(uid, uname,
				sid, individual.getOntClass().getLocalName()).checkRules();

		ObjectNode on = Json.newObject();
		if (proname == null) {
			for (StmtIterator si = individual.listProperties(); si.hasNext();) {
				StatementImpl sti = (StatementImpl) si.next();
				if (list_privacy_pro
						.contains(sti.getPredicate().getLocalName())) {
					// if the property is object property, add a "+";else add a
					// "-"
					ArrayNode an = on.arrayNode();
					if (sti.getObject().toString().startsWith(prefix)) {
						ObjectProperty op = model.getObjectProperty(sti
								.getPredicate().toString());
						for (NodeIterator ni = individual
								.listPropertyValues(op); ni.hasNext();) {
							an.add(ni.next().toString());
						}
						on.putArray("+" + sti.getPredicate().getLocalName())
								.addAll(an);
					} else {
						on.put("-" + sti.getPredicate().getLocalName(), sti
								.getObject().toString());
					}
				}
			}
		} else {
			OntProperty op = model.getOntProperty(prefix + proname);
			if (individual.getPropertyValue(op) == null) {
				return ok(JsonUtil.getFalseJson(1007,
						StringUtil.GET_INDIVIDUAL_PROPERTY));
			}
			if (list_privacy_pro.contains(proname)) {
				if (individual.getPropertyValue(op).toString()
						.startsWith(prefix)) {
					ObjectProperty obp = model.getObjectProperty(prefix
							+ proname);
					ArrayNode an = on.arrayNode();
					for (NodeIterator ni = individual.listPropertyValues(obp); ni
							.hasNext();) {
						an.add(ni.next().toString());
					}
					on.putArray("+" + proname).addAll(an);
				} else {
					on.put("-" + proname, individual.getPropertyValue(op)
							.toString());
				}
			}
		}

		Logger.info(on.toString());
		StringUtil.printEnd(StringUtil.GET_INDIVIDUAL_PROPERTY);
		return ok(on);
	}

	/**
	 * get individuals by labels
	 * 
	 * @return
	 */
	public static Result getIndivByLabel() {
		StringUtil.printStart(StringUtil.GET_BY_LABEL);
		JsonNode json = request().body().asJson();
		Logger.info(json.toString());

		String uid = json.findPath("uid").textValue();

		if (uid == null) {
			return ok(JsonUtil.getFalseJson(1006, StringUtil.GET_BY_LABEL));
		}

		// confirm whether user is correct
		if (!UserUtil.confirmUser(uid)) {
			return ok(JsonUtil.getFalseJson(1004, StringUtil.GET_BY_LABEL));
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

		// get the labels
		JsonNode array = json.findPath("label");
		if (array.isArray()) {
			for (Iterator<JsonNode> it = array.elements(); it.hasNext();) {
				JsonNode label = it.next();
				queryString += "?tmp rdfs:label \"" + label.textValue()
						+ "\"^^<http://www.w3.org/2001/XMLSchema#string>.";
			}
			queryString += "}";
		}

		// get the filters
		JsonNode array2 = json.findPath("filter");
		ArrayList<String> list_filter = new ArrayList<String>();
		if (array2.isArray()) {
			for (Iterator<JsonNode> it = array2.elements(); it.hasNext();) {
				JsonNode label = it.next();
				list_filter.add(label.textValue());
			}
		}

		ResultSet resultSet = QueryUtil.doQuery(model, queryString);

		Boolean isClass = json.path("isClass").asBoolean();
		ObjectNode re = Json.newObject();

		while (resultSet.hasNext()) {
			QuerySolution result = resultSet.nextSolution();
			String name = result.get("tmp").toString();

			// deal with class label
			if (isClass) {
				OntClass oc = model.getOntClass(name);
				for (ExtendedIterator<?> ei = oc.listInstances(); ei.hasNext();) {
					Individual i = (Individual) ei.next();
					if (ModelUtil.isUserIndiv(i, uid)) {
						ObjectNode tmp = Json.newObject();

						for (StmtIterator st = i.listProperties(); st.hasNext();) {
							StatementImpl sti = (StatementImpl) st.next();

							OntProperty tmp_pro = model.getOntProperty(sti
									.getPredicate().toString());
							if (tmp_pro != null) {
								tmp.put(tmp_pro.getLabel(null) + "#"
										+ sti.getPredicate().getLocalName(),
										sti.getObject().toString());
							}
						}
						re.put(i.getLocalName(), tmp);
					}
				}

			} else {

				// deal with property label
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
					// deal with property label, if developer sets the
					// filter, it should be removed
					if (!list_filter.contains(tmpClass.getLabel(null))) {

						for (ExtendedIterator<?> ei2 = tmpClass.listInstances(); ei2
								.hasNext();) {
							Individual individual = (Individual) ei2.next();
							if (ModelUtil.isUserIndiv(individual, uid)) {
								ObjectNode onNode = Json.newObject();
								// add property with label, set pro name like
								// "time#created_time", "null#type"

								// add other properties
								for (StmtIterator si = individual
										.listProperties(); si.hasNext();) {
									Statement stmt = si.next();
									OntProperty tmp_pro = model
											.getOntProperty(stmt.getPredicate()
													.toString());
									if (tmp_pro != null) {
										onNode.put(tmp_pro.getLabel(null)
												+ "#"
												+ stmt.getPredicate()
														.getLocalName(), stmt
												.getObject().toString());
									}
								}

								re.put(individual.getLocalName(), onNode);
							}
						}
					}
				}
			}
		}

		QueryUtil.closeQE();
		Logger.info(re.toString());
		StringUtil.printEnd(StringUtil.GET_BY_LABEL);
		return ok(re);
	}

	/**
	 * remove the individual
	 * 
	 * @param name
	 * @return
	 */
	public static Result remove() {
		StringUtil.printStart(StringUtil.REMOVE_INDIVIDUAL);
		JsonNode json = request().body().asJson();
		Logger.info(json.toString());
		String indivname = json.findPath("indivname").textValue();
		String proname = json.findPath("proname").textValue();
		String token = json.findPath("uid").textValue();

		if (indivname == null || token == null) {
			return ok(JsonUtil.getFalseJson(1006, StringUtil.REMOVE_INDIVIDUAL));
		}

		// confirm whether user is correct
		if (!UserUtil.confirmUser(token)) {
			return ok(JsonUtil.getFalseJson(1004, StringUtil.REMOVE_INDIVIDUAL));
		}

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		Individual i = model.getIndividual(prefix + indivname);

		if (i == null) {
			return ok(JsonUtil.getFalseJson(1007, StringUtil.REMOVE_INDIVIDUAL));
		}

		// if user do not set property name, delete the individual
		if (proname == null) {
			Logger.info("before remove");
			Long a = System.currentTimeMillis();
			i.remove();
			Long b = System.currentTimeMillis();
			Logger.info("after remove and use " + (b - a));
		} else {
			// if user sets property name, delete the property
			OntProperty op = model.getOntProperty(prefix + proname);
			if (op != null) {
				i.removeProperty(op, i.getPropertyValue(op));
			} else {
				return ok(JsonUtil.getFalseJson(1007,
						StringUtil.REMOVE_INDIVIDUAL));
			}
		}
		// update the model in db
		model.commit();
		// MyOntModel.getInstance().updateModel(model);
		return ok(JsonUtil.getTrueJson(StringUtil.REMOVE_INDIVIDUAL));
	}
}
