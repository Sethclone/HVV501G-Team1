package is.hi.store.util;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.*;

import is.hi.store.dto.FieldErrorDetail;
import is.hi.store.exception.InvalidRequestException;

public class ImageStorage {
	public static File uploadDirectory = new File("uploads/");
	public static List allowedMimeTypes = List.of("image/png", "image/jpeg");

	public static String save(MultipartFile file) throws IOException {	
        if (file == null) { return null; }
		if(!uploadDirectory.exists()) uploadDirectory.mkdir();

		InputStream fis = TikaInputStream.get(file.getInputStream());
		Tika tika = new Tika();
		String detectedFileType = tika.detect(fis);

		if(!allowedMimeTypes.contains(detectedFileType)) 
			throw new InvalidRequestException(List.of(new FieldErrorDetail("image", detectedFileType + " not supported")));	

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String timestamp = now.format(formatter);
		String fileName = timestamp + "_" + file.getOriginalFilename() + "." + detectedFileType.substring(detectedFileType.indexOf("/")+1);
		Path filePath = Paths.get(uploadDirectory.toString()).resolve(fileName);

		Files.copy(fis, filePath);
		String imageUrl = "/api/images/" + fileName;
		return imageUrl;
	}
}
