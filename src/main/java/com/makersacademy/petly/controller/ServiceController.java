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
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/services/{id}/edit")
    public String editService(@PathVariable Long id, Model model) {
        User provider = getCurrentUser();
        Service service = serviceRepository.findById(id).orElseThrow();
        if (!service.getProvider().getId().equals(provider.getId())) {
            return "redirect:/dashboard/provider";
        }
        model.addAttribute("service", service);
        return "services/edit";
    }

    @PostMapping("/services/{id}/update")
    public RedirectView updateService(@PathVariable Long id, @ModelAttribute Service updatedService) {
        Service service = serviceRepository.findById(id).orElseThrow();
        User provider = getCurrentUser();
        if (!service.getProvider().getId().equals(provider.getId())) {
            return new RedirectView("/dashboard/provider");
        }

        service.setName(updatedService.getName());
        service.setType(updatedService.getType());
        service.setPrice(updatedService.getPrice());
        service.setPriceUnit(updatedService.getPriceUnit());
        service.setLocation(updatedService.getLocation());
        service.setDescription(updatedService.getDescription());
        service.setDurationMinutes(updatedService.getDurationMinutes());
        serviceRepository.save(service);

        return new RedirectView("/dashboard/provider");
    }

    @PostMapping("/services/{id}/delete")
    public RedirectView deleteService(@PathVariable Long id) {
        Service service = serviceRepository.findById(id).orElseThrow();
        User user = getCurrentUser();

        if (!service.getProvider().getId().equals(user.getId())) {
            return new RedirectView("/dashboard/provider");
        }

        serviceRepository.delete(service);
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
