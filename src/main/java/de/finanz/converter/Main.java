package de.finanz.converter;


import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.io.CSVExporter;
import de.finanz.converter.stocks.SharedHeld;
import de.finanz.converter.stocks.StockHelper;
import de.finanz.converter.stocks.StockPrice;
import de.finanz.converter.transaction.Transaction;
import de.finanz.converter.transaction.TransactionHelper;

import java.io.IOException;
import java.util.List;


public class Main {


    public static final String OUTPUT_FILE_NAME = "bilanz.csv";

    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = new TransactionHelper().readAndNormalizeTransactions();
        StockHelper stockHelper = new StockHelper();
        List<StockPrice> stockPrices = stockHelper.readAllStockPrices();
        List<SharedHeld> sharedHelds = stockHelper.readAllSharedHelds();

        Bilanz bilanz = new Bilanz(transactions, stockPrices, sharedHelds);

        CSVExporter.export(bilanz, OUTPUT_FILE_NAME);

    }
}