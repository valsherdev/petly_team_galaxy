package com.makersacademy.petly.controller;

import com.makersacademy.petly.model.Pet;
import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.PetRepository;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.List;

@Controller
public class PetController {
    @Autowired
    PetRepository petRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/my-pets")
    public String getMyPetsPage(Model model) {
        List<Pet> pets = petRepository.findByOwnerId(getCurrentUser().getId());

        model.addAttribute("pets", pets);
        return "pets/my-pets";
    }

    @GetMapping("/my-pets/add")
    public ModelAndView getPetAddForm() {
        Pet pet = new Pet();
        ModelAndView modelAndView = new ModelAndView("pets/add-pet");
        modelAndView.addObject("pet", pet);
        return modelAndView;
    }

    @PostMapping("/my-pets/add")
    public String savePetProfile(
            @ModelAttribute("pet") Pet petForm,
            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile) {


        Pet pet = new Pet();

//            if (imageFile != null && !imageFile.isEmpty()) {
//                byte[] compressedImage =
//                        imageService.compressImage(imageFile);
//                profile.setProfilePicture(compressedImage);
//            }
        pet.setName(petForm.getName());
        pet.setType(petForm.getType());
        pet.setBreed(petForm.getBreed());
        pet.setAge(petForm.getAge());
        pet.setDescription(petForm.getDescription());
        pet.setOwner(getCurrentUser());
        petRepository.save(pet);
        return "redirect:/my-pets";
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