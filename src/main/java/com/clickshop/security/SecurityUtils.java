package com.clickshop.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.clickshop.entity.User;
import com.clickshop.repository.UserRepository;

@Component
public class SecurityUtils {
    @Autowired
    private UserRepository userRepository;
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println(username);
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }
    
    public int getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    public User.Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }
}