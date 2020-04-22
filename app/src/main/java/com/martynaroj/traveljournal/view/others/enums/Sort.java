package com.martynaroj.traveljournal.view.others.enums;

public enum Sort {

    DATE_LATEST("Date added: latest"),
    DATE_OLDEST("Date added: oldest"),
    POPULARITY("Popularity: greatest"),
    DURATION_LONGEST("Duration: longest"),
    DURATION_SHORTEST("Duration: shortest");

    private String value;

    Sort(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
