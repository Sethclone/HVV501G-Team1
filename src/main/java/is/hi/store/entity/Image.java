package is.hi.store.entity;

import jakarta.persistence.*;

@Entity
@Table(name="images")
public class Image {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	@Column(name="slug", unique=true)
	private String slug;
	@Lob
	@Column(name = "image", columnDefinition="BLOB")
	private byte[] image;
}
