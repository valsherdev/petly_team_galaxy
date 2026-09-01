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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.ui.Model;

import java.util.List;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ServiceRepository serviceRepository;

	@Autowired
	private PetRepository petRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@RequestMapping(value = "/")
	public RedirectView index() {
		return new RedirectView("/users/after-login");
	}

	@GetMapping("/dashboard/owner")
	public String ownerDashboard(Model model) {
		List<Pet> pets = petRepository.findByOwnerId(getCurrentUser().getId());
		Iterable<Service> services = serviceRepository.findAll();
		List<Booking> bookings = bookingRepository.findByOwnerId(getCurrentUser().getId());
		model.addAttribute("user", getCurrentUser());
		model.addAttribute("pets", pets);
		model.addAttribute("services", services);
		model.addAttribute("bookings", bookings);
		return "dashboards/owner";
	}

	@GetMapping("/dashboard/provider")
	public String providerDashboard(Model model) {
		User user = getCurrentUser();
		List<Service> services = serviceRepository.findByProvider(user);
		model.addAttribute("user", user);
		model.addAttribute("services", services);
		if (!model.containsAttribute("newService")) {
		model.addAttribute("newService", new Service());}
		return "dashboards/provider";
	}

	@PostMapping("/profile/provider_name")
	public RedirectView updateProviderName(@RequestParam String name, RedirectAttributes redirectAttributes) {

		User currentUser = getCurrentUser();

		currentUser.setName(name);
		userRepository.save(currentUser);

		redirectAttributes.addFlashAttribute(
				"providerNameSuccess",
				"Name updated successfully!"
		);

		return new RedirectView("/dashboard/provider");
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


