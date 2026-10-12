package is.hi.store.controller;

import is.hi.store.dto.ProductResponse;
import is.hi.store.dto.ReorderRequest;
import is.hi.store.dto.ImageUploadRequest;
import is.hi.store.service.ProductService;
import is.hi.store.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import is.hi.store.dto.ProductCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

	@GetMapping("/{slug}")
	public ResponseEntity<byte[]> getImage(@PathVariable String slug) {
		byte[] image = imageService.getImageBySlug(slug);
		return new ResponseEntity<byte[]>(image, HttpStatus.OK);
	}
}
