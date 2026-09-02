package com.makersacademy.petly.controller;


import com.makersacademy.petly.model.Booking;
import com.makersacademy.petly.model.Rating;
import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.BookingRepository;
import com.makersacademy.petly.repository.RatingRepository;
import com.makersacademy.petly.repository.ServiceRepository;
import com.makersacademy.petly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;

@Controller
public class RatingController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RatingRepository ratingRepository;



    @GetMapping("/bookings/{id}/rate")
    public String showRatingForm(@PathVariable Long id,
                                 Model model) {

        User currentUser = getCurrentUser();
        Booking booking = bookingRepository.findById(id).orElseThrow();

        if (!isEligibleToRate(booking, currentUser)) {
            return "redirect:/dashboard/owner/bookings";
        }

        model.addAttribute("booking", booking);
        return "ratings/create";
    }


    @PostMapping("/bookings/{id}/rate")
    public RedirectView createRating(@PathVariable Long id,
                                     @RequestParam Integer stars) {

        Booking booking = bookingRepository.findById(id).orElseThrow();
        User currentUser = getCurrentUser();

        if (!isEligibleToRate(booking, currentUser)) {
            return new RedirectView("/dashboard/owner/bookings");
        }

        Rating rating = new Rating(booking, booking.getService(), currentUser, stars);
        ratingRepository.save(rating);
        return new RedirectView("/dashboard/owner/bookings");
    }



    private boolean isEligibleToRate(Booking booking, User currentUser) {
        if (!booking.getOwner().getId().equals(currentUser.getId())) {
            return false;
        }
        if (!"CONFIRMED".equals(booking.getStatus())) {
            return false;
        }
        if (booking.getEndTime().isAfter(LocalDateTime.now())) {
            return false;
        }
        return !ratingRepository.existsByBookingId(booking.getId());
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
