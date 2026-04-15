package de.finanz.converter.io;

import com.opencsv.CSVWriter;
import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.calculation.Calculator;
import de.finanz.converter.calculation.ECalculationType;
import de.finanz.converter.cash.AvailableCash;
import de.finanz.converter.cash.EAvailableCashTyp;
import de.finanz.converter.categorie.ECategoryType;
import de.finanz.converter.categorie.ESuperCategoryType;

import java.io.FileWriter;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CSVExporter {

    public static void export(Bilanz bilanz, String outputFileName) throws IOException {

        CSVWriter writer = new CSVWriter(
                new FileWriter(outputFileName),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
        );

        // Header
        List<YearMonth> gesetzteYearMonths = bilanz.getYearMonthsSorted();
        String[] header = new String[gesetzteYearMonths.size() + 1];
        header[0] = "Kategorie";

        int i = 1;
        for (YearMonth yearMonth : gesetzteYearMonths) {
            header[i++] = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN); // JANUARY, FEBRUARY, ...
        }

        writer.writeNext(header);

        for (ESuperCategoryType superCategoryType : ESuperCategoryType.values()) {
            List<ECategoryType> categoryTypes = Arrays.stream(ECategoryType.values())
                    .filter(categoryType -> superCategoryType.equals(categoryType.getSuperCategoryType()))
                    .toList();
            String[] superCategoryNameRow = new String[1];
            superCategoryNameRow[0] = superCategoryType.getName();
            writer.writeNext(superCategoryNameRow);


            for (ECategoryType categoryType : categoryTypes) {
                List<String> rows = new ArrayList<>();
                rows.add(categoryType.getName());
                for (YearMonth yearMonth : gesetzteYearMonths) {
                    // TODO wenn in keinem Monat ein Wert dazu eingetragen wurde, soll die Zeile gar nicht
                    //  geschrieben werden
                    Double value = bilanz.getCategoryValue(categoryType, yearMonth);
                    rows.add(String.format("%.2f", value) + " €");
                }

                writer.writeNext(rows.toArray(String[]::new));
            }
            writer.writeNext(new String[0]); //Leerzeile
        }

        Calculator calculator = new Calculator(bilanz);
        for (ECalculationType calculationType : ECalculationType.values()) {
            List<String> rows = new ArrayList<>();
            rows.add(calculationType.getName());
            for (YearMonth yearMonth : gesetzteYearMonths) {
                rows.add(String.format("%.2f", calculator.getCalculationValue(calculationType, yearMonth)) + " €");
            }

            writer.writeNext(rows.toArray(String[]::new));
        }
        writer.writeNext(new String[0]); //Leerzeile

        for (EAvailableCashTyp availableCashTyp : EAvailableCashTyp.values()) {
            List<String> rows = new ArrayList<>();
            rows.add(availableCashTyp.getBezeichnung());
            for (YearMonth yearMonth : gesetzteYearMonths) {
                Optional<AvailableCash> availableCashOptional = bilanz.getAvailableCashesInYearMonths(availableCashTyp, yearMonth);
                availableCashOptional.ifPresent(availableCash -> rows.add(String.format("%.2f", availableCash.getBetrag()) + " €"));
            }
            writer.writeNext(rows.toArray(String[]::new));
        }

        writer.close();
    }
}