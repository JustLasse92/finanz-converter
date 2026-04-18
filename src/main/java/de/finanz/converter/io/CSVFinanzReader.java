package de.finanz.converter.io;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import de.finanz.converter.cash.AvailableCash;
import de.finanz.converter.stocks.SharedHeld;
import de.finanz.converter.stocks.StockPrice;
import de.finanz.converter.transaction.Transaction;
import lombok.AllArgsConstructor;
import org.apache.commons.io.input.BOMInputStream;

import java.io.FileReader;
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
public class CSVFinanzReader {

    private static final Path TRANSACTIONS_ROOT_PATH = Path.of(System.getenv("TRANSACTIONS_ROOT_PATH"));
    private static final Path STOCK_PRICE_PATH = resolvePath("STOCK_PRICE_FILE_PATH");
    private static final Path EXPENSES_TRANSACTIONS_FILE_PATH = resolvePath("EXPENSES_TRANSACTIONS_FILE_PATH");
    private static final Path SHARED_HELD_PATH = resolvePath("SHARED_HELD_FILE_PATH");
    private static final Path AVAILABLE_CASH_PATH = resolvePath("AVAILABLE_CASH_FILE_PATH");
    private static final Path TRANSACTIONS_SINGLE_2023_PATH = resolvePath("TRANSACTIONS_SINGLE_2023_FILE_PATH");

    private static Path resolvePath(String env) {
        return TRANSACTIONS_ROOT_PATH.resolve(Path.of(System.getenv(env)));
    }

    public double readSummeUmsaetze2023() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(TRANSACTIONS_SINGLE_2023_PATH.toFile()))) {
            List<String[]> rows = reader.readAll();
            return rows.stream()
                    .map(row -> row[0])
                    .mapToDouble(Double::parseDouble)
                    .sum();
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AvailableCash> readAvailableCash() throws IOException {
        return readCSV(AVAILABLE_CASH_PATH, AvailableCash.class);
    }

    public List<Transaction> readTransactions() throws IOException {
        return readCSV(TRANSACTIONS_ROOT_PATH, Transaction.class);
    }

    public List<Transaction> readExpensesTransactions() throws IOException {
        return readCSV(EXPENSES_TRANSACTIONS_FILE_PATH, Transaction.class);
    }

    public List<SharedHeld> readAllSharedHelds() throws IOException {
        assert Files.isRegularFile(SHARED_HELD_PATH);
        return readCSV(SHARED_HELD_PATH, SharedHeld.class);
    }

    public List<StockPrice> readAllStockPrices() throws IOException {
        assert Files.isRegularFile(STOCK_PRICE_PATH);
        return readCSV(STOCK_PRICE_PATH, StockPrice.class);
    }

    private <T> List<T> readCSV(Path path, Class<T> type) throws IOException {
        if (Files.isDirectory(path)) {
            return readCSVFolder(path, type);
        } else {
            return readCSVFile(path, type);
        }
    }

    private <T> List<T> readCSVFolder(Path path, Class<T> type) throws IOException {
        List<T> transactions = new ArrayList<>();
        try (Stream<Path> paths = Files.list(path)) {
            List<Path> files = paths.filter(p -> p.toString().endsWith(".csv"))
                    .toList();
            for (Path p : files) {
                transactions.addAll(readCSVFile(p, type));
            }
        }
        return transactions;
    }

    private <T> List<T> readCSVFile(Path path, Class<T> type) throws IOException {
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
