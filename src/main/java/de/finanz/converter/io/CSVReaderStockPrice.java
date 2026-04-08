package de.finanz.converter.io;

import de.finanz.converter.stocks.StockPrice;

public class CSVReaderStockPrice extends CSVReader<StockPrice> {
    public CSVReaderStockPrice() {
        super(StockPrice.class);
    }
}
