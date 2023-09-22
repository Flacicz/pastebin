package com.example.pastebin.controllers;

import com.example.pastebin.dto.PasteDTO;
import com.example.pastebin.mappers.PasteMapper;
import com.example.pastebin.repositories.PasteDetailsRepository;
import com.example.pastebin.services.PasteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class PasteController {
    private final PasteService pasteService;

    public PasteController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @PostMapping("/add_paste")
    public String addPaste(PasteDTO pasteDTO, Principal principal) {
        pasteService.saveText(pasteDTO, principal.getName());
        return "redirect:/pastes";
    }

    @GetMapping("pastes")
    public String allPastes(Model model) {
        model.addAttribute("pastes", pasteService.findAll());
        return "pastes";
    }
}
