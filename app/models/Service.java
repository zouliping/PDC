package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Service extends Model {

	private static final long serialVersionUID = -7223161544296006083L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_sid_seq")
	public Integer sid;
	public String name;
	public String token;

	public static Finder<Integer, Service> find = new Finder<Integer, Service>(
			Integer.class, Service.class);
}
