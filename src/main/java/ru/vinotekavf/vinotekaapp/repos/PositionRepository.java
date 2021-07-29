package ru.vinotekavf.vinotekaapp.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.vinotekavf.vinotekaapp.dto.PositionProviderDto;
import ru.vinotekavf.vinotekaapp.entities.Position;
import ru.vinotekavf.vinotekaapp.entities.Provider;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {

    @Query("SELECT new ru.vinotekavf.vinotekaapp.dto.PositionProviderDto(" +
            "pr.name, pr.phone, po.maker, po.vendorCode, po.productName, " +
            "po.volume, po.releaseYear, po.price, po.remainder, po.fvVendorCode, " +
            "po.fvProductName) FROM Position po JOIN po.provider pr")
    List<PositionProviderDto> getJoinInformation();

    Position findByFvVendorCode(String fvVendorCode);

    @Query("from Position p where p.provider = :provider and p.vendorCode = :vendorCode and p.productName = :productName and p.volume = :volume " +
        "and p.releaseYear = :releaseYear")
    List<Position> findPositionList(Provider provider, String vendorCode, String productName, String volume, String releaseYear);

    List<Position> findAllByVendorCode(String vendorCode);

    void deleteAllByProvider(Provider provider);

    List<Position> findAllByProvider(Provider provider);

    @Query("from Position p where p.isActive = true order by p.provider.name, p.productName")
    List<Position> findAllByIsActiveTrueOrderByProviderAscProductNameAsc();

    @Query("from Position p where LOWER(concat(p.vendorCode)) like LOWER(concat('%', :query, '%')) " +
        "or LOWER(concat(p.productName)) like LOWER(concat('%', :query, '%')) " +
        "or p.releaseYear = :query " +
        "or LOWER(concat(p.maker)) like LOWER(concat('%', :query, '%')) " +
        "or LOWER(concat(p.fvVendorCode)) like LOWER(concat('%', :query, '%')) " +
        "or LOWER(concat(p.fvProductName)) like LOWER(concat('%', :query, '%')) ")
    List<Position> findByQuery(String query);

    @Query("from Position p where (LOWER(concat(p.vendorCode)) like LOWER(concat('%', :query, '%')) " +
        "or LOWER(concat(p.productName)) like LOWER(concat('%', :query, '%')) " +
        "or p.releaseYear = :query " +
        "or LOWER(concat(p.maker)) like LOWER(concat('%', :query, '%')) " +
        "or LOWER(concat(p.fvVendorCode)) like LOWER(concat('%', :query, '%')) " +
        "or LOWER(concat(p.fvProductName)) like LOWER(concat('%', :query, '%'))) " +
        "and p.provider = :provider")
    List<Position> findByQueryAndProvider(String query, Provider provider);
}
