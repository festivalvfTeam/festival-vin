package ru.vinotekavf.vinotekaapp.services;

import au.com.bytecode.opencsv.*;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.vinotekavf.vinotekaapp.dto.PositionProviderDto;
import ru.vinotekavf.vinotekaapp.entities.Position;
import ru.vinotekavf.vinotekaapp.entities.Provider;
import ru.vinotekavf.vinotekaapp.repos.PositionRepository;
import ru.vinotekavf.vinotekaapp.repos.ProviderRepository;
import ru.vinotekavf.vinotekaapp.utils.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

@Service
public class PositionService {
    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Value("${spring.datasource.url}")
    private String URL_DB;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String QUERY = "select pr.name, pr.phone, po.maker, po.vendor_code, po.product_name, " +
            "po.volume, po.release_year, po.price, po.remainder, po.fv_vendor_code, po.fv_product_name " +
            "from provider pr " +
            "join posit po on pr.id = po.provider_id";

    public void save(Position position) {
        List<Position> positionList = positionRepository.findPositionList(position.getProvider(), position.getVendorCode(), position.getProductName(),
            position.getVolume(), position.getReleaseYear());
        positionList.sort(Comparator.comparing(Position::getLastChange));
        Long curTime = Calendar.getInstance().getTimeInMillis();

         if (!positionList.isEmpty() && curTime - positionList.get(0).getLastChange() > 900000) {
             Position positionFromDb = positionList.get(0);

            positionFromDb.setVendorCode(position.getVendorCode());
            positionFromDb.setProductName(position.getProductName());
            positionFromDb.setVolume(position.getVolume());
            positionFromDb.setReleaseYear(position.getReleaseYear());
            positionFromDb.setPrice(position.getPrice());
            positionFromDb.setPromotionalPrice(position.getPromotionalPrice());
            positionFromDb.setRemainder(position.getRemainder());
            positionFromDb.setMaker(position.getMaker());
            positionFromDb.setLastChange(Calendar.getInstance().getTimeInMillis());
            positionRepository.save(positionFromDb);
        } else {
            positionRepository.save(position);
        }
    }

    public List<Position> findAll() {
        return positionRepository.findAll();
    }

    public List<Position> getAllActive() {
        return positionRepository.findAllByIsActiveTrueOrderByProviderAscProductNameAsc();
    }

    public List<Position> findAllByProvider (Provider provider) {
        return positionRepository.findAllByProvider(provider);
    }

