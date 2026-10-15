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

	public long getId() {return id;}
	public String getSlug() {return slug;}
	public byte[] getImage() {return image;}

	public void setId(long id) {this.id = id;}
	public void setSlug(String slug) {this.slug = slug;}
	public void setImage(byte[] image) {this.image = image;}
}
