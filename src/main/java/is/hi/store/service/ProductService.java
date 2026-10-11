package is.hi.store.service;

import is.hi.store.dto.ProductResponse;
import is.hi.store.entity.Product;
import is.hi.store.entity.User;
import is.hi.store.entity.StockMovement;
import is.hi.store.exception.ProductNotFoundException;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.exception.InvalidCredentialsException;
import is.hi.store.repository.ProductRepository;
import is.hi.store.repository.UserRepository;
import is.hi.store.repository.StockMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import is.hi.store.dto.ProductCreateRequest;
import is.hi.store.dto.ProductUpdateRequest;
import is.hi.store.dto.StockMovementRequest;
import is.hi.store.dto.FieldErrorDetail;
import is.hi.store.dto.StockMovementResponse;
import is.hi.store.entity.StockMovement.MovementType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public interface ProductService {
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductCreateRequest request);
	StockMovementResponse stockMovement(long productId, long userId, StockMovementRequest request);
	ProductResponse flagProduct(long id, Boolean flag);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
    void deleteProduct(Long id);
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

	// Product stock change and the movement record commit together, or neither does.
	@Transactional
	public StockMovementResponse stockMovement(long productId, long userId, StockMovementRequest request) {
		Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
		User user = userRepository.findById(userId);
		if(user == null)
			throw new InvalidCredentialsException("Account no longer exists");

		List<FieldErrorDetail> errors = new ArrayList<>();

		int currentStockQuantity = product.getStockQuantity();
		int movementQuantity = request.getQuantity();

		// Missing quantity in the JSON also lands here as 0.
		if(movementQuantity <= 0)
			errors.add(new FieldErrorDetail("quantity", "Quantity must be a positive number"));
		// Without this a null type fell through to the subtract branch and skipped the stock check.
		if(request.getType() == null)
			errors.add(new FieldErrorDetail("type", "Type must be ADD or REMOVE"));
		if(request.getType() == MovementType.REMOVE && currentStockQuantity < movementQuantity)
			errors.add(new FieldErrorDetail("stock", "Product Stock is too low for this operation"));

		if(!errors.isEmpty())
			throw new InvalidRequestException(errors);

		if(request.getType() == MovementType.ADD)
			product.setStockQuantity(currentStockQuantity + movementQuantity);
		else
			product.setStockQuantity(currentStockQuantity - movementQuantity);

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
        // Default to 0, a null stock would crash stock movements (UC1) with a 500.
        product.setStockQuantity(request.getStockQuantity() == null ? 0 : request.getStockQuantity());
        product.setImageUrl(imageUrl);

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

	public ProductResponse flagProduct(long id, Boolean flag) {
		Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
		if(flag == null)
			throw new InvalidRequestException(List.of(new FieldErrorDetail("flagged", "Flagged must be true or false")));

		product.setReorderFlagged(flag);
		return mapToResponse(productRepository.save(product));
	}

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        List<FieldErrorDetail> errors = new ArrayList<>();
        if (request != null) {
            if (request.getName() != null) {
                if (request.getName().isBlank()) {
                    errors.add(new FieldErrorDetail("name", "Name must not be blank"));
                } else if (request.getName().length() > 255) {
                    errors.add(new FieldErrorDetail("name", "Name must be at most 255 characters"));
                }
            }
            if (request.getCategory() != null) {
                if (request.getCategory().isBlank()) {
                    errors.add(new FieldErrorDetail("category", "Category must not be blank"));
                } else if (request.getCategory().length() > 255) {
                    errors.add(new FieldErrorDetail("category", "Category must be at most 255 characters"));
                }
            }
            if (request.getPrice() != null) {
                if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add(new FieldErrorDetail("price", "Price must be greater than zero"));
                }
            }
            if (request.getStockQuantity() != null) {
                if (request.getStockQuantity() < 0) {
                    errors.add(new FieldErrorDetail("stockQuantity", "Stock quantity must not be negative"));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }

        if (request != null) {
            if (request.getName() != null) {
                product.setName(request.getName());
            }
            if (request.getCategory() != null) {
                product.setCategory(request.getCategory());
            }
            if (request.getPrice() != null) {
                product.setPrice(request.getPrice());
            }
            if (request.getStockQuantity() != null) {
                product.setStockQuantity(request.getStockQuantity());
            }
        }

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.delete(product);
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
