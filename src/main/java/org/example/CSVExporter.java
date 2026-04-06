package org.example;

import com.opencsv.CSVWriter;
import org.example.kategorie.Categorie;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Month;
import java.time.format.TextStyle;
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

        // Daten
        for (Categorie k : bilanz.getCategories().values()) {
            String[] row = new String[gesetzteMonate.size() + 1];
            row[0] = k.getType().getName();

            int j = 1;
            for (Month m : gesetzteMonate) {
                row[j++] = String.valueOf(k.getValue(m));
            }

            writer.writeNext(row);
        }

        writer.close();
    }
}