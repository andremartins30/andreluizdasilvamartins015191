package mt.gov.seplag.backend.service;

import mt.gov.seplag.backend.domain.user.User;
import mt.gov.seplag.backend.domain.user.UserRepository;
import mt.gov.seplag.backend.shared.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Obtém o usuário autenticado atualmente
     * @return User autenticado
     * @throws UnauthorizedException se não houver usuário autenticado
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Usuário não autenticado");
        }

        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado: " + username));
    }

    /**
     * Obtém o ID do usuário autenticado
     * @return ID do usuário autenticado
     * @throws UnauthorizedException se não houver usuário autenticado
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Verifica se há um usuário autenticado
     * @return true se houver usuário autenticado, false caso contrário
     */
    public boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());
        } catch (Exception e) {
            return false;
        }
    }
}
