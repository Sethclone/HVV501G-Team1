package is.hi.store.config;

import is.hi.store.entity.User;
import is.hi.store.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Reads the bearer token and, if valid, loads the user it belongs to. Role and email come from the DB,
// not the token, so an admin's changes (UC7: demotion, email change, password reset) apply on the next request.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			if (jwtService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
				// By id, not email, so a token survives an admin changing the user's email.
				User user = userRepository.findById(jwtService.extractUserId(token));
				if (user != null) {	// deleted user: stay unauthenticated, ends in a 401
					// A pending password change grants no role, so SecurityConfig only lets PUT /api/users/me through.
					String authority = user.isMustChangePassword() ? "PASSWORD_CHANGE_REQUIRED" : "ROLE_" + user.getRole();
					var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of(new SimpleGrantedAuthority(authority)));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		}
		filterChain.doFilter(request, response);
	}
}
