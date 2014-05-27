package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.google.common.base.Joiner;

@Entity
public class Rules extends Model {

	private static final long serialVersionUID = -8695072079729151635L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rules_rid_seq")
	public Integer rid;
	public String uid;
	public String classname;
	@Column(columnDefinition = "text")
	public String pro;
	public Boolean allpro;
	public Integer level;

	public static Finder<Integer, Rules> find = new Finder<Integer, Rules>(
			Integer.class, Rules.class);

	public ArrayList<String> getPro() {
		// Split the string along the semicolons and create the set from the
		// resulting array
		return new ArrayList<String>(Arrays.asList(pro.split(";")));
	}

	public void setPro(List<String> pro) {
		// Join the strings into a semicolon separated string
		this.pro = Joiner.on(";").join(pro);
	}
}
