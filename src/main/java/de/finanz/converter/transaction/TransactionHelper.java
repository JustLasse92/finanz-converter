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

public class TransactionHelper {
    private final CSVFinanzReader csvFinanzReader = new CSVFinanzReader();
    private final List<Transaction> transactionsAdditionalContext;

    public TransactionHelper() throws IOException {
        this.transactionsAdditionalContext = csvFinanzReader.readTransactionsAdditionalContext();
    }

    public Categorie<ECategoryType> mapToCategorie(Transaction transaction) {
        // Vordefinierte Auslagen werden direkt kategorisiert
        ECategoryType categoryType = null;
        EAdditionalCategory additionalCategory = transaction.getAdditionalCategory();
        if (additionalCategory != null) {
            if (additionalCategory == EAdditionalCategory.AUSLAGEN) {
                categoryType = transaction.getUmsatztyp().equals(EUmsatztyp.AUSGANG) ?
                        ECategoryType.AUSLAGEN_AUSGANG : ECategoryType.AUSLAGEN_EINGANG;
            } else {
                throw new FinanzConverterException("Unexpected value: " + additionalCategory);
            }
        } else {
            // Alle anderen Kategorien werden über die Matcher bestimmt
            List<ECategoryType> categoryTypeList = Arrays.stream(ECategoryType.values())
                    .filter(e -> e.matches(transaction))
                    .toList();
            if (categoryTypeList.size() != 1) {
                throw new FinanzConverterException("Es wird ein Match von CategoryType erwartet. Gefunden wurden: " + categoryTypeList + "\nvon: \n " + transaction);
            }
            categoryType = categoryTypeList.getFirst();
        }

        Categorie<ECategoryType> categorie = new Categorie<>(categoryType);
        YearMonth month = transaction.getYearMonthOfBuchungsdatum();
        categorie.addValue(month, transaction.getBetrag());
        return categorie;
    }

    public List<Transaction> getNormalizedTransactions(List<Transaction> allTransactions) {
        List<Transaction> transactions = new ArrayList<>(allTransactions);
        removeIrrelevantTransactions(transactions);
        return transactions;
    }

    public void replaceWithAdditionalTransactionContext(List<Transaction> transactions) {
        outer:
        for (Transaction transactionWithAdditionalContext : this.transactionsAdditionalContext) {
            for (int i = 0; i < transactions.size(); i++) {
                if (transactions.get(i).almostEqual(transactionWithAdditionalContext)) {
                    transactions.remove(i);
                    transactions.add(i, transactionWithAdditionalContext);
                    continue outer;
                }
            }
            throw new FinanzConverterException("Konnte keine passende Transaction finden zu: " + transactionWithAdditionalContext);
        }
    }

    private void removeIrrelevantTransactions(List<Transaction> transactions) {
        YearMonth now = YearMonth.now();

        transactions.removeIf(transaction -> {
            // Beträge die 0 sind brauchen nicht betrachtet werden
            YearMonth yearMonth = transaction.getYearMonthOfBuchungsdatum();
            return transaction.getBetrag() == 0
                    // Nur Einträge für das aktuelle Jahr sind relevant
                    || yearMonth.getYear() != now.getYear()
                    // Nur abgeschlossene Monate werden berechnet
                    || !yearMonth.isBefore(now);
        });
    }


}
