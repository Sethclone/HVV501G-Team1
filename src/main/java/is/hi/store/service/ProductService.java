package is.hi.store.service;

import is.hi.store.dto.ProductResponse;
import is.hi.store.entity.Product;
import is.hi.store.entity.User;
import is.hi.store.entity.StockMovement;
import is.hi.store.exception.ProductNotFoundException;
import is.hi.store.exception.ProductStockException;
import is.hi.store.repository.ProductRepository;
import is.hi.store.repository.UserRepository;
import is.hi.store.repository.StockMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import is.hi.store.dto.ProductCreateRequest;
import is.hi.store.dto.StockMovementRequest;
import is.hi.store.dto.StockMovementResponse;
import is.hi.store.entity.StockMovement.MovementType;
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
	StockMovementResponse stockMovement(long productId, long userId, StockMovementRequest request);
	void flagProduct(long id, boolean flag);
}

@Service
class ProductServiceImplementation implements ProductService {
    private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final StockMovementRepository stockMovementRepository;
    private final String uploadDirectory = "uploads/";
  
    public ProductServiceImplementation(
		ProductRepository productRepository,
		UserRepository userRepository,
		StockMovementRepository stockMovementRepository
	) {
        this.productRepository = productRepository;
		this.userRepository = userRepository;
		this.stockMovementRepository = stockMovementRepository;
    }

    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        return new ProductResponse(product);
    }

	public StockMovementResponse stockMovement(long productId, long userId, StockMovementRequest request) {
		Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
		User user = userRepository.findById(userId);
	
		int currentStockQuantity = product.getStockQuantity();
		int movementQuantity = request.getQuantity();

		if(request.getType() == MovementType.ADD)
			product.setStockQuantity(currentStockQuantity + movementQuantity);
		else {
			if(product.getStockQuantity() < request.getQuantity())
				throw new ProductStockException(productId);
			else
				product.setStockQuantity(currentStockQuantity - movementQuantity);
		}

		productRepository.save(product);

		StockMovement stockMovement = new StockMovement();
		stockMovement.setProduct(product);
		stockMovement.setQuantity(request.getQuantity());
		stockMovement.setType(request.getType());
		stockMovement.setPerformedBy(user);

		stockMovementRepository.save(stockMovement);

		return new StockMovementResponse(stockMovement, product.getStockQuantity());

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

	public void flagProduct(long id, boolean flag) {
		Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
		product.setReorderFlagged(flag);

		productRepository.save(product);
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
