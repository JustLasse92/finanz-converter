package de.finanz.converter.calculation;

import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.cash.AvailableCash;
import de.finanz.converter.exception.FinanzConverterException;
import de.finanz.converter.kategorie.Categorie;
import de.finanz.converter.kategorie.ECategoryType;
import de.finanz.converter.kategorie.ESuperCategoryType;
import de.finanz.converter.stocks.SharedHeld;
import de.finanz.converter.stocks.StockPrice;

import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Calculator {
    private Map<ECalculationType, Categorie<ECalculationType>> calculations;


    public Calculator(Bilanz bilanz) {
        calculations = new HashMap<>();

        Collection<Categorie<ECategoryType>> categories = bilanz.getAllCategories();
        List<StockPrice> stockPrices = bilanz.getStockPrices();
        List<SharedHeld> sharedHelds = bilanz.getSharedHelds();
        List<YearMonth> yearMonthsSorted = bilanz.getYearMonthsSorted();
        List<AvailableCash> availableCashes = bilanz.getAvailableCashes();

        calculateEinnahmenGesamt(categories, yearMonthsSorted);
        calculateAusgabenFix(categories, yearMonthsSorted);
        calculateAusgabenVariabel(categories, yearMonthsSorted);
        calculateAusgabenGesamt(yearMonthsSorted);
        calculateMonatlicherUeberschuss(categories, yearMonthsSorted);
        calculateSparrateGesamt(categories, yearMonthsSorted);
        calculateStocks(sharedHelds, stockPrices);
        calculateMonatlicheBilanz(yearMonthsSorted);
        calculateCash(availableCashes);
    }

    public Double getCalculationValue(ECalculationType calculationType, YearMonth yearMonth) {
        if (calculations.containsKey(calculationType)) {
            return calculations.get(calculationType).getValue(yearMonth);
        }
        return 0.0;
    }

    private void calculateCash(List<AvailableCash> availableCashes) {
        for (AvailableCash availableCash : availableCashes) {
            addCalculation(ECalculationType.CASH, availableCash.getYearMonthOfDatum(), availableCash.getBetrag());
        }
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
            addCalculation(ECalculationType.findByName(sharedHeld.getName()), sharedHeld.getYearMonth(), stockValue);
        }
    }

    private void addCalculation(ECalculationType calculationType, YearMonth yearMonth, Double value) {
        calculations.putIfAbsent(calculationType, new Categorie<>(calculationType));
        Categorie<ECalculationType> calculation = calculations.get(calculationType);
        calculation.addValue(yearMonth, value);
    }

    // Monatlicher Überschuss = Einnahemen - Ausgaben - Sparrate
    private void calculateMonatlicherUeberschuss(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double monatlicherUeberschuss = getCalculationValue(ECalculationType.EINNAMEN_GESAMT, yearMonth)
                    + getCalculationValue(ECalculationType.AUSGABEN_GESAMT, yearMonth)
                    + sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARRATE), yearMonth);
            addCalculation(ECalculationType.UEBERSCHUSS_MONAT, yearMonth, monatlicherUeberschuss);
        }
    }

    private void calculateAusgabenGesamt(List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double ausgabenGesamt = getCalculationValue(ECalculationType.AUSGABEN_FIX, yearMonth)
                    + getCalculationValue(ECalculationType.AUSGABEN_VARIABEL, yearMonth);
            addCalculation(ECalculationType.AUSGABEN_GESAMT, yearMonth, ausgabenGesamt);
        }
    }

    private void calculateMonatlicheBilanz(List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double bilanz = getCalculationValue(ECalculationType.CASH, yearMonth)
                    + getCalculationValue(ECalculationType.VANGUARD_FTSE_ALL_WORLD, yearMonth)
                    + getCalculationValue(ECalculationType.ISHARES_NASDAQ_100, yearMonth)
                    + getCalculationValue(ECalculationType.BITCOIN, yearMonth);
            addCalculation(ECalculationType.BILANZ_MONAT, yearMonth, bilanz);
        }
    }

    // (Monatliche Sparrate + Monatlicher Überschuss)
    private void calculateSparrateGesamt(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double sparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARRATE), yearMonth);
            // Sparrate ist negativ angegeben. Hier wird der postive Wert gebraucht
            sparrate *= -1;
            Double ueberschussMonat = getCalculationValue(ECalculationType.UEBERSCHUSS_MONAT, yearMonth);
            double sparrateGesamt = sparrate + ueberschussMonat;
            addCalculation(ECalculationType.SPARRATE_GESAMT, yearMonth, sparrateGesamt);
        }
    }

    private void calculateAusgabenVariabel(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double sumValues = sumValuesOfSuperCategoryTypes(categories, List.of(ESuperCategoryType.LEBENSHALTUNG, ESuperCategoryType.AUTO_TANKEN,
                    ESuperCategoryType.ENTERTAINMENT, ESuperCategoryType.SONSTIGE), yearMonth);
            double sparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARRATE), yearMonth);
            double value = sumValues - sparrate;
            addCalculation(ECalculationType.AUSGABEN_VARIABEL, yearMonth, value);
        }

    }

    private void calculateAusgabenFix(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double value = sumValuesOfSuperCategoryTypes(categories, List.of(ESuperCategoryType.WOHNEN,
                    ESuperCategoryType.SONSTIGE_VERTRAEGE, ESuperCategoryType.VERSICHERUNGEN), yearMonth);
            addCalculation(ECalculationType.AUSGABEN_FIX, yearMonth, value);
        }
    }

    private void calculateEinnahmenGesamt(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double value = sumValuesOfSuperCategoryTypes(categories, List.of(ESuperCategoryType.EINKOMMEN), yearMonth);
            addCalculation(ECalculationType.EINNAMEN_GESAMT, yearMonth, value);
        }
    }

    private double sumValuesOfSuperCategoryTypes(Collection<Categorie<ECategoryType>> categories,
                                                 Collection<ESuperCategoryType> superCategoryTypes, YearMonth yearMonth) {
        return sumValuesOfAllTypes(categories, superCategoryTypes, List.of(), yearMonth);
    }

    private double sumValuesOfCategoryTypes(Collection<Categorie<ECategoryType>> categories, Collection<ECategoryType> categoryTypes
            , YearMonth yearMonth) {
        return sumValuesOfAllTypes(categories, List.of(), categoryTypes, yearMonth);
    }

    private double sumValuesOfAllTypes(Collection<Categorie<ECategoryType>> categories,
                                       Collection<ESuperCategoryType> superCategoryTypes,
                                       Collection<ECategoryType> categoryTypes, YearMonth yearMonth) {
        return categories.stream()
                .filter(categorie -> superCategoryTypes.contains(categorie.getType().getSuperCategoryType()) || categoryTypes.contains(categorie.getType()))
                .map(categorie -> categorie.getValue(yearMonth))
                .mapToDouble(d -> d)
                .sum();
    }

}
