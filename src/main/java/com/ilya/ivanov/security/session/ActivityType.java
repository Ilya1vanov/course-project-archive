package com.ilya.ivanov.security.session;

import com.itextpdf.text.BaseColor;

/**
 * Created by ilya on 6/5/17.
 */
public enum ActivityType {
    ADD("Added files: ", "+", BaseColor.GREEN), REMOVE("Removed files: ", "-", BaseColor.RED);

    private final String description;

    private final String sign;

    private final BaseColor color;

    ActivityType(String description, String sign, BaseColor color) {
        this.description = description;
        this.sign = sign;
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public String getSign() {
        return sign;
    }

    public BaseColor getColor() {
        return color;
    }
}
