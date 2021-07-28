package ru.vinotekavf.vinotekaapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PositionProviderDto {
    private String providerName;
    private String providerPhone;

    private String maker;
    private String vendorCode;//артикул
    private String productName;
    private String volume;//объем
    private String releaseYear;
    private String price;
    private String remainder;//остаток

    //артикул в Festival Vin
    private String fvVendorCode;
    private String fvProductName;

    public static String[] getColumnsNames() {
        return Arrays.stream(PositionProviderDto.class.getDeclaredFields()).map(Field::getName).toArray(String[]::new);
    }

    public String[] toStrArray() {
        return Stream.of(providerName, providerPhone, maker, vendorCode, productName, volume, releaseYear, price, remainder, fvVendorCode, fvProductName)
                .map(value -> {
                    if (value == null)
                        return "";
                    return value;
                }
                ).toArray(String[]::new);
    }
}
