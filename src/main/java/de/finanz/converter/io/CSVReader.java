package de.finanz.converter.io;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.AllArgsConstructor;
import org.apache.commons.io.input.BOMInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
public abstract class CSVReader<T> {

    // Wird nur benötigt um die Class zur Laufzeit anzugeben
    private Class<T> type;

    public List<T> readTransactions(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            return readTransactionsFolder(path);
        } else {
            return readTransactionsFile(path);
        }
    }

    private List<T> readTransactionsFolder(Path path) throws IOException {
        List<T> transactions = new ArrayList<>();
        try (Stream<Path> paths = Files.list(path)) {
            List<Path> files = paths.filter(p -> p.toString().endsWith(".csv"))
                    .toList();
            for (Path p : files) {
                transactions.addAll(readTransactionsFile(p));
            }
        }
        return transactions;
    }

    private List<T> readTransactionsFile(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path);
             // BOMInputStream wird benötigt, da Excel und LibreWriter als ertes Zeichen eine BOM schreiben
             // (Byte Order Mark -> \uFEFF = UTF-8 BOM)
             // Der BOMInputStream kann mit und ohne BOM umgehen
             BOMInputStream bomIn = BOMInputStream.builder().setInputStream(is).get();
             Reader reader = new InputStreamReader(bomIn, StandardCharsets.UTF_8)) {
            return new CsvToBeanBuilder(reader).withType(type).build().parse();
        }
    }
}
