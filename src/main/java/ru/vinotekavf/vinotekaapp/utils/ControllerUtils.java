package ru.vinotekavf.vinotekaapp.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;
import ru.vinotekavf.vinotekaapp.enums.ExcelColumns;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ControllerUtils {

    public static Map<String, String> getErrors(BindingResult bindingResult) {
        Collector<FieldError, ?, Map<String, String>> collector = Collectors.toMap(
            fieldError -> fieldError.getField() + "Error",
            FieldError::getDefaultMessage
        );
        return bindingResult.getFieldErrors().stream().collect(collector);
    }

    public static Integer[] getIntCols(String columns) {
        return Arrays.stream(columns.split(",")).map(str ->
                {
                    if (StringUtils.isNotBlank(str))
                        return ExcelColumns.valueOf(str.toUpperCase()).ordinal();
                    return -1;
                }
        ).toArray(Integer[]::new);
    }

    public static Path writeInDirectoryAndGetPath(MultipartFile file, String uploadPath) {
        Path filepath = Paths.get(uploadPath);
        if (!filepath.toFile().exists()) {
            filepath.toFile().mkdir();
        }
        filepath = Paths.get(uploadPath, file.getOriginalFilename());
        try {
            file.transferTo(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filepath;
    }
}
