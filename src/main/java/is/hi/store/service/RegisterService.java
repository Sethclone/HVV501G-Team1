package is.hi.store.service;

import is.hi.store.repository.UserRepository;



public interface RegisterService {
	void register(String username, String password, );		
}

class RegisterServiceImplementation implements RegisterService {
	private UserRepository userRepository;
}
