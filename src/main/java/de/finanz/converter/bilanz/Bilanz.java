package de.finanz.converter.bilanz;

import de.finanz.converter.cash.AvailableCash;
import de.finanz.converter.io.CSVFinanzReader;
import de.finanz.converter.kategorie.Categorie;
import de.finanz.converter.kategorie.ECategoryType;
import de.finanz.converter.stocks.SharedHeld;
import de.finanz.converter.stocks.StockPrice;
import de.finanz.converter.transaction.Transaction;
import de.finanz.converter.transaction.TransactionHelper;
import lombok.Getter;

import java.io.IOException;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Bilanz {
    private List<SharedHeld> sharedHelds;
    private List<StockPrice> stockPrices;
    private List<Transaction> transactions;
    private List<AvailableCash> availableCashes;
    private Map<ECategoryType, Categorie> categories;

    public Bilanz() throws IOException {
        CSVFinanzReader csvFinanzReader = new CSVFinanzReader();
        TransactionHelper transactionHelper = new TransactionHelper();
        this.categories = new HashMap<>();
        this.stockPrices = csvFinanzReader.readAllStockPrices();
        this.sharedHelds = csvFinanzReader.readAllSharedHelds();
        this.availableCashes = csvFinanzReader.readAvailableCash();
        // TODO TransactionHelper schöner machen
        transactions = transactionHelper.readAndNormalizeTransactions();
        transactions.stream()
                .map(TransactionHelper::mapToCategorie)
                .forEach(this::addCategoryValues);
    }

    public void addCategoryValues(Categorie k) {
        Categorie currentCategorie = getCategory(k.getType());
        k.getValues().forEach((currentCategorie::addValue));
        categories.put(currentCategorie.getType(), currentCategorie);
    }

    public Collection<Categorie> getAllCategories() {
        return categories.values();
    }

    public Categorie getCategory(ECategoryType eCategoryType) {
        return categories.getOrDefault(eCategoryType, new Categorie(eCategoryType));
    }

    public Double getCategoryValue(ECategoryType categoryType, Month month) {
        if (categories.containsKey(categoryType)) {
            return categories.get(categoryType).getValue(month);
        }
        return 0d;
    }

    public List<Month> getMonthsSorted() {
        return getCategories().values()
                .stream()
                .map(k -> k.getValues().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}