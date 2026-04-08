package de.finanz.converter.kategorie;

import lombok.Getter;

import java.time.Month;
import java.util.EnumMap;
import java.util.Map;

@Getter
public class Categorie {
    private ECategoryType type;
    private Map<Month, Double> values;

    public Categorie(ECategoryType type) {
        this.type = type;
        this.values = new EnumMap<>(Month.class);
    }

    public void addValue(Month monat, double value) {
        values.put(monat, value + getValue(monat));
    }

    public double getValue(Month monat) {
        return values.getOrDefault(monat, 0.0);
    }
}
