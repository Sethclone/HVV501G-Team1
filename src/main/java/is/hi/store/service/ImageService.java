package is.hi.store.service;

import is.hi.store.repository.ImageRepository;
import is.hi.store.entity.Image;
import is.hi.store.dto.RegisterResponse;
import is.hi.store.exception.EmailAlreadyExistsException;
import is.hi.store.exception.ProductNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.InputStream;

public interface ImageService {
	void upload(MultipartFile file);
	byte[] getImageBySlug(String slug);
}

@Service
class ImageServiceImplementation implements ImageService {
	@Autowired
	private ImageRepository imageRepository;

	public byte[] getImageBySlug(String slug) {
		Image image = imageRepository.findBySlug(slug).orElseThrow(() -> new ProductNotFoundException(new Long(1)));
		byte[] data = image.getImage();
		return data;
	}

	public void upload(MultipartFile file) {
		try {
			Image image = new Image();
			image.setSlug("test");
			image.setImage(file.getBytes());

			imageRepository.save(image);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
