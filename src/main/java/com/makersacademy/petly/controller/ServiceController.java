package com.makersacademy.petly.controller;

import com.makersacademy.petly.model.Service;
import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.ServiceRepository;
import com.makersacademy.petly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/services")
    public String listServices(@RequestParam(required = false) String type, Model model) {
        List<Service> services = (type == null || type.isBlank())
                ? (List<Service>) serviceRepository.findAll()
                : serviceRepository.findByType(type);

        model.addAttribute("services", services);
        model.addAttribute("selectedType", type);
        return "services/list";
    }

    @PostMapping("/services/create")
    public RedirectView createService(@ModelAttribute Service service) {
        User user = getCurrentUser();
        service.setProvider(user);
        serviceRepository.save(service);
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
