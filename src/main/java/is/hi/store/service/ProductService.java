package is.hi.store.service;

import is.hi.store.dto.ProductResponse;
import is.hi.store.entity.Product;
import is.hi.store.exception.ProductNotFoundException;
import is.hi.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import is.hi.store.dto.ProductCreateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.nio.file.Files;
import java.nio.file.Path;

public interface ProductService {
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductCreateRequest request);
}

@Service
class ProductServiceImplementation implements ProductService {
    private final ProductRepository productRepository;
    private final String uploadDirectory = "uploads/";
  
    public ProductServiceImplementation(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        return new ProductResponse(product);
    }
    public ProductResponse createProduct(ProductCreateRequest request) {
        String imageUrl = null;
        MultipartFile file = request.getImage();


        if (file != null) {
            try {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                String timestamp = now.format(formatter);
                String fileName = timestamp + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDirectory).resolve(fileName);

                Files.copy(file.getInputStream(), filePath);
                imageUrl = "/images/" + fileName;
            } catch (IOException e) {
                throw new RuntimeException("you sleaze failed", e);
            }
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(imageUrl);

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setImageUrl(product.getImageUrl());
        response.setReorderFlagged(product.isReorderFlagged());
        response.setCreatedAt(product.getCreatedAt());
        return response;
    }
}
