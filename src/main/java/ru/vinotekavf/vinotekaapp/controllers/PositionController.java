package ru.vinotekavf.vinotekaapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vinotekavf.vinotekaapp.entities.Provider;
import ru.vinotekavf.vinotekaapp.services.PositionService;
import ru.vinotekavf.vinotekaapp.services.ProviderService;

@Controller
public class PositionController {

    @Autowired
    private PositionService positionService;

    @Autowired
    private ProviderService providerService;

    @PostMapping("/positionSearchFilter")
    public String positionSearch(@RequestParam("filter") String filter, Model model) {
        model.addAttribute("positions", positionService.findByFilter(filter));
        return "searchPosition";
    }

    @PostMapping("/providerPositionSearchFilter")
    public String providerPositionSearch(@RequestParam("filter") String filter, @RequestParam("providerId") Long providerId, Model model) {
        Provider provider = providerService.getProviderById(providerId);
        model.addAttribute("positions", positionService.findByFilterWithProvider(filter, provider));
        model.addAttribute("provider", provider);
        return "providerPositions";
    }

    @GetMapping("/positionSearch")
    public String getAllPositions(Model model) {
        model.addAttribute("positions", positionService.getAllActive());
        return "searchPosition";
    }
}
