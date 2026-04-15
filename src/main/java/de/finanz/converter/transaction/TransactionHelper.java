package de.finanz.converter.transaction;

import de.finanz.converter.categorie.Categorie;
import de.finanz.converter.categorie.ECategoryType;
import de.finanz.converter.exception.FinanzConverterException;
import de.finanz.converter.io.CSVFinanzReader;

import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.finanz.converter.TransactionCategoryMatcher.containsAnyEmpfaenger;
import static de.finanz.converter.TransactionCategoryMatcher.containsSender;

public class TransactionHelper {
    private static Integer YEAR = Integer.valueOf(System.getenv("YEAR"));
    private final CSVFinanzReader csvFinanzReader = new CSVFinanzReader();

    public static Categorie<ECategoryType> mapToCategorie(Transaction transaction) {
        List<ECategoryType> categoryTypeList = Arrays.stream(ECategoryType.values())
                .filter(e -> e.matches(transaction))
                .toList();
        if (categoryTypeList.size() != 1) {
            throw new FinanzConverterException("Es wird ein Match von CategoryType erwartet. Gefunden wurden: " + categoryTypeList + "\nvon: \n " + transaction);
        }

        Categorie<ECategoryType> categorie = new Categorie<>(categoryTypeList.getFirst());
        YearMonth month = transaction.getYearMonthOfBuchungsdatum();
        categorie.addValue(month, transaction.getBetrag());
        return categorie;
    }

    public List<Transaction> getNormalizedTransactions(List<Transaction> allTransactions) throws IOException {
        List<Transaction> transactions = new ArrayList<>(allTransactions);
        removeIrrelevantTransactions(transactions);
        removeExcludedTransactions(transactions);
        addAdditionalTransactions(transactions);
        return transactions;
    }

    private void removeIrrelevantTransactions(List<Transaction> transactions) {
        transactions.removeIf(transaction -> {
            // Beträge die 0 sind brauchen nicht betrachtet werden
            return transaction.getBetrag() == 0
                    // Eingänge vom Tagesgeldkonto sind nicht relevant
                    || (containsAnyEmpfaenger(transaction, "Lasse Ganske")
                    && containsSender(transaction, "Lasse Ganske"))
                    // Nur Einträge für das aktuelle Jahr sind relevant
                    || transaction.getYearMonthOfBuchungsdatum().getYear() != YEAR;
        });
    }

    private void addAdditionalTransactions(List<Transaction> transactions) throws IOException {
        transactions.addAll(csvFinanzReader.readAdditionalTransactions());
    }

    private void removeExcludedTransactions(List<Transaction> transactions) throws IOException {
        List<Integer> yearsOfTransactions = transactions.stream()
                .map(Transaction::getYearMonthOfBuchungsdatum)
                .map(YearMonth::getYear)
                .distinct()
                .toList();
        for (Transaction excludedTransaction : csvFinanzReader.readExcludedTransactions()) {
            if (!yearsOfTransactions.contains(excludedTransaction.getYearMonthOfBuchungsdatum().getYear())) {
                // Es sollen nur Transaktionen entfernt werden, wenn diese auch zum Jahr der Transaktionen gehören
                continue;
            }

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
