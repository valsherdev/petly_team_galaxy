package com.makersacademy.petly.controller;

import com.makersacademy.petly.model.Service;
import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.ServiceRepository;
import com.makersacademy.petly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.ui.Model;

import java.util.List;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ServiceRepository serviceRepository;

	@RequestMapping(value = "/")
	public RedirectView index() {
		return new RedirectView("/users/after-login");
	}

	@GetMapping("/dashboard/owner")
	public String ownerDashboard(Model model) {
		model.addAttribute("user", getCurrentUser());
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

	private User getCurrentUser() {
		DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
				.getContext()
				.getAuthentication()
				.getPrincipal();
		String username = (String) principal.getAttributes().get("email");
		return userRepository.findUserByUsername(username).orElseThrow();
	}
}


