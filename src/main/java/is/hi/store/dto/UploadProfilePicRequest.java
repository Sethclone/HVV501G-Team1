package is.hi.store.dto;

import org.springframework.web.multipart.MultipartFile;


public class UploadProfilePicRequest {
	MultipartFile image;

	public MultipartFile getImage() {
		return image;
	}
	public void setImage(MultipartFile image) {
		this.image = image;
	}
}
