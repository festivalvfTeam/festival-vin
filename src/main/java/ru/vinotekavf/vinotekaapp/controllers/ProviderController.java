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
import ru.vinotekavf.vinotekaapp.services.StorageService;
import ru.vinotekavf.vinotekaapp.utils.ControllerUtils;

import javax.transaction.Transactional;
import java.io.File;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Controller
public class ProviderController {

    @Autowired
    private PositionService positionService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private StorageService storageService;

    @GetMapping("providers/{provider}")
    public String editPositions(@PathVariable Provider provider, Model model) {
        model.addAttribute("provider", provider);
        model.addAttribute("productNameCols", provider.getProductNameCols());
        model.addAttribute("vendorCodeCols", provider.getVendorCodeCols());
        model.addAttribute("priceCols", provider.getPriceCols());
        model.addAttribute("promotionalPriceCols", provider.getPromotionalPriceCols());
        model.addAttribute("remainderCols", provider.getRemainderCols());
        model.addAttribute("volumeCols", provider.getVolumeCols());
        model.addAttribute("releaseYearCols", provider.getReleaseYearCols());
        model.addAttribute("makerCols", provider.getMakerCols());
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
                                @RequestParam("maker") String maker,
                                Model model
    )  {

        if (isNotEmpty(file)) {
            model.addAttribute("provider", provider);
            model.addAttribute("productNameCols", provider.getProductNameCols());
            model.addAttribute("vendorCodeCols", provider.getVendorCodeCols());
            model.addAttribute("priceCols", provider.getPriceCols());
            model.addAttribute("promotionalPriceCols", provider.getPromotionalPriceCols());
            model.addAttribute("remainderCols", provider.getRemainderCols());
            model.addAttribute("volumeCols", provider.getVolumeCols());
            model.addAttribute("releaseYearCols", provider.getReleaseYearCols());
            model.addAttribute("makerCols", provider.getMakerCols());

            Provider providerFromDB = providerService.getProviderById(provider.getId());
            providerFromDB.setProductNameCols(productName);
            providerFromDB.setVendorCodeCols(vendorCode);
            providerFromDB.setPriceCols(price);
            providerFromDB.setPromotionalPriceCols(promotionalPrice);
            providerFromDB.setRemainderCols(remainder);
            providerFromDB.setVolumeCols(volume);
            providerFromDB.setReleaseYearCols(releaseYear);
            providerFromDB.setMakerCols(maker);
            providerService.save(providerFromDB);

            Integer[] productNameCols = ControllerUtils.getIntCols(productName);
            Integer[] vendorCodeCols = ControllerUtils.getIntCols(vendorCode);
            Integer[] priceCols = ControllerUtils.getIntCols(price);
            Integer[] promotionalPriceCols = ControllerUtils.getIntCols(promotionalPrice);
            Integer[] remainderCols = ControllerUtils.getIntCols(remainder);
            Integer[] volumeCols = ControllerUtils.getIntCols(volume);
            Integer[] releaseYearCols = ControllerUtils.getIntCols(releaseYear);
            Integer[] makerCols = ControllerUtils.getIntCols(maker);

            File convertedFile = storageService.convertMultipartFile(file);

            if (convertedFile.getName().contains("xlsx") || convertedFile.getName().contains("xlsm")) {
                positionService.readXLSXAndWriteInDb(convertedFile, provider, vendorCode, productName, volume, releaseYear, price, promotionalPrice,
                        remainder, maker);
            } else if (convertedFile.getName().contains("xls")) {
                positionService.readXLSAndWriteInDb(convertedFile, provider, vendorCode, productName, volume, releaseYear, price, promotionalPrice,
                        remainder, maker);
            } else if (convertedFile.getName().contains(".csv")) {
                positionService.readCSVAndWriteInDb(convertedFile, "windows-1251", provider,
                        vendorCodeCols, productNameCols, volumeCols, releaseYearCols, priceCols, promotionalPriceCols, remainderCols, makerCols);
            }
        }
        return "matchPositions";
    }

    @PostMapping("providers/changeStatus")
    public String removeProvider(@RequestParam Long providerId, Model model) {
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
        model.addAttribute("provider", provider);
        return "providerPositions";
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

    @Transactional
    @GetMapping("/delete/{provider}")
    public String deleteProvider(@PathVariable Provider provider) {
        providerService.delete(provider);
        return "redirect:/disabledProviders";
    }
}
