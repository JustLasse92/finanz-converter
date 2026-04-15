package de.finanz.converter.categorie;

import lombok.Getter;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Categorie<T> {
    private T type;
    private Map<YearMonth, Double> values;

    public Categorie(T type) {
        this.type = type;
        this.values = new HashMap<>();
    }

    public void addValue(YearMonth yearMonth, double value) {
        values.put(yearMonth, value + getValue(yearMonth));
    }

    public double getValue(YearMonth yearMonth) {
        return values.getOrDefault(yearMonth, 0.0);
    }
}
