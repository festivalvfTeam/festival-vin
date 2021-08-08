package ru.vinotekavf.vinotekaapp.controllers;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.vinotekavf.vinotekaapp.entities.Position;
import ru.vinotekavf.vinotekaapp.entities.Provider;
import ru.vinotekavf.vinotekaapp.services.PositionService;
import ru.vinotekavf.vinotekaapp.services.ProviderService;
import ru.vinotekavf.vinotekaapp.services.StorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static ru.vinotekavf.vinotekaapp.utils.FileUtils.getValueFromXLSXCommonPrice;

@Controller
public class InitDBController {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    ProviderService providerService;

    @Autowired
    PositionService positionService;

    @Autowired
    StorageService storageService;

    @PostMapping("testingMatch")
    public String matchTestFile(@RequestParam("file") MultipartFile file,
        @RequestParam("provider") String provider,
        @RequestParam("phone") String phone,
        @RequestParam("managerName") String managerName,
        @RequestParam("productName") String productName,
        @RequestParam("vendorCode") String vendorCode,
        @RequestParam("price") String price,
        @RequestParam("promotionalPrice") String promotionalPrice,
        @RequestParam("remainder") String remainder,
        @RequestParam("volume") String volume,
        @RequestParam("releaseYear") String releaseYear,
        @RequestParam("maker") String maker,
        @RequestParam("fvVendorCode") String fvVendorCode,
        @RequestParam("fvProductName") String fvProductName
    ) throws IOException {

        if (isNotEmpty(file.getOriginalFilename())) {

            List<Position> positionList = new ArrayList<>();

            if (file.getOriginalFilename().contains("xlsx") || file.getOriginalFilename().contains("xlsm")) {
                Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)
                    .bufferSize(2048)
                    .open(storageService.uploadMultipartFile(file));

                Sheet sheet = workbook.getSheetAt(0);
                Provider curProvider = new Provider();
                Position position = new Position();

                for (Row row : sheet) {

                    if (getValueFromXLSXCommonPrice(provider, row).isEmpty() ||
                        getValueFromXLSXCommonPrice(provider, row).equals("Название компании")) {
                        continue;
                    }

                    if (ObjectUtils.isEmpty(providerService.getProviderByName(getValueFromXLSXCommonPrice(provider, row)))) {
                        curProvider.setName(getValueFromXLSXCommonPrice(provider, row));
                        providerService.save(curProvider);
                    } else {
                        curProvider = providerService.getProviderByName(getValueFromXLSXCommonPrice(provider, row));
                    }

                    position.setProductName(getValueFromXLSXCommonPrice(productName, row));
                    position.setVendorCode(getValueFromXLSXCommonPrice(vendorCode, row));
                    position.setPrice(getValueFromXLSXCommonPrice(price, row));
                    position.setPromotionalPrice(getValueFromXLSXCommonPrice(promotionalPrice, row));
                    position.setRemainder(getValueFromXLSXCommonPrice(remainder, row));
                    position.setVolume(getValueFromXLSXCommonPrice(volume, row));
                    position.setReleaseYear(getValueFromXLSXCommonPrice(releaseYear, row));
                    position.setMaker(getValueFromXLSXCommonPrice(maker, row));
                    position.setFvProductName(getValueFromXLSXCommonPrice(fvProductName, row));
                    position.setFvVendorCode(getValueFromXLSXCommonPrice(fvVendorCode, row));
                    position.setLastChange(Calendar.getInstance().getTimeInMillis());

                    Provider provider1 = providerService.getProviderByName(curProvider.getName());
                    position.setProvider(provider1);
                    positionList.add(position);
                    curProvider = new Provider();
                    position = new Position();
                }
                positionService.saveAll(positionList);
                workbook.close();
            }
        }
        return "redirect:/";
    }

   @GetMapping("testingMatch")
    public String getTestingMatch() {
        return "testingMatch";
    }
}
