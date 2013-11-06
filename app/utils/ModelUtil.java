package utils;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;

public class ModelUtil {

	/**
	 * get a specific class's properties
	 * 
	 * @param classname
	 * @return
	 */
	public static ArrayList<String> getPropertyList(String classname) {
		ArrayList<String> list = new ArrayList<String>();
		OntClass oc = getExistedClass(classname);

		if (oc == null) {
			return null;
		} else {
			// true means list all properties except relation
			for (Iterator<?> i = oc.listDeclaredProperties(true); i.hasNext();) {
				OntProperty op = (OntProperty) i.next();

				if (op != null) {
					list.add(op.getLocalName());
				}
			}
			return list;
		}
	}

	/**
	 * get a specific class's properties
	 * 
	 * @param classname
	 * @param oc
	 * @return
	 */
	public static ArrayList<String> getPropertyList(String classname,
			OntClass oc) {
		ArrayList<String> list = new ArrayList<String>();

		if (oc == null) {
			return null;
		} else {
			for (Iterator<?> i = oc.listDeclaredProperties(); i.hasNext();) {
				OntProperty op = (OntProperty) i.next();

				if (op != null) {
					list.add(op.getLocalName());
				}
			}
			return list;
		}
	}

	/**
	 * get a existed class
	 * 
	 * @param classname
	 * @return
	 */
	public static OntClass getExistedClass(String classname) {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		OntClass oc = model.getOntClass(prefix + classname);
		if (oc == null) {
			return null;
		} else {
			return oc;
		}
	}

}
