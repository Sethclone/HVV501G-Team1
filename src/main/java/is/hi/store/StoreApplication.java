package is.hi.store;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import is.hi.store.controller.RegisterController;

@SpringBootApplication
public class StoreApplication {
    public static void main(String[] args) {
      SpringApplication.run(StoreApplication.class, args);
    }
}
