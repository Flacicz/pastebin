package com.example.pastebin.controllers;

import com.example.pastebin.dto.PasteDTO;
import com.example.pastebin.services.PasteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.security.Principal;

@Controller
public class PasteController {
    private final PasteService pasteService;

    public PasteController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @GetMapping("/pastes")
    public String allPastes(Model model) {
        model.addAttribute("pastes", pasteService.findAll());
        return "pastes";
    }

    @GetMapping("/pastes/{hash}")
    public String morePasteInfo(@PathVariable int hash, Model model) {
        model.addAttribute("paste", pasteService.findPasteByHash(hash));
        model.addAttribute("paste_hash", hash);
        return "paste_info";
    }

    @GetMapping("/add_paste")
    public String addPastePage() {
        return "home";
    }

    @PostMapping("/add_paste")
    public String addPaste(PasteDTO pasteDTO, Principal principal) {
        pasteService.saveText(pasteDTO, principal.getName());
        return "redirect:/pastes";
    }

    @GetMapping("/edit_paste/{hash}")
    public String editPastePage(@PathVariable int hash, Model model) {
        PasteDTO pasteDTO = pasteService.findPasteByHash(hash);
        pasteService.saveOldTitle(pasteDTO.getTitle());
        model.addAttribute("paste", pasteDTO);
        return "edit_paste";
    }

    @PostMapping("/edit_paste/{hash}")
    public String editPaste(PasteDTO pasteDTO) throws IOException {
        pasteService.editPaste(pasteDTO);
        return "redirect:/pastes";
    }

    @GetMapping("/delete_paste/{hash}")
    public String deletePaste(@PathVariable int hash) {
        pasteService.deletePaste(hash);
        return "redirect:/pastes";
    }
}
