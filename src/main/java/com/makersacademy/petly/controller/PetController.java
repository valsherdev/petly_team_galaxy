package com.makersacademy.petly.controller;

import com.makersacademy.petly.service.ImageStorageService;
import com.makersacademy.petly.model.Pet;
import com.makersacademy.petly.model.User;
import com.makersacademy.petly.repository.PetRepository;
import com.makersacademy.petly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class PetController {
    @Autowired
    PetRepository petRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private ImageStorageService imageStorageService;


    @GetMapping("/my-pets")
    public String getMyPetsPage(Model model) {
        List<Pet> pets = petRepository.findByOwnerId(getCurrentUser().getId());

        if (pets == null) {
            pets = Collections.emptyList();
        }

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
            @RequestParam(value = "image", required = false)
            MultipartFile image) throws IOException {

        Pet pet = new Pet();

          if (!image.isEmpty()) {
            String imageUrl = imageStorageService.upload(image);
            pet.setPhoto(imageUrl);
        }

        pet.setName(petForm.getName());
        pet.setType(petForm.getType());
        pet.setBreed(petForm.getBreed());
        pet.setAge(petForm.getAge());
        pet.setDescription(petForm.getDescription());
        pet.setOwner(getCurrentUser());
        petRepository.save(pet);
        return "redirect:/my-pets";
    }

    @GetMapping("/my-pets/{id}/edit")
    public String editPetProfile(@PathVariable Long id, Model model) {

        Optional<Pet> pet = petRepository.findById(id);
        if (pet.isPresent()) {
            model.addAttribute("pet", pet.get());
            return "pets/edit-pet";
        }
        return "redirect:/my-pets";
    }

    @PostMapping("/my-pets/{id}/edit")
    public String updatePetProfile(@PathVariable Long id,
                                   @ModelAttribute Pet petForm,
                                   @RequestParam(value = "image", required = false)
                                   MultipartFile image) throws IOException {

        Optional<Pet> pet = petRepository.findById(id);
        if (pet.isPresent()) {
            Pet existingPet = pet.get();

        if (!image.isEmpty()) {
            String imageUrl = imageStorageService.upload(image);
            existingPet.setPhoto(imageUrl);
        }

            existingPet.setName(petForm.getName());
            existingPet.setType(petForm.getType());
            existingPet.setBreed(petForm.getBreed());
            existingPet.setAge(petForm.getAge());
            existingPet.setDescription(petForm.getDescription());
            petRepository.save(existingPet);
        }
        return "redirect:/my-pets";
    }

    @PostMapping("/my-pets/{id}/delete")
    public String deletePet(@PathVariable Long id) {

        Pet pet = petRepository.findById(id).orElseThrow();
        petRepository.delete(pet);

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