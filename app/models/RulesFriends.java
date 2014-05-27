package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class RulesFriends extends Model {

	private static final long serialVersionUID = 6647266865522167141L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rules_friends_rfid_seq")
	public Integer rfid;
	public Integer rid;
	public String fid;

	public static Finder<Integer, RulesFriends> find = new Finder<Integer, RulesFriends>(
			Integer.class, RulesFriends.class);
}
