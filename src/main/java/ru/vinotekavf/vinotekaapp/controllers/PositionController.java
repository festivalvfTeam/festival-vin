package ru.vinotekavf.vinotekaapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vinotekavf.vinotekaapp.services.PositionService;

@Controller
public class PositionController {

    @Autowired
    private PositionService positionService;

    @PostMapping("/positionSearchFilter")
    public String positionSearch(@RequestParam("vendorCodeFilter") String vendorCodeFilter, Model model) {
        model.addAttribute("positions", positionService.findByVendorCode(vendorCodeFilter));
        return "searchPosition";
    }

    @GetMapping("/positionSearch")
    public String getAllPositions(Model model) {
        model.addAttribute("positions", positionService.getAllActive());
        return "searchPosition";
    }
}
