package de.finanz.converter.transaction;

import de.finanz.converter.exception.FinanzConverterException;
import de.finanz.converter.io.CSVReaderTransaction;
import de.finanz.converter.kategorie.Categorie;
import de.finanz.converter.kategorie.ECategoryType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TransactionHelper {

    private static Path TRANSACTIONS_PATH = Path.of(System.getenv("TRANSACTIONS_PATH"));
    private static Path TRANSACTIONS_ADDITIONAL_PATH = Path.of(System.getenv("TRANSACTIONS_ADDITIONAL_PATH"));
    private static Path TRANSACTIONS_EXCLUDED_PATH = Path.of(System.getenv("TRANSACTIONS_EXCLUDED_PATH"));

    private CSVReaderTransaction csvReader = new CSVReaderTransaction();

    public static Categorie mapToCategorie(Transaction transaction) {
        List<ECategoryType> categoryTypeList = Arrays.stream(ECategoryType.values())
                .filter(e -> e.matches(transaction))
                .toList();
        if (categoryTypeList.size() != 1) {
            throw new FinanzConverterException("Anzahl Matches von CategoryType ist " + categoryTypeList.size() + " von: \n " + transaction);
        }

        Categorie categorie = new Categorie(categoryTypeList.getFirst());
        Month month = Month.of(transaction.getBuchungsdatum().get(Calendar.MONTH) + 1);
        categorie.addValue(month, transaction.getBetrag());
        return categorie;
    }

    private void addAdditionalTransactions(List<Transaction> transactions) throws IOException {
        assert Files.isRegularFile(TRANSACTIONS_ADDITIONAL_PATH);

        transactions.addAll(csvReader.readTransactions(TRANSACTIONS_ADDITIONAL_PATH));
    }

    private void removeExcludedTransactions(List<Transaction> transactions) throws IOException {
        assert Files.isRegularFile(TRANSACTIONS_EXCLUDED_PATH);

        for (Transaction excludedTransaction : csvReader.readTransactions(TRANSACTIONS_EXCLUDED_PATH)) {
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

    public List<Transaction> readAndNormalizeTransactions() throws IOException {
        assert Files.isDirectory(TRANSACTIONS_PATH);
        List<Transaction> transactions = csvReader.readTransactions(TRANSACTIONS_PATH);
        removeExcludedTransactions(transactions);
        addAdditionalTransactions(transactions);
        return transactions;
    }


}
