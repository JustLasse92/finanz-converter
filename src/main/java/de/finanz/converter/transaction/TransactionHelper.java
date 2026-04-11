package de.finanz.converter.transaction;

import de.finanz.converter.exception.FinanzConverterException;
import de.finanz.converter.io.CSVFinanzReader;
import de.finanz.converter.kategorie.Categorie;
import de.finanz.converter.kategorie.ECategoryType;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

public class TransactionHelper {
    private final CSVFinanzReader csvFinanzReader = new CSVFinanzReader();

    public static Categorie mapToCategorie(Transaction transaction) {
        List<ECategoryType> categoryTypeList = Arrays.stream(ECategoryType.values())
                .filter(e -> e.matches(transaction))
                .toList();
        if (categoryTypeList.size() != 1) {
            throw new FinanzConverterException("Anzahl Matches von CategoryType ist " + categoryTypeList.size() + " von: \n " + transaction);
        }

        Categorie categorie = new Categorie(categoryTypeList.getFirst());
        YearMonth month = transaction.getYearMonthOfBuchungsdatum();
        categorie.addValue(month, transaction.getBetrag());
        return categorie;
    }

    public List<Transaction> readAndNormalizeTransactions() throws IOException {
        List<Transaction> transactions = csvFinanzReader.readTransactions();
        removeExcludedTransactions(transactions);
        addAdditionalTransactions(transactions);
        return transactions;
    }

    private void addAdditionalTransactions(List<Transaction> transactions) throws IOException {
        transactions.addAll(csvFinanzReader.readAdditionalTransactions());
    }

    private void removeExcludedTransactions(List<Transaction> transactions) throws IOException {
        for (Transaction excludedTransaction : csvFinanzReader.readExcludedTransactions()) {
            List<Transaction> transactionToRemove = transactions.stream()
                    .filter(transaction -> transaction.almostEqual(excludedTransaction))
                    .toList();

            if (transactionToRemove.size() != 1) {
                throw new FinanzConverterException("Anzahl Matches von excluded Transaction ist " +
                        transactionToRemove.size() + " von: \n " + excludedTransaction);
            }

            transactions.remove(transactionToRemove.getFirst());
        }
    }


}
