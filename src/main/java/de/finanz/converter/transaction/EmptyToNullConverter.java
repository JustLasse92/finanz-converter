package de.finanz.converter.transaction;

import com.opencsv.bean.AbstractBeanField;

public class EmptyToNullConverter extends AbstractBeanField<String, String> {

    @Override
    protected Object convert(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value;
    }
}