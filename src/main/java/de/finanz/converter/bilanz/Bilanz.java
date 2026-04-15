package de.finanz.converter.bilanz;

import de.finanz.converter.cash.AvailableCash;
import de.finanz.converter.cash.EAvailableCashTyp;
import de.finanz.converter.categorie.Categorie;
import de.finanz.converter.categorie.ECategoryType;
import de.finanz.converter.exception.FinanzConverterException;
import de.finanz.converter.io.CSVFinanzReader;
import de.finanz.converter.stocks.SharedHeld;
import de.finanz.converter.stocks.StockPrice;
import de.finanz.converter.transaction.Transaction;
import de.finanz.converter.transaction.TransactionHelper;
import lombok.Getter;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class Bilanz {
    private final List<SharedHeld> sharedHelds;
    private final List<StockPrice> stockPrices;
    private final List<Transaction> allTransactions;
    private final List<AvailableCash> availableCashes;
    private final Map<ECategoryType, Categorie<ECategoryType>> categories;
    private final double umsatz2023;

    public Bilanz() throws IOException {
        CSVFinanzReader csvFinanzReader = new CSVFinanzReader();

        TransactionHelper transactionHelper = new TransactionHelper();
        this.categories = new HashMap<>();
        this.stockPrices = csvFinanzReader.readAllStockPrices();
        this.sharedHelds = csvFinanzReader.readAllSharedHelds();
        this.availableCashes = csvFinanzReader.readAvailableCash();
        this.allTransactions = csvFinanzReader.readTransactions();
        this.umsatz2023 = csvFinanzReader.readSummeUmsaetze2023();

        // TODO TransactionHelper schöner machen
        transactionHelper.getNormalizedTransactions(allTransactions).stream()
                .map(TransactionHelper::mapToCategorie)
                .forEach(this::addCategoryValues);
    }

    public void addCategoryValues(Categorie<ECategoryType> k) {
        Categorie<ECategoryType> currentCategorie = getCategory(k.getType());
        k.getValues().forEach((currentCategorie::addValue));
        categories.put(currentCategorie.getType(), currentCategorie);
    }

    public Collection<Categorie<ECategoryType>> getAllCategories() {
        return categories.values();
    }

    public Categorie<ECategoryType> getCategory(ECategoryType eCategoryType) {
        return categories.getOrDefault(eCategoryType, new Categorie<>(eCategoryType));
    }

    public Double getCategoryValue(ECategoryType categoryType, YearMonth yearMonth) {
        if (categories.containsKey(categoryType)) {
            return categories.get(categoryType).getValue(yearMonth);
        }
        return 0d;
    }

    public Optional<AvailableCash> getAvailableCashesInYearMonths(EAvailableCashTyp typ, YearMonth yearMonth) {
        List<AvailableCash> availableCashList = getAvailableCashes().stream()
                .filter(cash -> cash.getTyp().equals(typ))
                .filter(cash -> cash.getYearMonthOfDatum().equals(yearMonth))
                .toList();
        if (availableCashList.size() > 1) {
            throw new FinanzConverterException("Es wurde ein Eintrag in AvailableCash in " + yearMonth + " für typ " + typ.getBezeichnung() + " erwartet. Gefunden: " + availableCashList.size());
        }
        return availableCashList.isEmpty() ? Optional.empty() : Optional.of(availableCashList.getFirst());
    }

    public List<YearMonth> getYearMonthsSorted() {
        return getCategories().values()
                .stream()
                .map(k -> k.getValues().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}