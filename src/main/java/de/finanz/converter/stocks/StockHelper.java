package de.finanz.converter.stocks;

import de.finanz.converter.io.CSVReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StockHelper {

    private static Path STOCK_PRICE_PATH = Path.of(System.getenv("STOCK_PRICE_PATH"));
    private static Path SHARED_HELD_PATH = Path.of(System.getenv("SHARED_HELD_PATH"));

    private CSVReader csvReader = new CSVReader();

    public List<SharedHeld> readAllSharedHelds() throws IOException {
        assert Files.isRegularFile(SHARED_HELD_PATH);
        return csvReader.readCSV(SHARED_HELD_PATH, SharedHeld.class);
    }

    public List<StockPrice> readAllStockPrices() throws IOException {
        assert Files.isRegularFile(STOCK_PRICE_PATH);
        return csvReader.readCSV(STOCK_PRICE_PATH, StockPrice.class);
    }
}
