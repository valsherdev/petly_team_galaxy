package com.makersacademy.petly.controller;

import com.makersacademy.petly.model.Service;
import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.RatingRepository;
import com.makersacademy.petly.repository.ServiceRepository;
import com.makersacademy.petly.repository.UserRepository;
import com.makersacademy.petly.PostcodeService;
import com.makersacademy.petly.DistanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostcodeService postcodeService;

    @Autowired
    private DistanceService distanceService;

    @Autowired
    private RatingRepository ratingRepository;


    @GetMapping("/services")
    public String listServices(@RequestParam(required = false) String type,
                               @RequestParam(defaultValue = "1000") double maxKm, Model model) {

        User user = getCurrentUser();
        List<Service> services = (type == null || type.isBlank())
                ? (List<Service>) serviceRepository.findAll()
                : serviceRepository.findByType(type);

        Map<Long, Double> distances = new HashMap<>();

        if (user.getLatitude() != null && user.getLongitude() != null) {

            for (Service service : services) {

                if (service.getLatitude() != null && service.getLongitude() != null) {

                    double distance = distanceService.calculateDistance(
                            user.getLatitude(),
                            user.getLongitude(),
                            service.getLatitude(),
                            service.getLongitude()
                    );

                    distances.put(service.getId(), distance);
                }
            }

            services.sort((service1, service2) -> {

                Double distance1 = distances.get(service1.getId());
                Double distance2 = distances.get(service2.getId());

                if (distance1 == null && distance2 == null) {
                    return 0;
                }

                if (distance1 == null) {
                    return 1;
                }

                if (distance2 == null) {
                    return -1;
                }

                return Double.compare(distance1, distance2);
            });
        }


        Map<Long, Double> avgRatingByServiceId = new HashMap<>();
        Map<Long, Long> ratingCountByServiceId = new HashMap<>();

        for (Service service: services) {
            long count = ratingRepository.countByServiceId(service.getId());
            if (count > 0) {
                Double averageRating = ratingRepository.findAverageStarsByServiceId(service.getId());
                avgRatingByServiceId.put(service.getId(), averageRating);
                ratingCountByServiceId.put(service.getId(), count);
            }
        }

        services = services.stream()
                .filter(s -> {
                    Double dist = distances.get(s.getId());
                    return dist != null && dist <= maxKm;
                })
                .collect(Collectors.toList());



        model.addAttribute("services", services);
        model.addAttribute("selectedType", type);
        model.addAttribute("user", user);
        model.addAttribute("distances", distances);
        model.addAttribute("avgRatingByServiceId", avgRatingByServiceId);
        model.addAttribute("ratingCountByServiceId", ratingCountByServiceId);
        model.addAttribute("maxKm", maxKm);
        return "services/list";
    }

    @PostMapping("/services/create")
    public RedirectView createService(@ModelAttribute Service service, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();

        boolean validPostcode = postcodeService.setCoordinates(service);

        if (!validPostcode) {
            redirectAttributes.addFlashAttribute(
                    "postcodeError",
                    "Please input a valid postcode"
            );

            redirectAttributes.addFlashAttribute(
                    "newService",
                    service
            );

            return new RedirectView("/dashboard/provider");
        }
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
    public RedirectView updateService(@PathVariable Long id, @ModelAttribute Service updatedService, RedirectAttributes redirectAttributes) {
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
        boolean validPostcode = postcodeService.setCoordinates(service);


        if (!validPostcode) {
            redirectAttributes.addFlashAttribute(
                    "postcodeError",
                    "Please input a valid postcode"
            );

            return new RedirectView("/services/" + id + "/edit");
        }
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
