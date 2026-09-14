package is.hi.store.entity;
import jakarta.persistence.*;
import java.time.Instant;
 
@Entity
@Table(name="users")
public class User {
	public enum Role{STAFF, ADMIN};
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	@Column(name="name")
	private String name;
	@Column(name="email")
	private String email;
	@Column(name="password")
	private String password;
	@Enumerated(EnumType.STRING)
	@Column(name="role")
	private Role role;
	@Column(name="createdAt")
	private Instant createdAt;

	public User() {createdAt = Instant.now();}

	public void setUsername(String name) {this.name=name;}
	public void setPassword(String password) {this.password = password;}
	public void setEmail(String email) {this.email = email;}
	public void setRole(Role role) {this.role = role;};

	public String getUsername() {return name;}
	public String getPassword() {return password;}
	public String getEmail() {return email;}
	public Role getRole() {return role;}
	public Instant getCreatedAt() {return createdAt;}
	public long getId() {return id;}
}
