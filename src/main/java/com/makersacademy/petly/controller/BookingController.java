package com.makersacademy.petly.controller;

import com.makersacademy.petly.model.Booking;
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

import java.time.LocalDateTime;
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

        LocalDateTime start = LocalDateTime.parse(startTime, FORMATTER);
        LocalDateTime end;

        if (service.getDuration() != null) {
            end = start.plus(service.getDuration());
        } else {
            end = LocalDateTime.parse(endTime, FORMATTER);
        }

        List<Booking> existingBookings = bookingRepository.findByServiceId(serviceId);
        boolean isAvailable = true;

        for (Booking booking: existingBookings) {
            boolean overlaps = start.isBefore(booking.getEndTime()) && end.isAfter(booking.getStartTime());
            boolean isConfirmed = booking.getStatus().equals("CONFIRMED");
            if (overlaps && isConfirmed) {
                isAvailable = false;
                break;
            }
        }

        if (!isAvailable) {
            return new RedirectView("/services/" + serviceId + "/book?error=conflict");
        }

        Booking booking = new Booking(pet, service, start, end, owner,service.getProvider());
        bookingRepository.save(booking);
        return new RedirectView("/dashboard/owner/bookings");
    }

    @GetMapping("/dashboard/provider/bookings")
    public String providerBookings(Model model) {
        User provider = getCurrentUser();

        List<Booking> pendingBookings = bookingRepository.findByProviderIdAndStatus(provider.getId(), "PENDING");
        List<Booking> upcomingBookings = bookingRepository.findByProviderIdAndStatusAndEndTimeAfter(provider.getId(), "CONFIRMED", LocalDateTime.now());
        List<Booking> pastBookings = bookingRepository.findByProviderIdAndStatusAndEndTimeBefore(provider.getId(), "CONFIRMED", LocalDateTime.now());

        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", upcomingBookings);
        model.addAttribute("pastBookings", pastBookings);
        return "bookings/provider";
    }


    @GetMapping("/dashboard/owner/bookings")
    public String ownerBookings(Model model) {
        User owner = getCurrentUser();

        List<Booking> pendingBookings = bookingRepository.findByOwnerIdAndStatus(owner.getId(), "PENDING");
        List<Booking> declinedBookings = bookingRepository.findByOwnerIdAndStatus(owner.getId(), "DECLINED");
        List<Booking> confirmedBookings = bookingRepository.findByOwnerIdAndStatusAndEndTimeAfter(owner.getId(), "CONFIRMED", LocalDateTime.now());
        List<Booking> pastBookings = bookingRepository.findByOwnerIdAndStatusAndEndTimeBefore(owner.getId(), "CONFIRMED", LocalDateTime.now());

        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("declinedBookings", declinedBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("pastBookings", pastBookings);
        return "bookings/owner";
    }


    @PostMapping("/bookings/{id}/approve")
    public RedirectView approveBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        User provider = getCurrentUser();

        if (!booking.getProvider().getId().equals(provider.getId())) {
            return new RedirectView("/dashboard/provider/bookings");
        }

        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
        return new RedirectView("/dashboard/provider/bookings");
    }

    @PostMapping("/bookings/{id}/decline")
    public RedirectView declineBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        User provider = getCurrentUser();

        if (!booking.getProvider().getId().equals(provider.getId())) {
            return new RedirectView("/dashboard/provider/bookings");
        }

        booking.setStatus("DECLINED");
        bookingRepository.save(booking);
        return new RedirectView("/dashboard/provider/bookings");
    }

    @PostMapping("/bookings/{id}/cancel")
    public RedirectView cancelBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        User provider = getCurrentUser();

        boolean isOwner = booking.getOwner() != null && booking.getOwner().getId().equals(getCurrentUser().getId());
        boolean isProvider = booking.getProvider() != null && booking.getProvider().getId().equals(getCurrentUser().getId());

        if (!isOwner && !isProvider) {
            return new RedirectView("/dashboard/owner/bookings");
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        return new RedirectView("/dashboard/owner/bookings");
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
