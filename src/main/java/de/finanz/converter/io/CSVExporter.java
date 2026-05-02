package de.finanz.converter.io;

import com.opencsv.CSVWriter;
import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.calculation.Calculator;
import de.finanz.converter.calculation.ECalculationType;
import de.finanz.converter.cash.AvailableCash;
import de.finanz.converter.cash.EAvailableCashTyp;
import de.finanz.converter.categorie.ECategoryType;
import de.finanz.converter.categorie.ESuperCategoryType;
import de.finanz.converter.exception.FinanzConverterException;
import de.finanz.converter.stocks.Stock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CSVExporter {

    public static final String FILE_NAME_FORMAT = "bilanz_%d.csv";
    private static final List<ECalculationType> DIFFERENZ = List.of(ECalculationType.CASH_DIFFERENZ
            , ECalculationType.GIROKONTO_DIFFERENZ);
    private static final Path OUTPUT_PATH = Path.of(System.getenv("OUTPUT_PATH"));
    //    private static final String FILE_NAME_FORMAT = OUTPUT_PATH + FILE_NAME;
    private static final List<ECalculationType> CALCULATION_TYPE_EINNAHMEN = List.of(ECalculationType.EINNAMEN_GESAMT);
    private static final List<ECalculationType> CALCULATION_TYPE_AUSGABEN = List.of(ECalculationType.GIROKONTO_AUSGABEN_FIX,
            ECalculationType.AUSGABEN_VARIABEL,
            ECalculationType.BARGELD_AUSGABEN,
            ECalculationType.AUSGABEN_GESAMT);
    private static final List<ECalculationType> CALCULATION_TYPE_SPARRATE = List.of(ECalculationType.UEBERSCHUSS_MONAT, ECalculationType.SPARRATE_GESAMT);
    private static final List<ECalculationType> CALCULATION_TYPE_KONTOSTAENDE =
            List.of(ECalculationType.GIROKONTO_IST, ECalculationType.CASH);
    private static final List<EAvailableCashTyp> AVAILABLE_CASH_LIST = List.of(EAvailableCashTyp.TAGESGELDKONTO, EAvailableCashTyp.BARGELD);
    private static final List<ECalculationType> CALCULATION_TYPE_WERTPAPIERE =
            List.of(ECalculationType.VERRECHNUNGSKONTO, ECalculationType.VANGUARD_FTSE_ALL_WORLD,
                    ECalculationType.ISHARES_NASDAQ_100,
                    ECalculationType.BITCOIN);
    private static final List<EAvailableCashTyp> AVAILABLE_CASH_WERTPAPIERE = List.of(EAvailableCashTyp.AKTIEN_VL);
    private static final List<ECalculationType> CALCULATION_TYPE_BILANZ = List.of(ECalculationType.BILANZ_MONAT);
    private final Bilanz bilanz;
    private final Calculator calculator;
    private final List<YearMonth> allGesetzteYearMonths;
    private List<YearMonth> currentGesetzteYearMonths;

    public CSVExporter(Bilanz bilanz) {
        this.bilanz = bilanz;
        this.calculator = new Calculator(bilanz);
        this.allGesetzteYearMonths = bilanz.getYearMonthsSorted();
    }

    public void export() {
        allGesetzteYearMonths.stream()
                .map(YearMonth::getYear)
                .distinct()
                .forEach(year -> {
                    String fileName = FILE_NAME_FORMAT.formatted(year);
                    try (FileWriter fileWriter =
                                 new FileWriter("./" + fileName)) {
                        currentGesetzteYearMonths = allGesetzteYearMonths.stream()
                                .filter(yearMonth -> yearMonth.getYear() == year)
                                .toList();
                        CSVWriter writer = new CSVWriter(
                                fileWriter,
                                CSVWriter.DEFAULT_SEPARATOR,
                                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                CSVWriter.DEFAULT_LINE_END
                        );

                        writeHeader(writer);
                        writeCategories(writer);
                        writeCalculations(writer);
                        writeKurse(writer);

                        Files.copy(Path.of(fileName), Path.of(OUTPUT_PATH + File.separator + fileName), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new FinanzConverterException(e);
                    }
                });
    }

    // "Kategorie","Januar","Februar","März" ...
    private void writeHeader(CSVWriter writer) {
        List<String> rows = new ArrayList<>();
        rows.add("Kategorie");

        currentGesetzteYearMonths.stream()
                .map(YearMonth::getMonth)
                .map(m -> m.getDisplayName(TextStyle.FULL, Locale.GERMAN))
                .forEach(rows::add);

        writer.writeNext(rows.toArray(String[]::new));
        writer.writeNext(new String[0]); //Leerzeile
    }

    private void writeCategories(CSVWriter writer) {
        for (ESuperCategoryType superCategoryType : ESuperCategoryType.values()) {
            // SuperCategory als Überschrift: "Einkommen", "Wohnen", "Versicherungen" ...
            writer.writeNext(new String[]{superCategoryType.getName()});


            // Alle Kategorien der jeweiligen Superkategorie: Möbel/Einrichtung", "Kleidung", ...
            List<ECategoryType> categoryTypes = Arrays.stream(ECategoryType.values())
                    .filter(categoryType -> superCategoryType.equals(categoryType.getSuperCategoryType()))
                    .toList();
            for (ECategoryType categoryType : categoryTypes) {
                List<Double> values = new ArrayList<>();
                for (YearMonth yearMonth : currentGesetzteYearMonths) {
                    values.add(bilanz.getCategoryValue(categoryType, yearMonth));
                }

                if (values.stream().noneMatch(d -> d != 0)) {
                    // Wenn alle Werte 0 sind, soll die Zeile nicht geschrieben werden
                    continue;
                }

                List<String> rows = new ArrayList<>();
                rows.add(categoryType.getName());
                rows.addAll(values.stream().map(this::formatBetrag).toList());

                writer.writeNext(rows.toArray(String[]::new));
            }
            writer.writeNext(new String[0]); //Leerzeile
        }
    }

    private void writeKurse(CSVWriter writer) {
        writer.writeNext(new String[0]); //Leerzeile
        writer.writeNext(new String[0]); //Leerzeile
        writer.writeNext(new String[]{"Kurse"});
        Set<String> stockNames = bilanz.getStocks().stream().map(Stock::getName).collect(Collectors.toSet());
        for (String stockName : stockNames) {
            List<String> rows = new ArrayList<>();
            rows.add(stockName);
            for (YearMonth yearMonth : currentGesetzteYearMonths) {
                Optional<Double> kursOptional = bilanz.getStocks().stream()
                        .filter(stock -> stock.getName().equals(stockName))
                        .filter(stock -> stock.getYearMonthOfDatum().equals(yearMonth))
                        .map(Stock::getKurs)
                        .findAny();
                if (kursOptional.isEmpty()) {
                    continue;
                }

                rows.add(formatBetrag(kursOptional.get()));
            }


            writer.writeNext(rows.toArray(String[]::new));
        }
        writer.writeNext(new String[0]); //Leerzeile
    }

    private String formatBetrag(Double betrag) {
        return String.format("%.2f", betrag);
    }


    private void writeCalculationsOfTypes(CSVWriter writer, String header,
                                          List<ECalculationType> calculationTypes) {
        writeCalculationsOfTypes(writer, header, calculationTypes, List.of());
    }

    private void writeCalculationsOfTypes(CSVWriter writer, String header,
                                          List<ECalculationType> calculationTypes,
                                          List<EAvailableCashTyp> availableCashTyps) {
        writer.writeNext(new String[]{header});


        for (EAvailableCashTyp availableCashTyp : availableCashTyps) {
            List<String> row = new ArrayList<>();
            row.add(availableCashTyp.getBezeichnung());
            for (YearMonth yearMonth : currentGesetzteYearMonths) {
                Optional<AvailableCash> availableCashOptional = bilanz.getAvailableCashesInYearMonths(availableCashTyp, yearMonth);
                double betrag = availableCashOptional.isPresent() ? availableCashOptional.get().getBetrag() : 0.0;
                row.add(this.formatBetrag(betrag));
            }
            writer.writeNext(row.toArray(String[]::new));
        }

        for (ECalculationType type : calculationTypes) {
            List<String> row = new ArrayList<>();
            row.add(type.getName());
            for (YearMonth yearMonth : currentGesetzteYearMonths) {
                row.add(formatBetrag(calculator.getCalculationValue(type, yearMonth)));
            }
            writer.writeNext(row.toArray(String[]::new));
        }

        writer.writeNext(new String[0]); //Leerzeile
    }

    private void writeCashDifferenz(CSVWriter writer) {
        if (differenceExists()) {
            // Differenz soll nur ausgegeben werden, wenn auch eine Vorhanden ist
            writeCalculationsOfTypes(writer, "Differenz in den Berechnungen", DIFFERENZ);
        }
    }

    private boolean differenceExists() {
        return (int) currentGesetzteYearMonths.stream()
                .mapToDouble(yearMonth -> DIFFERENZ.stream()
                        .mapToDouble(type -> calculator.getCalculationValue(type, yearMonth))
                        .sum())
                .sum() != 0;
    }


    private void writeCalculations(CSVWriter writer) {
        writeCalculationsOfTypes(writer, "Einnahmen", CALCULATION_TYPE_EINNAHMEN);
        writeCalculationsOfTypes(writer, "Ausgaben", CALCULATION_TYPE_AUSGABEN);
        writeCalculationsOfTypes(writer, "Sparrate", CALCULATION_TYPE_SPARRATE);
        writeCalculationsOfTypes(writer, "Kontostände", CALCULATION_TYPE_KONTOSTAENDE, AVAILABLE_CASH_LIST);
        writeCashDifferenz(writer);
        writeCalculationsOfTypes(writer, "Wertpapiere", CALCULATION_TYPE_WERTPAPIERE, AVAILABLE_CASH_WERTPAPIERE);
        writeCalculationsOfTypes(writer, "Bilanz", CALCULATION_TYPE_BILANZ);
    }


}