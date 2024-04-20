package com.example.pastebin.controllers;

import com.example.pastebin.dto.PasteDTO;
import com.example.pastebin.entity.PasteDetails;
import com.example.pastebin.services.PasteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@Slf4j
public class PasteController {
    private final PasteService pasteService;

    public PasteController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @GetMapping("/")
    public String addPastePage(PasteDTO pasteDTO) {
        return "home";
    }

    @PostMapping("/")
    public String addPaste(PasteDTO pasteDTO, Principal principal) {
        log.info(pasteDTO.getTitle());
        pasteService.savePaste(pasteDTO, principal.getName());
        return "redirect:/pastes";
    }

    @GetMapping("/pastes")
    public String allPastes(Model model, Principal principal) {
        List<PasteDTO> pastes = pasteService.findAll();

        model.addAttribute("pasteDTOs", pastes);
        model.addAttribute("user", principal.getName());

        return "pastes";
    }

    @GetMapping("/pastes/{hash}")
    public String morePasteInfo(@PathVariable int hash, Model model, Principal principal) throws IOException {
        PasteDTO pasteDTO = pasteService.findPasteDTOByHash(hash);
        PasteDetails pasteDetails = pasteService.findPasteDetailsByHash(hash);
        pasteService.addView(pasteDTO);

        model.addAttribute("paste", pasteDTO);
        model.addAttribute("meLiked", pasteService.meLikedCheck(principal.getName(), hash));
        model.addAttribute("likes", (long) pasteDetails.getLikes().size());
        model.addAttribute("date_of_created", pasteDetails.getDateOfCreated());
        model.addAttribute("user", principal.getName());
        return "paste_info";
    }

    @GetMapping("/edit_paste/{hash}")
    public String editPastePage(@PathVariable int hash, Model model) {
        PasteDTO pasteDTO = pasteService.findPasteDTOByHash(hash);

        model.addAttribute("pasteDTO", pasteDTO);
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

    @GetMapping("/pastes/{hash}/like")
    public String addLike(Principal principal,
                          @PathVariable int hash,
                          RedirectAttributes redirectAttributes,
                          @RequestHeader(required = false) String referer) {
        pasteService.addLike(principal.getName(), hash);

        UriComponents components = UriComponentsBuilder.fromHttpUrl(referer).build();

        components.getQueryParams()
                .forEach(redirectAttributes::addAttribute);

        return "redirect:" + components.getPath();
    }
}
