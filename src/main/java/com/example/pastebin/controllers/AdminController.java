package com.example.pastebin.controllers;

import com.example.pastebin.dto.UserDTO;
import com.example.pastebin.repositories.UserRepository;
import com.example.pastebin.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminController {
    private final UserService userService;
    private final UserRepository userRepository;

    public AdminController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @GetMapping("/edit_user/{id}")
    public String editStudentPage(@PathVariable(value = "id") Long id, Model model) {
        UserDTO userDTO = userService.findUserById(id);

        if (userDTO == null) {
            return "redirect:/users";
        }

        model.addAttribute("user", userDTO);
        model.addAttribute("id", id);

        return "edit_user";
    }

    @PostMapping("/edit_user/{id}")
    public String editStudent(UserDTO userDTO) {
        userService.editUser(userDTO);
        return "redirect:/users";
    }

    @GetMapping("/delete_user/{id}")
    public String deleteUser(@PathVariable(value = "id") Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }
}
