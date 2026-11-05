package is.hi.store.util;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.*;

public class ImageStorage {
	public static File uploadDirectory = new File("uploads/");
	public static List allowedMimeTypes = List.of("image/png", "image/jpeg");

	public static String save(MultipartFile file) throws IOException {	
        if (file == null) { return null; }
		if(!uploadDirectory.exists()) uploadDirectory.mkdir();

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String timestamp = now.format(formatter);
		String fileName = timestamp + "_" + file.getOriginalFilename();
		Path filePath = Paths.get(uploadDirectory.toString()).resolve(fileName);

		Files.copy(file.getInputStream(), filePath);
		String imageUrl = "/images/" + fileName;
		return imageUrl;
	}
}
