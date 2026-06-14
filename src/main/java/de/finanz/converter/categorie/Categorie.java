package de.finanz.converter.categorie;

import lombok.Getter;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Categorie<T> {
    private T type;
    private Map<YearMonth, Double> valuesYearMonths;
    private Map<Integer, Double> valuesYears;

    public Categorie(T type) {
        this.type = type;
        this.valuesYearMonths = new HashMap<>();
        this.valuesYears = new HashMap<>();
    }

    public void addValue(YearMonth yearMonth, double value) {
        valuesYearMonths.put(yearMonth, value + getValue(yearMonth));
    }

    public void addValue(Integer year, double value) {
        valuesYears.put(year, value + getValue(year));
    }

    public double getValue(YearMonth yearMonth) {
        return valuesYearMonths.getOrDefault(yearMonth, 0.0);
    }

    public double getValue(Integer year) {
        return valuesYears.getOrDefault(year, 0.0);
    }
}
