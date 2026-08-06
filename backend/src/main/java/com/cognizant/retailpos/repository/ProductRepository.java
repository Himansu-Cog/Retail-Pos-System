package com.cognizant.retailpos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cognizant.retailpos.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByBarcode(String barcode);

    @Query("select p from Product p where lower(p.name) like lower(concat('%', :text, '%')) " +
            "or lower(p.supplierMaster) like lower(concat('%', :text, '%'))")
    Page<Product> search(@Param("text") String text, Pageable pageable);

    @Query("select count(p) from Product p where lower(trim(p.name)) = lower(trim(:name))")
    long countName(@Param("name") String name);

    @Query("select count(p) from Product p where lower(trim(p.name)) = lower(trim(:name)) and p.id <> :id")
    long countNameExcept(@Param("name") String name, @Param("id") Long id);
}
