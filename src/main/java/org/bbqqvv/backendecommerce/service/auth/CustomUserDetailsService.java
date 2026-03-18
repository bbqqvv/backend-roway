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
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByUsernameEntity(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getAuthorities().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }

}
