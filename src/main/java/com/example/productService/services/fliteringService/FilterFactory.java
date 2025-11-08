package com.example.productService.services.fliteringService;

public class FilterFactory {
    public static Filter getFilterFromkey(String key) {
        if (key == null) {
            return null;
        }
        if (key.equalsIgnoreCase("BRAND")) {
            return new BrandFilter();
        } else if (key.equalsIgnoreCase("RAM")) {
            return new RAMfilter();
        }
        return null;
    }
}
