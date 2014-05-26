package models;

import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Rules extends Model {

	private static final long serialVersionUID = -8695072079729151635L;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_sid_seq")
	public Integer rid;
	public String uid;
	public String classname;
	public ArrayList<String> pro;
	public Boolean allpro;
	public Integer level;

	public static Finder<Integer, Rules> find = new Finder<Integer, Rules>(
			Integer.class, Rules.class);
}
