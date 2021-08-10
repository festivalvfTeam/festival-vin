package ru.vinotekavf.vinotekaapp.controllers;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
import static ru.vinotekavf.vinotekaapp.utils.FileUtils.getValueFromCommonPrice;

@Controller
public class InitDBController {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

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

            Workbook workbook = StreamingReader.builder()
                .rowCacheSize(100)
                .bufferSize(2048)
                .open(storageService.uploadMultipartFile(file));

            Sheet sheet = workbook.getSheetAt(0);
            Provider curProvider = new Provider();
            Position position = new Position();

            for (Row row : sheet) {

                if (getValueFromCommonPrice(provider, row).isEmpty() ||
                    getValueFromCommonPrice(provider, row).equals("Название компании")) {
                    continue;
                } else if (StringUtils.isEmpty(curProvider.getName()) || ObjectUtils.isEmpty(providerService.getProviderByName(getValueFromCommonPrice(provider, row)))) {
                    curProvider = new Provider();
                    curProvider.setName(getValueFromCommonPrice(provider, row));
                    providerService.save(curProvider);
                } else if (curProvider.getName().equals(getValueFromCommonPrice(provider, row))) {
                } else {
                    curProvider = providerService.getProviderByName(getValueFromCommonPrice(provider, row));
                }

                position.setProductName(getValueFromCommonPrice(productName, row));
                position.setVendorCode(getValueFromCommonPrice(vendorCode, row));
                position.setPrice(getValueFromCommonPrice(price, row));
                position.setPromotionalPrice(getValueFromCommonPrice(promotionalPrice, row));
                position.setRemainder(getValueFromCommonPrice(remainder, row));
                position.setVolume(getValueFromCommonPrice(volume, row));
                position.setReleaseYear(getValueFromCommonPrice(releaseYear, row));
                position.setMaker(getValueFromCommonPrice(maker, row));
                position.setFvProductName(getValueFromCommonPrice(fvProductName, row));
                position.setFvVendorCode(getValueFromCommonPrice(fvVendorCode, row));
                position.setLastChange(Calendar.getInstance().getTimeInMillis());

                position.setProvider(curProvider);
                positionList.add(position);
                if (!positionList.isEmpty() && positionList.size() % batchSize == 0) {
                    positionService.saveAll(positionList);
                    positionList.clear();
                }
                position = new Position();
            }
            positionService.saveAll(positionList);
            workbook.close();
        }
        return "redirect:/";
    }

   @GetMapping("testingMatch")
    public String getTestingMatch() {
        return "testingMatch";
    }
}
