package de.finanz.converter.io;

import com.opencsv.CSVWriter;
import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.kategorie.ECategoryType;
import de.finanz.converter.kategorie.ESuperCategoryType;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CSVExporter {

    public static void export(Bilanz bilanz, String datei) throws IOException {

        CSVWriter writer = new CSVWriter(
                new FileWriter(datei),
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
        );

        // Header
        List<Month> gesetzteMonate = bilanz.getMonthsSorted();
        String[] header = new String[gesetzteMonate.size() + 1];
        header[0] = "Kategorie";

        int i = 1;
        for (Month m : gesetzteMonate) {
            header[i++] = m.getDisplayName(TextStyle.FULL, Locale.GERMAN); // JANUARY, FEBRUARY, ...
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
                for (Month month : gesetzteMonate) {
                    // TODO wenn in keinem Monat ein Wert dazu eingetragen wurde, soll die Zeile gar nicht
                    //  geschrieben werden
                    rows.add(String.valueOf(bilanz.getCategoryValue(categoryType, month)));
                }

                writer.writeNext(rows.toArray(String[]::new));
            }
            writer.writeNext(new String[0]); //Leerzeile
        }


        writer.close();
    }
}