package com.makersacademy.petly.controller;

import com.makersacademy.petly.model.Pet;
import com.makersacademy.petly.model.Service;
import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.BookingRepository;
import com.makersacademy.petly.repository.PetRepository;
import com.makersacademy.petly.repository.ServiceRepository;
import com.makersacademy.petly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.RedirectView;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class BookingController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PetRepository petRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


    @GetMapping("/services/{id}/book")
    public String showBookingForm(@PathVariable Long id,
                                  @RequestParam(required = false) String error,
                                  Model model) {
        Service service = serviceRepository.findById(id).orElseThrow();
        User owner = getCurrentUser();
        List<Pet> myPets = petRepository.findByOwnerId(owner.getId());
        model.addAttribute("service", service);
        model.addAttribute("myPets", myPets);
        model.addAttribute("error", error);
        return "bookings/create";
    }

    @PostMapping("/bookings/create")
    public RedirectView createBooking(@RequestParam Long serviceId,
                                      @RequestParam Long petId,
                                      @RequestParam String startTime,
                                      @RequestParam(required = false) String endTime) {

        Service service = serviceRepository.findById(serviceId).orElseThrow();
        User owner = getCurrentUser();
        Pet pet = petRepository.findById(petId).orElseThrow();
        return new RedirectView("/dashboard/owner");

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
