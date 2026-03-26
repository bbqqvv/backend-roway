package org.bbqqvv.backendecommerce.service.auth;


import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@lombok.extern.slf4j.Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final org.bbqqvv.backendecommerce.repository.UserRepository userRepository;
    
    public CustomUserDetailsService(org.bbqqvv.backendecommerce.repository.UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        
        String password = user.getPassword() != null ? user.getPassword() : "";
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                password,
                authorities
        );
    }
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        java.util.Set<GrantedAuthority> authorities = new java.util.HashSet<>();
        if (user.getAuthorities() != null) {
            user.getAuthorities().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.name())));
        }
        if (user.getPermissions() != null) {
            user.getPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.name())));
        }
        return authorities;
    }

}
