package is.hi.store.entity;
import jakarta.persistence.*;

@Entity
@Table(name="users")
public class User {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	@Column(name="name")
	private String name;
	@Column(name="password")
	private String password;

	public User() {};

	public void setName(String name) {this.name=name;}
	public String getName() {return name;}
	public long getId() {return id;}
}
