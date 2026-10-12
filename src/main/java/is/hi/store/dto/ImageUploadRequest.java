package is.hi.store.dto;

import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

public class ImageUploadRequest {

    private MultipartFile image;

    public ImageUploadRequest() {}

    public MultipartFile getImage() {
        return image;
    }
    public void setImage(MultipartFile image) {
        this.image = image;
    }
}
