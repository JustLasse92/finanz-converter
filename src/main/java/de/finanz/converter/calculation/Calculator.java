package de.finanz.converter.calculation;

import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.cash.AvailableCash;
import de.finanz.converter.cash.EAvailableCashTyp;
import de.finanz.converter.categorie.Categorie;
import de.finanz.converter.categorie.ECategoryType;
import de.finanz.converter.categorie.ESuperCategoryType;
import de.finanz.converter.exception.FinanzConverterException;
import de.finanz.converter.stocks.Stock;
import de.finanz.converter.transaction.Transaction;

import java.time.Month;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Calculator {
    private static final List<EAvailableCashTyp> CASH_TYPS = List.of(EAvailableCashTyp.BARGELD, EAvailableCashTyp.TAGESGELDKONTO);
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
        List<Stock> stocks = bilanz.getStocks();
        List<YearMonth> yearMonthsSorted = bilanz.getYearMonthsSorted();
        List<AvailableCash> availableCashes = bilanz.getAvailableCashes();
        List<Transaction> allTransactions = bilanz.getAllTransactions();

        calculateVerrechnungskonto(allTransactions, yearMonthsSorted);
        calculateBargeldausgaben(availableCashes, categories);
        calculateEinnahmenGesamt(categories, yearMonthsSorted);
        calculateAusgabenFix(categories, yearMonthsSorted);
        calculateAusgabenVariabel(categories, yearMonthsSorted);
        calculateAusgabenGesamt(yearMonthsSorted);
        calculateMonatlicherUeberschuss(categories, yearMonthsSorted);
        calculateSparrateGesamt(categories, yearMonthsSorted);
        calculateSparplan(categories, yearMonthsSorted);
        calculateStocks(stocks);
        calculateGirokontoIst(allTransactions, bilanz.getUmsatz2023());
        calculateAuslagen(categories, yearMonthsSorted);
        calculateCash(availableCashes);
        calculateMonatlicheBilanz(availableCashes, yearMonthsSorted);
        calculateGirokontoDifferenz(categories);
        calculateCashDifferenz();
        calculateJahreswerte(categories, yearMonthsSorted);
    }


    public Double getCalculationValue(ECalculationType calculationType, YearMonth yearMonth) {
        if (calculations.containsKey(calculationType)) {
            return calculations.get(calculationType).getValue(yearMonth);
        }
        return 0.0;
    }

    public Categorie<ECalculationType> getCalculationCategory(ECalculationType calculationType) {
        return calculations.get(calculationType);
    }

    private void calculateJahreswerte(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        Map<Integer, Double> sumSparratePerYear = new HashMap<>();
        Map<Integer, Integer> monthsPerYear = new HashMap<>();
        for (YearMonth yearMonth : yearMonthsSorted) {
            int year = yearMonth.getYear();
            monthsPerYear.put(year, 1 + monthsPerYear.getOrDefault(year, 0));
            double sumSparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.VERRECHNUNGSKONTO_SPARPLAN), yearMonth);
            sumSparratePerYear.put(year, Math.abs(sumSparrate) + sumSparratePerYear.getOrDefault(year, 0.0));
        }
        for (Integer year : monthsPerYear.keySet()) {
            double sparrateDurchschnitt = sumSparratePerYear.get(year) / monthsPerYear.get(year);
            addCalculation(ECalculationType.SPARPLAN_DURCHSCHNITT, year, sparrateDurchschnitt);
        }

    }


    private void calculateBargeldausgaben(List<AvailableCash> availableCashes, Collection<Categorie<ECategoryType>> categories) {
        for (AvailableCash availableCash : availableCashes) {
            if (!availableCash.getTyp().equals(EAvailableCashTyp.BARGELD)) {
                continue;
            }
            YearMonth aktuellerYearMonth = availableCash.getYearMonthOfDatum();
            YearMonth vormonat = aktuellerYearMonth.minusMonths(1);

            List<Double> bargeldVormonatOptional = availableCashes.stream()
                    .filter(cash -> cash.getTyp().equals(EAvailableCashTyp.BARGELD))
                    .filter(cash -> cash.getYearMonthOfDatum().equals(vormonat))
                    .map(AvailableCash::getBetrag)
                    .toList();

            if (bargeldVormonatOptional.size() > 1) {
                throw new FinanzConverterException("Es wurde erwartet einen Bargeldbestand zum Vormonat zu finden. "
                        + "Es wurden jedoch " + bargeldVormonatOptional.size() + " gefunden");
            }

            Double bargeldAktuellerMonat = availableCash.getBetrag();
            Double bargeldVormonat = bargeldVormonatOptional.isEmpty() ? 0.0 : bargeldVormonatOptional.getFirst();
            Double bargeldAbhebungen = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.BARGELDABHEBUNGEN), aktuellerYearMonth);
            Double bargeldAusgaben = bargeldAktuellerMonat + bargeldAbhebungen - bargeldVormonat;

            addCalculation(ECalculationType.BARGELD_AUSGABEN, aktuellerYearMonth, bargeldAusgaben);
        }
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

    private void calculateCashDifferenz() {
        Categorie<ECalculationType> cashIst = calculations.get(ECalculationType.CASH);
        Map<YearMonth, Double> cashIstValues = cashIst.getValuesYearMonths();
        cashIstValues.forEach((yearMonth, cashIstBetrag) -> {
            Double monatlicherUeberschuss = getCalculationValue(ECalculationType.UEBERSCHUSS_MONAT, yearMonth);
            Double cashIstBetragVormonat = getCalculationValue(ECalculationType.CASH, yearMonth.minusMonths(1));
            Double cashCalculated =
                    cashIstBetragVormonat + monatlicherUeberschuss;
            Double diff = cashIstBetrag - cashCalculated;
            if (Math.abs(diff) > 3) {
                addCalculation(ECalculationType.CASH_DIFFERENZ, yearMonth, diff);
            }
        });
    }

    private void calculateGirokontoDifferenz(Collection<Categorie<ECategoryType>> categories) {
        Categorie<ECalculationType> girokontoIst = calculations.get(ECalculationType.GIROKONTO_IST);
        Map<YearMonth, Double> values = girokontoIst.getValuesYearMonths();
        values.forEach((yearMonth, girokontoIstBetrag) -> {
            Double monatlicherUeberschuss = getCalculationValue(ECalculationType.UEBERSCHUSS_MONAT, yearMonth);
            Double girokontoIstBetragVormonat = getCalculationValue(ECalculationType.GIROKONTO_IST, yearMonth.minusMonths(1));
            double transferzahlungenGesamt = categories.stream()
                    .filter(categorie -> categorie.getType().getSuperCategoryType().equals(ESuperCategoryType.TRANSFER))
                    // Keine Ahnung wieso der Sparplan nicht dabei sein darf
                    .filter(categorie -> !categorie.getType().equals(ECategoryType.VERRECHNUNGSKONTO_SPARPLAN))
                    .mapToDouble(categorie -> categorie.getValue(yearMonth))
                    .sum();
            double bargeldausgaben = getCalculationValue(ECalculationType.BARGELD_AUSGABEN, yearMonth);
            Double girokontoCalculated =
                    girokontoIstBetragVormonat + monatlicherUeberschuss + transferzahlungenGesamt - bargeldausgaben;
            Double diff = girokontoIstBetrag - girokontoCalculated;
            addCalculation(ECalculationType.GIROKONTO_DIFFERENZ, yearMonth, diff);
        });
    }

    private void calculateVerrechnungskonto(List<Transaction> allTransactions, List<YearMonth> yearMonths) {
        for (YearMonth yearMonth : yearMonths) {
            double sum = allTransactions.stream()
                    .filter(transaction -> transaction.getAdditionalOrderAusfuehrungsdatum() != null)
                    .filter(transaction -> transaction.getYearMonthOfBuchungsdatum().equals(yearMonth))
                    .filter(transaction -> {
                        // Wenn die Order nicht im selben Monat ausgeführt wird, liegt das Geld solange auf dem Verrechnungskonto
                        return transaction.getYearMonthOfAdditionalOrderAusfuehrungsdatum().isAfter(transaction.getYearMonthOfBuchungsdatum());
                    })
                    .mapToDouble(Transaction::getBetrag)
                    .map(Math::abs)
                    .sum();

            addCalculation(ECalculationType.VERRECHNUNGSKONTO, yearMonth, sum);
        }

    }

    private void calculateGirokontoIst(List<Transaction> allTransactions, final Double umsatz2023) {
        addCalculation(ECalculationType.GIROKONTO_IST, YearMonth.of(2023, Month.DECEMBER), umsatz2023);
        YearMonth now = YearMonth.now();

        List<YearMonth> allYearMonths = allTransactions.stream()
                .map(Transaction::getYearMonthOfBuchungsdatum)
                .filter(y -> y.getMonth().compareTo(now.getMonth()) < 0 || y.getYear() < now.getYear())
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

    private void calculateCash(List<AvailableCash> availableCashes) {
        availableCashes.stream()
                .filter(cash -> CASH_TYPS.contains(cash.getTyp()))
                .forEach(availableCash -> addCalculation(ECalculationType.CASH, availableCash.getYearMonthOfDatum(), availableCash.getBetrag()));

        getCalculationCategory(ECalculationType.GIROKONTO_IST).getValuesYearMonths()
                .forEach(((yearMonth, aDouble) -> addCalculation(ECalculationType.CASH, yearMonth, aDouble)));
    }

    private void calculateStocks(List<Stock> stocks) {
        for (Stock stock : stocks) {
            Double gehalteneAnteile = stock.getGehalteneAnteile();
            Double kurs = stock.getKurs();
            Double stockValue = gehalteneAnteile * kurs;
            addCalculation(ECalculationType.findByName(stock.getName()), stock.getYearMonthOfDatum(), stockValue);
        }
    }

    private void addCalculation(ECalculationType calculationType, YearMonth yearMonth, Double value) {
        calculations.putIfAbsent(calculationType, new Categorie<>(calculationType));
        Categorie<ECalculationType> calculation = calculations.get(calculationType);
        calculation.addValue(yearMonth, value);
    }

    private void addCalculation(ECalculationType calculationType, Integer year, double value) {
        calculations.putIfAbsent(calculationType, new Categorie<>(calculationType));
        Categorie<ECalculationType> calculation = calculations.get(calculationType);
        calculation.addValue(year, value);
    }

    // Monatlicher Überschuss = Einnahemen - Ausgaben - Sparrate
    private void calculateMonatlicherUeberschuss(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double monatlicherUeberschuss = getCalculationValue(ECalculationType.EINNAMEN_GESAMT, yearMonth)
                    + getCalculationValue(ECalculationType.AUSGABEN_GESAMT, yearMonth)
                    + sumValuesOfCategoryTypes(categories, List.of(ECategoryType.VERRECHNUNGSKONTO_SPARPLAN), yearMonth);
            addCalculation(ECalculationType.UEBERSCHUSS_MONAT, yearMonth, monatlicherUeberschuss);
        }
    }

    private void calculateAusgabenGesamt(List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double ausgabenGesamt = getCalculationValue(ECalculationType.GIROKONTO_AUSGABEN_FIX, yearMonth)
                    + getCalculationValue(ECalculationType.GIROKONTO_AUSGABEN_VARIABEL, yearMonth)
                    + getCalculationValue(ECalculationType.BARGELD_AUSGABEN, yearMonth);
            addCalculation(ECalculationType.AUSGABEN_GESAMT, yearMonth, ausgabenGesamt);
        }
    }

    private void calculateMonatlicheBilanz(List<AvailableCash> availableCashes, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double bilanz = getCalculationValue(ECalculationType.CASH, yearMonth)
                    + getCalculationValue(ECalculationType.VANGUARD_FTSE_ALL_WORLD, yearMonth)
                    + getCalculationValue(ECalculationType.ISHARES_NASDAQ_100, yearMonth)
                    + getCalculationValue(ECalculationType.VERRECHNUNGSKONTO, yearMonth)
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
            double sparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.VERRECHNUNGSKONTO_SPARPLAN), yearMonth);
            // Sparrate ist negativ angegeben. Hier wird der postive Wert gebraucht
            sparrate = Math.abs(sparrate);
            Double ueberschussMonat = getCalculationValue(ECalculationType.UEBERSCHUSS_MONAT, yearMonth);
            double sparrateGesamt = sparrate + ueberschussMonat;
            addCalculation(ECalculationType.SPARRATE_GESAMT, yearMonth, sparrateGesamt);
        }
    }

    private void calculateSparplan(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double sparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.VERRECHNUNGSKONTO_SPARPLAN), yearMonth);
            // Sparrate ist negativ angegeben. Hier wird der postive Wert gebraucht
            sparrate = Math.abs(sparrate);
            addCalculation(ECalculationType.SPARPLAN, yearMonth, sparrate);
        }
    }

    private void calculateAusgabenVariabel(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double sumValues = sumValuesOfSuperCategoryTypes(categories, CATEGORY_TYPES_AUSGABEN_VARIABEL, yearMonth);
//            double sparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARPLAN), yearMonth);
//            double bargeldausgaben = getCalculationValue(ECalculationType.BARGELD_AUSGABEN, yearMonth);
            double value = sumValues;
            addCalculation(ECalculationType.GIROKONTO_AUSGABEN_VARIABEL, yearMonth, value);
        }

    }

    private void calculateAusgabenFix(Collection<Categorie<ECategoryType>> categories, List<YearMonth> yearMonthsSorted) {
        for (YearMonth yearMonth : yearMonthsSorted) {
            double value = sumValuesOfSuperCategoryTypes(categories, CATEGORY_TYPES_AUSGABEN_FIX, yearMonth);
            addCalculation(ECalculationType.GIROKONTO_AUSGABEN_FIX, yearMonth, value);
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
