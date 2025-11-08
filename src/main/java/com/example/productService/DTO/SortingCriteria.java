package com.example.productService.DTO;

import com.example.productService.model.Product;

import java.util.Comparator;

public enum SortingCriteria {
    PRICE_ASC(Comparator.comparing(Product::getPrice, Comparator.nullsLast(Double::compareTo))),
    PRICE_DESC(Comparator.comparing(Product::getPrice, Comparator.nullsLast(Double::compareTo)).reversed()),
    TITLE_ASC(Comparator.comparing(Product::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))),
    TITLE_DESC(Comparator.comparing(Product::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)).reversed());

    private final Comparator<Product> comparator;

    SortingCriteria(Comparator<Product> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Product> getComparator() {
        return comparator;
    }
}

