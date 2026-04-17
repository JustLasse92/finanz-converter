package de.finanz.converter.calculation;

import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.cash.AvailableCash;
import de.finanz.converter.cash.EAvailableCashTyp;
import de.finanz.converter.categorie.Categorie;
import de.finanz.converter.categorie.ECategoryType;
import de.finanz.converter.categorie.ESuperCategoryType;
import de.finanz.converter.exception.FinanzConverterException;
import de.finanz.converter.stocks.SharedHeld;
import de.finanz.converter.stocks.StockPrice;
import de.finanz.converter.transaction.Transaction;

import java.time.Month;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Calculator {
    private static final List<EAvailableCashTyp> CASH_TYPS = List.of(EAvailableCashTyp.BARGELD, EAvailableCashTyp.TAGESGELDKONTO, EAvailableCashTyp.VERRECHNUNGSKONTO);
    private static final Set<ESuperCategoryType> CATEGORY_TYPES_AUSGABEN_VARIABEL = Set.of(
            ESuperCategoryType.LEBENSHALTUNG,
            ESuperCategoryType.MOBILITAET,
            ESuperCategoryType.ENTERTAINMENT,
            ESuperCategoryType.ARBEIT_STUDIUM,
            ESuperCategoryType.SONSTIGE,
            ESuperCategoryType.AUSLAGEN);
    private static final Set<ESuperCategoryType> CATEGORY_TYPES_AUSGABEN_FIX = Set.of(
            ESuperCategoryType.WOHNEN,
            ESuperCategoryType.SONSTIGE_VERTRAEGE,
            ESuperCategoryType.VERSICHERUNGEN);
    private Map<ECalculationType, Categorie<ECalculationType>> calculations;


    public Calculator(Bilanz bilanz) {
        calculations = new HashMap<>();

        Collection<Categorie<ECategoryType>> categories = bilanz.getAllCategories();
        List<StockPrice> stockPrices = bilanz.getStockPrices();
        List<SharedHeld> sharedHelds = bilanz.getSharedHelds();
        List<YearMonth> yearMonthsSorted = bilanz.getYearMonthsSorted();
        List<AvailableCash> availableCashes = bilanz.getAvailableCashes();
        List<Transaction> allTransactions = bilanz.getAllTransactions();

        calculateEinnahmenGesamt(categories, yearMonthsSorted);
        calculateAusgabenFix(categories, yearMonthsSorted);
        calculateAusgabenVariabel(categories, yearMonthsSorted);
        calculateAusgabenGesamt(yearMonthsSorted);
        calculateMonatlicherUeberschuss(categories, yearMonthsSorted);
        calculateSparrateGesamt(categories, yearMonthsSorted);
        calculateStocks(sharedHelds, stockPrices);
        calculateGirokontoIst(allTransactions, bilanz.getUmsatz2023());
        calculateAuslagen(categories, yearMonthsSorted);
        calculateCash(availableCashes, yearMonthsSorted);
        calculateMonatlicheBilanz(availableCashes, yearMonthsSorted);
        calculateGirokontoDifferenz(categories);
    }


    public Double getCalculationValue(ECalculationType calculationType, YearMonth yearMonth) {
        if (calculations.containsKey(calculationType)) {
            return calculations.get(calculationType).getValue(yearMonth);
        }
        return 0.0;
    }

    private void calculateAuslagen(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double auslagenAusgangGesamt = sumValuesOfCategoryTypes(categories,
                    List.of(ECategoryType.AUSLAGEN_AUSGANG), yearMonth);
            double auslagenEingangGesamt = sumValuesOfCategoryTypes(categories,
                    List.of(ECategoryType.AUSLAGEN_EINGANG), yearMonth);
            addCalculation(ECalculationType.AUSLAGEN_AUSGANG_GESAMT, yearMonth, auslagenAusgangGesamt);
            addCalculation(ECalculationType.AUSLAGEN_EINGANG_GESAMT, yearMonth, auslagenEingangGesamt);
        }
    }

    private void calculateGirokontoDifferenz(Collection<Categorie<ECategoryType>> categories) {
        Categorie<ECalculationType> girokontoIst = calculations.get(ECalculationType.GIROKONTO_IST);
        Map<YearMonth, Double> values = girokontoIst.getValues();
        values.forEach((yearMonth, girokontoIstBetrag) -> {
            Double monatlicherUeberschuss = getCalculationValue(ECalculationType.UEBERSCHUSS_MONAT, yearMonth);
            Double girokontoIstBetragVormonat = getCalculationValue(ECalculationType.GIROKONTO_IST, yearMonth.minusMonths(1));
            double transferTagesgeldGesamt = categories.stream()
                    .filter(categorie -> categorie.getType().equals(ECategoryType.TAGESGELDKONTO_TRANSFER))
                    .mapToDouble(categorie -> categorie.getValue(yearMonth))
                    .sum();
            Double calcGesamt = girokontoIstBetragVormonat + monatlicherUeberschuss + transferTagesgeldGesamt;
            Double diff = girokontoIstBetrag - calcGesamt;
            addCalculation(ECalculationType.GIROKONTO_DIFFERENZ, yearMonth, diff);
        });
    }

    private void calculateGirokontoIst(List<Transaction> allTransactions, final Double umsatz2023) {
        addCalculation(ECalculationType.GIROKONTO_IST, YearMonth.of(2023, Month.DECEMBER), umsatz2023);

        List<YearMonth> allYearMonths = allTransactions.stream()
                .map(Transaction::getYearMonthOfBuchungsdatum)
                .sorted()
                .distinct()
                .toList();

        for (YearMonth yearMonth : allYearMonths) {
            Double sumTransactionsInYearMonth = sumAllTransactionsInYearMonth(allTransactions, yearMonth);
            Double betragVorher = getCalculationValue(ECalculationType.GIROKONTO_IST, yearMonth.minusMonths(1));
            Double girokontoIstBetrag = sumTransactionsInYearMonth + betragVorher;
            addCalculation(ECalculationType.GIROKONTO_IST, yearMonth, girokontoIstBetrag);
        }
    }

    private Double sumAllTransactionsInYearMonth(List<Transaction> transactions, YearMonth yearMonth) {
        return transactions.stream()
                .filter(t -> t.getYearMonthOfBuchungsdatum().equals(yearMonth))
                .map(Transaction::getBetrag)
                .mapToDouble(d -> d)
                .sum();
    }

    private void calculateCash(List<AvailableCash> availableCashes, List<YearMonth> yearMonthsSorted) {
        availableCashes.stream()
                .filter(cash -> CASH_TYPS.contains(cash.getTyp()))
                .forEach(availableCash -> addCalculation(ECalculationType.CASH, availableCash.getYearMonthOfDatum(), availableCash.getBetrag()));

        for (YearMonth yearMonth : yearMonthsSorted) {
            addCalculation(ECalculationType.CASH, yearMonth,
                    getCalculationValue(ECalculationType.GIROKONTO_IST, yearMonth));
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

    private void calculateMonatlicheBilanz(List<AvailableCash> availableCashes, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double bilanz = getCalculationValue(ECalculationType.CASH, yearMonth)
                    + getCalculationValue(ECalculationType.VANGUARD_FTSE_ALL_WORLD, yearMonth)
                    + getCalculationValue(ECalculationType.ISHARES_NASDAQ_100, yearMonth)
                    + getCalculationValue(ECalculationType.BITCOIN, yearMonth);
            addCalculation(ECalculationType.BILANZ_MONAT, yearMonth, bilanz);
        }

        availableCashes.stream()
                .filter(availableCash -> availableCash.getTyp().equals(EAvailableCashTyp.AKTIEN_VL))
                .forEach(availableCash -> addCalculation(ECalculationType.BILANZ_MONAT,
                        availableCash.getYearMonthOfDatum(), availableCash.getBetrag()));
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
            double sumValues = sumValuesOfSuperCategoryTypes(categories, CATEGORY_TYPES_AUSGABEN_VARIABEL, yearMonth);
            double sparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARRATE), yearMonth);
            double value = sumValues - sparrate;
            addCalculation(ECalculationType.AUSGABEN_VARIABEL, yearMonth, value);
        }

    }

    private void calculateAusgabenFix(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double value = sumValuesOfSuperCategoryTypes(categories, CATEGORY_TYPES_AUSGABEN_FIX, yearMonth);
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