    public void writeAllPositionsInCSV() {
        String CSV_FILE_PATH = "src/main/resources/files/data.csv";
        try (Connection connection = DriverManager.getConnection(URL_DB, "postgres", "5842");
             Statement stmt = connection.createStatement();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(CSV_FILE_PATH), "windows-1251"), ';'))
        {
            ResultSet myResultSet = stmt.executeQuery(QUERY);
            writer.writeAll(myResultSet, true);
        } catch (SQLException | IOException exception) {
            logger.error(exception.getMessage());
        }
    }

    public void writeAllPositionsInCSVByDTO() {
        String CSV_FILE_PATH = "src/main/resources/files/data.csv";
        List<PositionProviderDto> list = positionRepository.getJoinInformation();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(CSV_FILE_PATH), "windows-1251"), ';')) {
            writer.writeNext(PositionProviderDto.getColumnsNames());
            list.forEach(x -> writer.writeNext(x.toStrArray()));
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }
    }

    private ColumnPositionMappingStrategy<PositionProviderDto> setColumnMapping() {
        ColumnPositionMappingStrategy<PositionProviderDto> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(PositionProviderDto.class);
        String[] columns = new String[] {"providerName", "providerPhone", "maker", "vendorCode"
                , "productName", "volume", "releaseYear", "price", "remainder", "fvVendorCode", "fvProductName"};
        strategy.setColumnMapping(columns);
        return strategy;
    }

    public void readCSVAndWriteInDb(File file, String encoding, Provider provider, Integer[] vendorCodeIndexes, Integer[] productNameIndexes, Integer[] volumeIndexes,
                                    Integer[] releaseYearIndexes, Integer[] priceIndexes, Integer[] promotionalPriceIndexes, Integer[] remainderIndexes, Integer[] makerIndexes) {
        try (CSVReader csvReader = new CSVReader(new FileReader(file, Charset.forName(encoding)), ';', '\n', '"')) {
            String[] currentRow = csvReader.readNext();
            while (currentRow != null) {
                Position position = new Position();
                position.setProvider(provider);
                for (int index : vendorCodeIndexes) {
                    if (index != -1)
                        position.addVendorCode(currentRow[index]);
                }
                for (int index : productNameIndexes) {
                    if (index != -1)
                        position.addProductName(currentRow[index]);
                }
                for (int index : volumeIndexes) {
                    if (index != -1)
                        position.addVolume(currentRow[index]);
                }
                for (int index : releaseYearIndexes) {
                    if (index != -1)
                        position.addReleaseYear(currentRow[index]);
                }
                for (int index : priceIndexes) {
                    if (index != -1)
                        position.addPrice(currentRow[index]);
                }
                for (int index : promotionalPriceIndexes) {
                    if (index != -1)
                        position.addPromotionalPrice(currentRow[index]);
                }
                for (int index : remainderIndexes) {
                    if (index != -1)
                        position.addRemainder(currentRow[index]);
                }
                for (int index : makerIndexes) {
                    if (index != -1)
                        position.addMaker(currentRow[index]);
                }

                position.setLastChange(Calendar.getInstance().getTimeInMillis());

                if (!position.getPrice().isEmpty() && !position.getProductName().isEmpty() && NumberUtils.isParsable(position.getPrice().replaceAll("\\s+","")))
                    save(position);

                currentRow = csvReader.readNext();
            }
        } catch (FileNotFoundException exception) {
            logger.info("File not found");
            logger.error(exception.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readXLSXAndWriteInDb(File file, Provider provider, String vendorCode, String productName, String volume, String releaseYear,
        String price, String promotionalPrice, String remainder, String maker) {
        try (XSSFWorkbook book = new XSSFWorkbook(new FileInputStream(file))) {
            XSSFSheet sheet = book.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()) {
                XSSFRow row = (XSSFRow) rowIterator.next();
                Position position = new Position();
                position.setProvider(provider);

                position.setProductName(FileUtils.getValueFromXLSXColumn(productName, row));
                position.setVendorCode(FileUtils.getValueFromXLSXColumn(vendorCode, row));
                position.setPrice(FileUtils.getValueFromXLSXColumn(price, row));
                position.setPromotionalPrice(FileUtils.getValueFromXLSXColumn(promotionalPrice, row));
                position.setRemainder(FileUtils.getValueFromXLSXColumn(remainder, row));
                position.setVolume(FileUtils.getValueFromXLSXColumn(volume, row));
                position.setReleaseYear(FileUtils.getValueFromXLSXColumn(releaseYear, row));
                position.setMaker(FileUtils.getValueFromXLSXColumn(maker, row));
                position.setLastChange(Calendar.getInstance().getTimeInMillis());

                if (!position.getPrice().isEmpty() && !position.getProductName().isEmpty() && NumberUtils.isParsable(position.getPrice().replaceAll("\\s+","")))
                    save(position);
            }
        } catch (FileNotFoundException exception) {
            logger.info("File not found");
            logger.error(exception.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readXLSAndWriteInDb(File file, Provider provider, String vendorCode, String productName, String volume, String releaseYear,
        String price, String promotionalPrice, String remainder, String maker) {
        try (HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(file))) {
            HSSFSheet sheet = book.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()) {
                HSSFRow row = (HSSFRow) rowIterator.next();
                Position position = new Position();
                position.setProvider(provider);

                position.setProductName(FileUtils.getValueFromXLSColumn(productName, row));
                position.setVendorCode(FileUtils.getValueFromXLSColumn(vendorCode, row));
                position.setPrice(FileUtils.getValueFromXLSColumn(price, row));
                position.setPromotionalPrice(FileUtils.getValueFromXLSColumn(promotionalPrice, row));
                position.setRemainder(FileUtils.getValueFromXLSColumn(remainder, row));
                position.setVolume(FileUtils.getValueFromXLSColumn(volume, row));
                position.setReleaseYear(FileUtils.getValueFromXLSColumn(releaseYear, row));
                position.setMaker(FileUtils.getValueFromXLSColumn(maker, row));
                position.setLastChange(Calendar.getInstance().getTimeInMillis());

                if (!position.getPrice().isEmpty() && !position.getProductName().isEmpty() && NumberUtils.isParsable(position.getPrice().replaceAll("\\s+","")))
                    save(position);
            }
        } catch (FileNotFoundException exception) {
            logger.info("File not found");
            logger.error(exception.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Position> findByFilter(String query) {
        return positionRepository.findByQuery(query);
    }

    public List<Position> findByFilterWithProvider(String query, Provider provider) {
        return positionRepository.findByQueryAndProvider(query, provider);
    }
}
