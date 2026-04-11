package de.finanz.converter.calculation;

import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.exception.FinanzConverterException;
import de.finanz.converter.kategorie.Categorie;
import de.finanz.converter.kategorie.ECategoryType;
import de.finanz.converter.kategorie.ESuperCategoryType;
import de.finanz.converter.stocks.SharedHeld;
import de.finanz.converter.stocks.StockPrice;

import java.time.Month;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Calculator {
    // DONE
//     - Gesamte Einnahmen
//    - Fixe Ausgaben
//    - Variable Ausgaben
//    - Gesamte Ausgaben
//    - Monatlicher Überschuss
//    - Komplette Sparrate
//    - Aktien
//    - Bitcoin

    // NOCH ZU IMPLEMENTIEREN
//    - Bilanz (Wieviel Kapital ist am Ende des Monats da? Cash + Aktien + Bitcoin)
//    - Cash wird nicht berechnet, sondern nur angegeben
//    - Differenz zwischen berechneter Bilanz und tatsächlicher Bilanz

    private Map<ECalculationType, Calculation> calculations;


    public Calculator(Bilanz bilanz) {
        calculations = new HashMap<>();
        Collection<Categorie> categories = bilanz.getAllCategories();
        List<StockPrice> stockPrices = bilanz.getStockPrices();
        List<SharedHeld> sharedHelds = bilanz.getSharedHelds();
        calculateEinnahmenGesamt(categories);
        calculateAusgabenFix(categories);
        calculateAusgabenVariabel(categories);
        calculateAusgabenGesamt();
        calculateMonatlicherUeberschuss(categories);
        calculateSparrateGesamt(categories);
        calculateStocks(sharedHelds, stockPrices);
        calculateMonatlicheBilanz();
    }

    public Double getCalculationValue(ECalculationType calculationType, Month month) {
        if (calculations.containsKey(calculationType)) {
            return calculations.get(calculationType).getValue(month);
        }
        return 0.0;
    }

    private void calculateStocks(Collection<SharedHeld> sharedHelds, Collection<StockPrice> stockPrices) {
        for (SharedHeld sharedHeld : sharedHelds) {
            List<StockPrice> stockPricesZumSharedHeld = stockPrices.stream()
                    .filter(stockPrice -> stockPrice.getName().equals(sharedHeld.getName())
                            && stockPrice.getDatum().equals(sharedHeld.getDatum()))
                    .toList();
            if (stockPricesZumSharedHeld.size() != 1) {
                throw new FinanzConverterException("Es wurde erwartet einen Aktienpreis zu finden. Es wurden jedoch "
                        + stockPricesZumSharedHeld.size() + " gefunden zur gehaltenen Aktie: " + sharedHeld);
            }

            Double gehalteneAnteile = sharedHeld.getGehalteneAnteile();
            Double kurs = stockPricesZumSharedHeld.getFirst().getKurs();
            Double stockValue = gehalteneAnteile * kurs;
            addCalculation(ECalculationType.findByName(sharedHeld.getName()), sharedHeld.getMonth(), stockValue);
        }
    }

    private void addCalculation(ECalculationType calculationType, Month month, Double value) {
        calculations.putIfAbsent(calculationType, new Calculation(calculationType));
        Calculation calculation = calculations.get(calculationType);
        double roundedValue = Math.floor(value * 100) / 100;
        calculation.addValue(month, roundedValue);
    }

    // Monatlicher Überschuss = Einnahemen - Ausgaben - Sparrate
    private void calculateMonatlicherUeberschuss(Collection<Categorie> categories) {
        for (Month month : Month.values()) {
            double monatlicherUeberschuss = getCalculationValue(ECalculationType.EINNAMEN_GESAMT, month)
                    + getCalculationValue(ECalculationType.AUSGABEN_GESAMT, month)
                    + sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARRATE), month);
            addCalculation(ECalculationType.UEBERSCHUSS_MONAT, month, monatlicherUeberschuss);
        }
    }

    private void calculateAusgabenGesamt() {
        for (Month month : Month.values()) {
            double ausgabenGesamt = getCalculationValue(ECalculationType.AUSGABEN_FIX, month)
                    + getCalculationValue(ECalculationType.AUSGABEN_VARIABEL, month);
            addCalculation(ECalculationType.AUSGABEN_GESAMT, month, ausgabenGesamt);
        }
    }

    private void calculateMonatlicheBilanz() {
        for (Month month : Month.values()) {
            double bilanz = getCalculationValue(ECalculationType.CASH, month)
                    + getCalculationValue(ECalculationType.VANGUARD_FTSE_ALL_WORLD, month)
                    + getCalculationValue(ECalculationType.ISHARES_NASDAQ_100, month)
                    + getCalculationValue(ECalculationType.BITCOIN, month);
            addCalculation(ECalculationType.BILANZ_MONAT, month, bilanz);
        }
    }

    // (Monatliche Sparrate + Monatlicher Überschuss)
    private void calculateSparrateGesamt(Collection<Categorie> categories) {
        for (Month month : Month.values()) {
            double sparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARRATE), month);
            // Sparrate ist negativ angegeben. Hier wird der postive Wert gebraucht
            sparrate *= -1;
            Double ueberschussMonat = getCalculationValue(ECalculationType.UEBERSCHUSS_MONAT, month);
            double sparrateGesamt = sparrate + ueberschussMonat;
            addCalculation(ECalculationType.SPARRATE_GESAMT, month, sparrateGesamt);
        }
    }

    private void calculateAusgabenVariabel(Collection<Categorie> categories) {
        for (Month month : Month.values()) {
            double sumValues = sumValuesOfSuperCategoryTypes(categories, List.of(ESuperCategoryType.LEBENSHALTUNG, ESuperCategoryType.AUTO_TANKEN,
                    ESuperCategoryType.ENTERTAINMENT, ESuperCategoryType.SONSTIGE), month);
            double sparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARRATE), month);
            double value = sumValues - sparrate;
            addCalculation(ECalculationType.AUSGABEN_VARIABEL, month, value);
        }

    }

    private void calculateAusgabenFix(Collection<Categorie> categories) {
        for (Month month : Month.values()) {
            double value = sumValuesOfSuperCategoryTypes(categories, List.of(ESuperCategoryType.WOHNEN,
                    ESuperCategoryType.SONSTIGE_VERTRAEGE, ESuperCategoryType.VERSICHERUNGEN), month);
            addCalculation(ECalculationType.AUSGABEN_FIX, month, value);
        }
    }

    private void calculateEinnahmenGesamt(Collection<Categorie> categories) {
        for (YearMonth yearMonth : getYearMonths(categories)) {
            double value = sumValuesOfSuperCategoryTypes(categories, List.of(ESuperCategoryType.EINKOMMEN), yearMonth);
            addCalculation(ECalculationType.EINNAMEN_GESAMT, yearMonth, value);
        }
    }

    private Collection<YearMonth> getYearMonths(Collection<Categorie> categories) {
        return categories.stream()
                .map(categorie -> categorie.getValues().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

    private double sumValuesOfSuperCategoryTypes(Collection<Categorie> categories,
                                                 Collection<ESuperCategoryType> superCategoryTypes, YearMonth yearMonth) {
        return sumValuesOfAllTypes(categories, superCategoryTypes, List.of(), yearMonth);
    }

    private double sumValuesOfCategoryTypes(Collection<Categorie> categories, Collection<ECategoryType> categoryTypes
            , YearMonth yearMonth) {
        return sumValuesOfAllTypes(categories, List.of(), categoryTypes, yearMonth);
    }

    private double sumValuesOfAllTypes(Collection<Categorie> categories,
                                       Collection<ESuperCategoryType> superCategoryTypes,
                                       Collection<ECategoryType> categoryTypes, YearMonth yearMonth) {
        return categories.stream()
                .filter(categorie -> superCategoryTypes.contains(categorie.getType().getSuperCategoryType()) || categoryTypes.contains(categorie.getType()))
                .map(categorie -> categorie.getValue(yearMonth))
                .mapToDouble(d -> d)
                .sum();
    }

}
