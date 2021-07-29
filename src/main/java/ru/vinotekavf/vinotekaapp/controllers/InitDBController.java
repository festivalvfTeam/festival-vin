package ru.vinotekavf.vinotekaapp.controllers;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import ru.vinotekavf.vinotekaapp.utils.FileUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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
    ) throws IOException, InvalidFormatException {

        if (isNotEmpty(file.getOriginalFilename())) {

            if (file.getOriginalFilename().contains("xlsx") || file.getOriginalFilename().contains("xlsm")) {
                XSSFWorkbook book = new XSSFWorkbook(storageService.uploadMultipartFile(file));
                XSSFSheet sheet = book.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.rowIterator();
                while (rowIterator.hasNext()) {
                    XSSFRow row = (XSSFRow) rowIterator.next();
                    Provider curProvider = new Provider();
                    Position position = new Position();

                    curProvider.setName(FileUtils.getValueFromXLSXColumn(provider, row));
                    if (curProvider.getName().isEmpty() || curProvider.getName().equals("Название компании")) {
                        continue;
                    }
                    curProvider.setPhone(FileUtils.getValueFromXLSXColumn(phone, row));
                    curProvider.setManagerName(FileUtils.getValueFromXLSXColumn(managerName, row));

                    position.setProductName(FileUtils.getValueFromXLSXColumn(productName, row));
                    position.setVendorCode(FileUtils.getValueFromXLSXColumn(vendorCode, row));
                    position.setPrice(FileUtils.getValueFromXLSXColumn(price, row));
                    position.setPromotionalPrice(FileUtils.getValueFromXLSXColumn(promotionalPrice, row));
                    position.setRemainder(FileUtils.getValueFromXLSXColumn(remainder, row));
                    position.setVolume(FileUtils.getValueFromXLSXColumn(volume, row));
                    position.setReleaseYear(FileUtils.getValueFromXLSXColumn(releaseYear, row));
                    position.setMaker(FileUtils.getValueFromXLSXColumn(maker, row));
                    position.setFvProductName(FileUtils.getValueFromXLSXColumn(fvProductName, row));
                    position.setFvVendorCode(FileUtils.getValueFromXLSXColumn(fvVendorCode, row));
                    position.setLastChange(Calendar.getInstance().getTimeInMillis());

                    providerService.save(curProvider);
                    Provider provider1 = providerService.getProviderByName(curProvider.getName());
                    position.setProvider(provider1);
                    positionService.save(position);
                }
                book.close();
            }
        }
        return "redirect:/";
    }

   @GetMapping("testingMatch")
    public String getTestingMatch() {
        return "testingMatch";
    }
}
