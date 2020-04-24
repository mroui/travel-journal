package com.martynaroj.traveljournal.view.others.enums;

public enum Criterion {

    KEYWORDS;

    private Object value;

    Criterion() {}

    Criterion(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
