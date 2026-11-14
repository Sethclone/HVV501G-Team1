package is.hi.store.controller;

import is.hi.store.dto.*;
import is.hi.store.service.ImageService;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getProductById(@PathVariable String fileName) {
       	Resource resource = imageService.getImageByFilename(fileName);
		int dotIndex = resource.getFilename().lastIndexOf(".");
        return ResponseEntity.ok()
				.contentType(MediaType.valueOf("image/"+resource.getFilename().substring(dotIndex+1)))
				.body(resource);
    }

	//We could add a Post mapping for uploading images.
	//We could then decouple disk writes from database updates, fixing the atomicity issues.
	//Maybe not feasible so close to deadline.
}
