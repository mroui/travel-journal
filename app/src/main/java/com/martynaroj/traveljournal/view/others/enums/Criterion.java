package com.martynaroj.traveljournal.view.others.enums;

public enum Criterion {

    KEYWORDS,
    DAYS_FROM,
    DAYS_TO,
    DESTINATION,
    TAGS;

    private String value;

    Criterion() {}

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
