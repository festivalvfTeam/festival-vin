package ru.vinotekavf.vinotekaapp.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@Data
@EqualsAndHashCode(exclude="provider")
@Entity
@Table(name = "posit")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    private boolean isActive;

    private String vendorCode;//артикул
    private String productName;
    private String volume;//объем
    private String releaseYear;
    private String price;
    private String promotionalPrice;//цена по скидке/акции
    private String remainder;//остаток
    private String maker;
    private Long lastChange;

    //артикул в Festival Vin
    private String fvVendorCode;
    private String fvProductName;

    public Position() {
        isActive = true;
    }

    public void addVendorCode(String vendorCode) {
        if (this.vendorCode == null || this.vendorCode.isEmpty())
            this.vendorCode = vendorCode;
        else
            this.vendorCode = this.vendorCode + ", " + vendorCode;
    }

    public void addProductName(String productName) {
        if (this.productName == null || this.productName.isEmpty())
            this.productName = productName;
        else
            this.productName = this.productName + ", " + productName;
    }

    public void addVolume(String volume) {
        if (this.volume == null || this.volume.isEmpty())
            this.volume = volume;
        else
            this.volume = this.volume + ", " + volume;
    }

    public void addReleaseYear(String releaseYear) {
        if (this.releaseYear == null || this.releaseYear.isEmpty())
            this.releaseYear = releaseYear;
        else
            this.releaseYear = this.releaseYear + ", " + releaseYear;
    }

    public void addPrice(String price) {
        if (this.price == null || this.price.isEmpty())
            this.price = price;
        else
            this.price = this.price + ", " + price;
    }

    public void addPromotionalPrice(String promotionalPrice) {
        if (this.promotionalPrice == null || this.promotionalPrice.isEmpty())
            this.promotionalPrice = promotionalPrice;
        else
            this.promotionalPrice = this.promotionalPrice + ", " + promotionalPrice;
    }

    public void addRemainder(String remainder) {
        if (this.remainder == null || this.remainder.isEmpty())
            this.remainder = remainder;
        else
            this.remainder = this.remainder + ", " + remainder;
    }

    public void addMaker(String maker) {
        if (this.maker == null || this.maker.isEmpty())
            this.maker = maker;
        else
            this.maker = this.maker + ", " + maker;
    }
}
