package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Dev extends Model {

	private static final long serialVersionUID = -4976903340298025932L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dev_id_seq")
	public Integer id;
	public String dname;
	public String dpwd;
	public String dtoken;

	public static Finder<Integer, Dev> find = new Finder<Integer, Dev>(
			Integer.class, Dev.class);
}
