package ru.vinotekavf.vinotekaapp.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude="positions")
@Entity
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    private Set<Position> positions;

    private String name;
    private String phone;
    private String managerName;
    private boolean isActive;

    //поля запоминающие колонки файлов
    private String productNameCols;
    private String vendorCodeCols;
    private String priceCols;
    private String promotionalPriceCols;
    private String remainderCols;
    private String volumeCols;
    private String releaseYearCols;
    private String makerCols;

    public Provider() {
        isActive = true;
    }

    public Provider(String name, String phone, String managerName) {
        this.name = name;
        this.phone = phone;
        this.managerName = managerName;
        this.isActive = true;
    }
}
