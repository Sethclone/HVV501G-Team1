package is.hi.store.utils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.Tika;
import is.hi.store.dto.FieldErrorDetail;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.entity.Image;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;



public class ImageUtils {
	//These are not final, can be changed later
	//I think maxFileSize can be specified in application.properties, maybe that would be better?
	private static final String uploadDirectory = "uploads/";
	private static final List<String> allowedImageTypes = List.of("image/png");
	private static final long maxFileSize = 10*1024*1024;

	private static void validateUpload(MultipartFile file) throws IOException {
		Tika tika = new Tika();
		String detectedFileType = tika.detect(file.getBytes());
		List<FieldErrorDetail> errors = new ArrayList<>();

        if (file == null) {
			errors.add(new FieldErrorDetail("image", "image can not be null"));
		}
		if (file.getSize() > maxFileSize) {
			errors.add(new FieldErrorDetail("image", "image size can not excede "+maxFileSize+" bytes"));
		}
		if(!allowedImageTypes.contains(detectedFileType)) {
			errors.add(new FieldErrorDetail("image", detectedFileType + " not supported"));
		}

		if(!errors.isEmpty())
			throw new InvalidRequestException(errors);
	}

	public static String upload(MultipartFile file) throws IOException{
		validateUpload(file);

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String timestamp = now.format(formatter);
		String fileName = timestamp + "_" + file.getOriginalFilename();
		Path filePath = Paths.get(uploadDirectory).resolve(fileName);

		Files.copy(file.getInputStream(), filePath);
		String imageUrl = "/images/" + fileName;

		return imageUrl;
	}

	public static Image uploadToDatabase(MultipartFile file) throws IOException{	
		validateUpload(file);
		
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String timestamp = now.format(formatter);
		String fileName = timestamp + "_" + file.getOriginalFilename();

		String imageUrl = fileName;

		Image image = new Image();
		image.setSlug(imageUrl);
		image.setImage(file.getBytes());

		return image;
	}
}
