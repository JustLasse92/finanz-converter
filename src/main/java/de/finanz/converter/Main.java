package de.finanz.converter;


import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.io.CSVExporter;
import de.finanz.converter.stocks.StockPrice;
import de.finanz.converter.stocks.StockPriceHelper;
import de.finanz.converter.transaction.Transaction;
import de.finanz.converter.transaction.TransactionHelper;

import java.io.IOException;
import java.util.List;


public class Main {


    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = new TransactionHelper().readAndNormalizeTransactions();
        List<StockPrice> stockPrices = new StockPriceHelper().readAllStockPrices();

        Bilanz bilanz = new Bilanz(transactions, stockPrices);

        CSVExporter.export(bilanz, "bilanz.csv");

    }
}