package de.finanz.converter.stocks;

import de.finanz.converter.io.CSVReaderStockPrice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StockPriceHelper {

    private static Path STOCK_PRICE_PATH = Path.of(System.getenv("STOCK_PRICE_PATH"));

    private CSVReaderStockPrice csvReader = new CSVReaderStockPrice();

    public List<StockPrice> readAllStockPrices() throws IOException {
        assert Files.isRegularFile(STOCK_PRICE_PATH);
        return csvReader.readTransactions(STOCK_PRICE_PATH);
    }
}
