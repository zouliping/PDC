package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Rule extends Model {

	private static final long serialVersionUID = 4234041220914974836L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_sid_seq")
	public Integer rid;
	public String datachange;
	public String action;
	public String uid;

	public static Finder<Integer, Rule> find = new Finder<Integer, Rule>(
			Integer.class, Rule.class);
}
