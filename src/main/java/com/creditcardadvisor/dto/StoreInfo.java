package com.creditcardadvisor.dto;

public class StoreInfo {
    private final String name;
    private final String category;

    public StoreInfo(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}

