package is.hi.store.service;

import is.hi.store.dto.*;
import is.hi.store.entity.Product;
import is.hi.store.entity.User;
import is.hi.store.entity.StockMovement;
import is.hi.store.exception.ImageNotFoundException;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.repository.ProductRepository;
import is.hi.store.repository.UserRepository;
import is.hi.store.repository.StockMovementRepository;
import is.hi.store.util.ImageStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.MalformedURLException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.*;

public interface ImageService {
    Resource getImageByFilename(String fileName);
}

@Service
class ImageServiceImplementation implements ImageService {
  
    public ImageServiceImplementation() {}

    public Resource getImageByFilename(String fileName) throws MalformedURLException{
		if(fileName.contains("/")) {
			throw new InvalidRequestException(List.of(new FieldErrorDetail("fileName", "Not a valid filename")));	
		}

		Path path = Paths.get("uploads/").resolve(fileName);
		if(!path.toFile().exists())	
			throw new ImageNotFoundException(fileName);
		
		Resource resource = null;
		try {
			resource = new UrlResource(path.toUri());
		} catch (MalformedURLException e) {	
			throw new ImageNotFoundException(fileName);
		}

        return resource;
    }
}
