package ru.vinotekavf.vinotekaapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vinotekavf.vinotekaapp.entities.Provider;
import ru.vinotekavf.vinotekaapp.services.PositionService;
import ru.vinotekavf.vinotekaapp.services.ProviderService;
import ru.vinotekavf.vinotekaapp.utils.ControllerUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Controller
public class ProviderController {
    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private PositionService positionService;

    @Autowired
    private ProviderService providerService;

    @GetMapping("providers/{provider}")
    public String editPositions(@PathVariable Provider provider, Model model) {
        model.addAttribute("provider", provider);
        return "matchPositions";
    }

    @PostMapping("providers/{provider}")
    public String matchProducts(@PathVariable Provider provider,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam("productName") String productName,
                                @RequestParam("vendorCode") String vendorCode,
                                @RequestParam("price") String price,
                                @RequestParam("promotionalPrice") String promotionalPrice,
                                @RequestParam("remainder") String remainder,
                                @RequestParam("volume") String volume,
                                @RequestParam("releaseYear") String releaseYear,
                                @RequestParam("maker") String maker
    ) throws IOException {

        if (isNotEmpty(file)) {

            Integer[] productNameCols = ControllerUtils.getIntCols(productName);
            Integer[] vendorCodeCols = ControllerUtils.getIntCols(vendorCode);
            Integer[] priceCols = ControllerUtils.getIntCols(price);
            Integer[] promotionalPriceCols = ControllerUtils.getIntCols(promotionalPrice);
            Integer[] remainderCols = ControllerUtils.getIntCols(remainder);
            Integer[] volumeCols = ControllerUtils.getIntCols(volume);
            Integer[] releaseYearCols = ControllerUtils.getIntCols(releaseYear);
            Integer[] makerCols = ControllerUtils.getIntCols(maker);

            Path path = ControllerUtils.writeInDirectoryAndGetPath(file, uploadPath);

            file.transferTo(new File(uploadPath + "/" + file.getOriginalFilename()));
            if (file.getOriginalFilename().contains("xlsx") || file.getOriginalFilename().contains("xlsm")) {
                positionService.readXLSXAndWriteInDb(path.toString(), provider, vendorCode, productName, volume, releaseYear, price, promotionalPrice,
                        remainder, maker);
            } else if (file.getOriginalFilename().contains("xls")) {
                positionService.readXLSAndWriteInDb(path.toString(), provider, vendorCode, productName, volume, releaseYear, price, promotionalPrice,
                        remainder, maker);
            } else if (file.getOriginalFilename().contains(".csv")) {
                positionService.readCSVAndWriteInDb(path.toString(), "windows-1251", provider,
                        vendorCodeCols, productNameCols, volumeCols, releaseYearCols, priceCols, promotionalPriceCols, remainderCols, makerCols);
            }
        }
        return "matchPositions";
    }

    @PostMapping("providers/changeStatus")
    public String deleteProvider(@RequestParam Long providerId, Model model) {
        Provider provider = providerService.changeProviderStatus(providerId);
        if (!provider.isActive()) {
            model.addAttribute("providers", providerService.getAllActive());
            return "main";
        } else {
            model.addAttribute("providers", providerService.getAllDisabled());
            return "disabledProviders";
        }
    }

    @GetMapping("providers/allPositions/{provider}")
    public String getProviderPositions(@PathVariable Provider provider, Model model) {
        model.addAttribute("positions", positionService.findAllByProvider(provider));
        return "searchPosition";
    }

    @GetMapping("/newProvider")
    public String regNewProvider(){
        return "newProvider";
    }

    @PostMapping("/newProvider")
    public String addNewProvider(@RequestParam("providerName") String providerName,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("managerName") String managerName,
                                 Model model
    ) {
        providerService.save(new Provider(providerName, phone, managerName));
        model.addAttribute("providers", providerService.getAllActive());
        return "main";
    }

    @PostMapping("/providerSearch")
    public String searchProvider(@RequestParam("searchRequest") String searchRequest, Model model) {
        model.addAttribute("providers", providerService.getProvidersWithFilter(searchRequest));
        return "main";
    }

    @GetMapping("/disabledProviders")
    public String getDisabled(Model model) {
        model.addAttribute("providers", providerService.getAllDisabled());
        return "disabledProviders";
    }

    @GetMapping("/delete/{provider}")
    public String deleteProvider(@PathVariable Provider provider) {
        providerService.delete(provider);
        return "redirect:/disabledProviders";
    }
}
