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
    private final List<Transaction> expensesTransactions;

    public TransactionHelper() throws IOException {
        this.expensesTransactions = csvFinanzReader.readExpensesTransactions();
    }

    public Categorie<ECategoryType> mapToCategorie(Transaction transaction) {
        // Vordefinierte Auslagen werden direkt kategorisiert
        ECategoryType categoryType = null;
        for (Transaction expensesTransaction : expensesTransactions) {
            if (expensesTransaction.almostEqual(transaction)) {
                categoryType = transaction.getUmsatztyp().equals(EUmsatztyp.AUSGANG) ?
                        ECategoryType.AUSLAGEN_AUSGANG : ECategoryType.AUSLAGEN_EINGANG;
                break;
            }
        }

        // Alle anderen Kategorien werden über die Matcher bestimmt
        if (categoryType == null) {
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
