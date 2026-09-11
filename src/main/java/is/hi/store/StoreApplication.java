package is.hi.store;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import is.hi.store.entity.User;
import is.hi.store.repository.UserRepository;

@SpringBootApplication
@RestController
public class StoreApplication {
	@Autowired
	private UserRepository repository;

    public static void main(String[] args) {
      SpringApplication.run(StoreApplication.class, args);
    }

	//For testing
    @GetMapping("/insert")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
	  User test = new User();
	  test.setName(name);
	  repository.save(test);
	  return String.format("%s inserted into database", name);
    }
    @GetMapping("/findById")
    public String findByIdTest(@RequestParam(value = "id") int id) {
	  User test = repository.findById(id);
	  return String.format("%s", test.getName());
    }
    @GetMapping("/count")
    public String countTest() {
	  return String.format("%d", repository.count());
    }

}
