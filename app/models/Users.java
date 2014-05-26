package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Users extends Model {

	private static final long serialVersionUID = 5043001208730919822L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
	public Integer id;
	public String uid;
	public String pwd;
	public String token;

	public static Finder<Integer, Users> find = new Finder<Integer, Users>(
			Integer.class, Users.class);
}
