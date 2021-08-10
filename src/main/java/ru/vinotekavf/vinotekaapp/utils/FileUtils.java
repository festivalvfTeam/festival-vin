package ru.vinotekavf.vinotekaapp.utils;

import com.ibm.icu.text.Transliterator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import ru.vinotekavf.vinotekaapp.entities.Position;
import ru.vinotekavf.vinotekaapp.entities.Provider;
import ru.vinotekavf.vinotekaapp.enums.ExcelColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.poi.ss.usermodel.CellType.*;

public class FileUtils {

    public static XSSFCell getValuableXSSFCellFromMerged(XSSFSheet sheet, XSSFCell cell) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i); //Region of merged cells
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
            }
        }
        return cell;
    }

    public static HSSFCell getValuableHSSFCellFromMerged(HSSFSheet sheet, HSSFCell cell) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i); //Region of merged cells
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
            }
        }
        return cell;
    }

    public static String getValueFromCommonPrice(String column, Row row) {

            if (isNotBlank(column)) {
                Cell cell = row.getCell(ExcelColumns.valueOf(column.toUpperCase()).ordinal());
                if (isNotEmpty(cell)) {
                    switch (cell.getCellType()) {
                        case STRING:
                            return cell.getStringCellValue().replaceAll("\\s+", " ");
                        case NUMERIC:
                            return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
                        case FORMULA:
                            switch (cell.getCachedFormulaResultType()) {
                                case STRING:
                                    return cell.getStringCellValue();
                                case NUMERIC:
                                    return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
                                default:
                                    return "";
                            }
                        default:
                            return  "";
                    }
                }
            }
        return "";
    }

    public static String getValueFromXLSXColumn(String column, XSSFRow row) {
        String[] columns = column.split(",");

        for (String str : columns) {
            if (isNotBlank(str)) {
                XSSFCell cell = row.getCell(ExcelColumns.valueOf(str.toUpperCase()).ordinal());
                if (isNotEmpty(cell)) {
                    Cell cellWithValue = getValuableXSSFCellFromMerged(row.getSheet(), cell);
                    if (isNotEmpty(cellWithValue) && cellWithValue.getCellType() == STRING) {
                        return cellWithValue.getStringCellValue().replaceAll("\\s+", " ");
                    } else if (isNotEmpty(cellWithValue) && cellWithValue.getCellType() == NUMERIC) {
                        return BigDecimal.valueOf(cellWithValue.getNumericCellValue()).stripTrailingZeros().toPlainString();
                    } else if (isNotEmpty(cellWithValue) && cellWithValue.getCellType() == FORMULA) {
                        switch (cellWithValue.getCachedFormulaResultType()) {
                            case STRING:
                                return cellWithValue.getRichStringCellValue().getString();
                            case NUMERIC:
                                return BigDecimal.valueOf(cellWithValue.getNumericCellValue()).stripTrailingZeros().toPlainString();
                            default:
                                return "";
                        }
                    } else {
                        return "";
                    }
                }
            }
        }
        return "";
    }

    public static String getValueFromXLSColumn(String column, HSSFRow row) {
        String[] columns = column.split(",");

        for (String str : columns) {
            if (isNotBlank(str)) {
                HSSFCell cell = row.getCell(ExcelColumns.valueOf(str.toUpperCase()).ordinal());
                if (isNotEmpty(cell)) {
                    Cell cellWithValue = getValuableHSSFCellFromMerged(row.getSheet(), cell);
                    if (isNotEmpty(cellWithValue) && cellWithValue.getCellType() == STRING) {
                        return cellWithValue.getStringCellValue().replaceAll("\\s+", " ");
                    } else if (isNotEmpty(cellWithValue) && cellWithValue.getCellType() == NUMERIC) {
                        return BigDecimal.valueOf(cellWithValue.getNumericCellValue()).stripTrailingZeros().toPlainString();
                    } else if (isNotEmpty(cellWithValue) && cellWithValue.getCellType() == FORMULA) {
                        switch (cellWithValue.getCachedFormulaResultType()) {
                            case STRING:
                                return cellWithValue.getRichStringCellValue().getString();
                            case NUMERIC:
                                return BigDecimal.valueOf(cellWithValue.getNumericCellValue()).stripTrailingZeros().toPlainString();
                            default:
                                return "";
                        }
                    } else {
                        return "";
                    }
                }
            }
        }
        return "";
    }

    public static File xlsxToCsv(File excel, File csv) {
        StringBuilder data = new StringBuilder();

        try (FileOutputStream fos = new FileOutputStream(csv);
             FileInputStream fis = new FileInputStream(excel)){

            Workbook workbook = null;

            String ext = FilenameUtils.getExtension(excel.getName());

            if (ext.equalsIgnoreCase("xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (ext.equalsIgnoreCase("xls")) {
                workbook = new HSSFWorkbook(fis);
            }

            Row row;
            Cell cell;

            Sheet sheet = workbook.getSheetAt(0);

            for (Row cells : sheet) {
                row = cells;
                for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                    cell = row.getCell(i);
                    if (ObjectUtils.isNotEmpty(cell)) {
                        switch (cell.getCellType()) {
                            case BOOLEAN:
                                data.append(cell.getBooleanCellValue()).append(";");
                                break;
                            case NUMERIC:
                                data.append(BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString()).append(";");
                                break;
                            case STRING:
                                data.append(cell.getStringCellValue().replaceAll("\\s+", " ")).append(";");
                                break;
                            case BLANK:
                                data.append("" + ";");
                                break;
                            case FORMULA:
                                switch (cell.getCachedFormulaResultType()) {
                                    case STRING:
                                        data.append(cell.getStringCellValue().replaceAll("\\s+", " ")).append(";");
                                        break;
                                    case NUMERIC:
                                        data.append(BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString()).append(";");
                                        break;
                                    default:
                                        data.append(cell).append(";");
                                }
                                break;
                            default:
                                data.append(cell).append(";");
                        }
                    } else {
                        data.append(";");
                    }
                }
                data.append('\n');
            }
            fos.write(data.toString().getBytes());
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        return csv;
    }

    public File writeAllToXLSXFile(List<Provider> providers) throws IOException {
        File file = new File("files/Common price.xlsx");
        file.getParentFile().mkdirs();

        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = createXSSFSheet(book);
        createHeader(book, sheet);
        createCells(book, sheet, providers);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            book.write(outputStream);
            book.close();
        }
        return file;
    }

    public File writeSingleToXLSXFile(Provider provider) throws IOException {
        Transliterator toLatinTrans = Transliterator.getInstance("Cyrillic-Latin");
        File file = new File( "files/" + toLatinTrans.transliterate(provider.getName()) + ".xlsx");
        file.getParentFile().mkdirs();

        List<Provider> providers = new ArrayList<>();
        providers.add(provider);
        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = createXSSFSheet(book);
        createHeader(book, sheet);
        createCells(book, sheet, providers);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            book.write(outputStream);
            book.close();
        }
        return file;
    }

    private static XSSFSheet createXSSFSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("Прайсы");
        for (int i = 0; i < 13; i++) {
            sheet.setColumnWidth(i, 4000);
        }
        return sheet;
    }

    private static void createHeader(XSSFWorkbook workbook, XSSFSheet sheet) {
        XSSFRow header = sheet.createRow(0);

        XSSFCellStyle headerStyle = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(false);
        headerStyle.setFont(font);

        XSSFCell headerCell = header.createCell(0);
        headerCell.setCellValue("Название");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(1);
        headerCell.setCellValue("Имя менеджера");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(2);
        headerCell.setCellValue("Контактный номер для заказа");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(3);
        headerCell.setCellValue("Производитель");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(4);
        headerCell.setCellValue("Артикул");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(5);
        headerCell.setCellValue("Полное наименование");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(6);
        headerCell.setCellValue("Объём, если есть");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(7);
        headerCell.setCellValue("Год (винтаж), если есть");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(8);
        headerCell.setCellValue("Цена");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(9);
        headerCell.setCellValue("Цена по акции или скидке");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(10);
        headerCell.setCellValue("Остаток");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(11);
        headerCell.setCellValue("Артикул ФВ");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(12);
        headerCell.setCellValue("Наименование ФВ");
        headerCell.setCellStyle(headerStyle);
    }

    private static void createCells(XSSFWorkbook workbook, XSSFSheet sheet, List<Provider> providers) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        int counter = 1;

        for (Provider provider : providers) {
            List<Position> sortedList = provider.getPositions().stream()
                .sorted(Comparator.comparing(Position::getProductName))
                .collect(Collectors.toList());
            for (Position position : sortedList) {
                XSSFRow row = sheet.createRow(counter);

                XSSFCell cell = row.createCell(0);
                cell.setCellValue(provider.getName());
                cell.setCellStyle(style);

                cell = row.createCell(1);
                cell.setCellValue(provider.getManagerName());
                cell.setCellStyle(style);

                cell = row.createCell(2);
                cell.setCellValue(provider.getPhone());
                cell.setCellStyle(style);

                cell = row.createCell(3);
                cell.setCellValue(position.getMaker());
                cell.setCellStyle(style);

                cell = row.createCell(4);
                cell.setCellValue(position.getVendorCode());
                cell.setCellStyle(style);

                cell = row.createCell(5);
                cell.setCellValue(position.getProductName());
                cell.setCellStyle(style);

                cell = row.createCell(6);
                cell.setCellValue(position.getVolume());
                cell.setCellStyle(style);

                cell = row.createCell(7);
                cell.setCellValue(position.getReleaseYear());
                cell.setCellStyle(style);

                cell = row.createCell(8);
                cell.setCellValue(position.getPrice());
                cell.setCellStyle(style);

                cell = row.createCell(9);
                cell.setCellValue(position.getPromotionalPrice());
                cell.setCellStyle(style);

                cell = row.createCell(10);
                cell.setCellValue(position.getRemainder());
                cell.setCellStyle(style);

                cell = row.createCell(11);
                cell.setCellValue(position.getFvVendorCode());
                cell.setCellStyle(style);

                cell = row.createCell(12);
                cell.setCellValue(position.getFvProductName());
                cell.setCellStyle(style);

                counter++;
            }
        }
    }
}
