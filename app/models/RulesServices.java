package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class RulesServices extends Model {

	private static final long serialVersionUID = 1810193715095349802L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rules_services_rsid_seq")
	public Integer rsid;
	public Integer rid;
	public String sid;

	public static Finder<Integer, RulesServices> find = new Finder<Integer, RulesServices>(
			Integer.class, RulesServices.class);
}
