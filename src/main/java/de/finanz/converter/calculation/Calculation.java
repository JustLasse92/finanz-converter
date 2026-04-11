package de.finanz.converter.calculation;

import lombok.Getter;

import java.time.Month;
import java.util.EnumMap;
import java.util.Map;

@Getter
public class Calculation {
    ECalculationType type;
    private Map<Month, Double> values;

    public Calculation(ECalculationType type) {
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
