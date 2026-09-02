package com.makersacademy.petly.controller;

import com.makersacademy.petly.PostcodeService;
import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostcodeService postcodeService;

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        return "profile/my-profile";
    }

    @PostMapping("/profile/location")
    public RedirectView updateLocation(
            @RequestParam String location,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();

        currentUser.setLocation(location);

        boolean validPostcode = postcodeService.setCoordinates(currentUser);

        if (!validPostcode) {
            redirectAttributes.addFlashAttribute(
                    "postcodeError",
                    "Please input a valid postcode"
            );

            return new RedirectView("/profile");
        }

        userRepository.save(currentUser);

        redirectAttributes.addFlashAttribute(
                "locationSuccess",
                "Location updated successfully!"
        );

        return new RedirectView("/profile");
    }

    @PostMapping("/profile/name")
    public RedirectView updateName(@RequestParam String name, RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();

        currentUser.setName(name);
        userRepository.save(currentUser);

        redirectAttributes.addFlashAttribute(
                "nameSuccess",
                "Name updated successfully!"
        );

        return new RedirectView("/profile");
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
