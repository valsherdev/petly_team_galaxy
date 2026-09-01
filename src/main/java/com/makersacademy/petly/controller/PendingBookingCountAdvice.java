package com.makersacademy.petly.controller;


import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.BookingRepository;
import com.makersacademy.petly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class PendingBookingCountAdvice {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @ModelAttribute("pendingBookingCount")
    public long pendingBookingCount() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof DefaultOidcUser)) {
            return 0;
        }

        DefaultOidcUser principal = (DefaultOidcUser) authentication.getPrincipal();
        String username = (String) principal.getAttributes().get("email");
        User currentUser = userRepository.findUserByUsername(username).orElse(null);


        if (currentUser == null || !"SERVICE_PROVIDER".equals(currentUser.getRole())) {
            return 0;
        }

        return bookingRepository.countByProviderIdAndStatus(currentUser.getId(), "PENDING");
    }
}
