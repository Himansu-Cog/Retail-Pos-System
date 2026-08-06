package com.cognizant.retailpos.enums;

import java.math.BigDecimal;

public enum ProductCategory {
    GROCERY("5.00"),
    BEVERAGES("12.00"),
    DAIRY("5.00"),
    BAKERY("5.00"),
    SNACKS("12.00"),
    PERSONAL_CARE("18.00"),
    HOME_CARE("18.00"),
    ELECTRONICS("18.00"),
    OTHER("18.00");

    private final BigDecimal taxPercentage;

    ProductCategory(String taxPercentage) {
        this.taxPercentage = new BigDecimal(taxPercentage);
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }
}
