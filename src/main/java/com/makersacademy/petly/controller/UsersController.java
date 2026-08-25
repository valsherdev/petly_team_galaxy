package com.makersacademy.petly.controller;

import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class UsersController {
    @Autowired
    UserRepository userRepository;

    @GetMapping("/users/after-login")
    public RedirectView afterLogin() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String username = (String) principal.getAttributes().get("email");
        User user = userRepository
                .findUserByUsername(username)
                .orElseGet(() -> userRepository.save(new User(username)));

        if (user.getRole() == null) {
            return new RedirectView("/users/select-role");
        }

        if ("SERVICE_PROVIDER".equals(user.getRole())) {
            return new RedirectView("/dashboard/provider");
        } else {
            return new RedirectView("/dashboard/owner");
        }
    }

    @GetMapping("/users/select-role")
    public String showSelectRolePage() {
        return "users/select_role";
    }

    @PostMapping("/users/select-role")
    public RedirectView selectRole(@RequestParam("name") String name,
                                   @RequestParam("role") String role) {
        User user = getCurrentUser();
        user.setName(name);
        user.setRole(role);
        userRepository.save(user);

        if ("SERVICE_PROVIDER".equals(user.getRole())) {
            return new RedirectView("/dashboard/provider");
        } else {
            return new RedirectView("/dashboard/owner");
        }
    }

    private User getCurrentUser() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String username = (String) principal.getAttributes().get("email");
        return userRepository.findUserByUsername(username).orElseThrow();
    }
}
