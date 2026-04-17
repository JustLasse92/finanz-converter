package de.finanz.converter;

import de.finanz.converter.transaction.EUmsatztyp;
import de.finanz.converter.transaction.Transaction;

import java.util.Arrays;
import java.util.Calendar;

public class TransactionCategoryMatcher {

    public static boolean containsAnySender(Transaction transaction, String... sender) {
        String transactionSender = transaction.getSender().toLowerCase();
        return Arrays.stream(sender)
                .map(String::toLowerCase)
                .anyMatch(transactionSender::contains);
    }

    public static boolean containsAnyVerwendungszweck(Transaction transaction, String... verwendungszwecke) {
        String transactionVerwendungszweck = transaction.getVerwendungszweck().toLowerCase();
        return Arrays.stream(verwendungszwecke)
                .map(String::toLowerCase)
                .anyMatch(transactionVerwendungszweck::contains);
    }

    public static boolean containsAnyEmpfaenger(Transaction transaction, String... empfaenger) {
        String transactionEmpfaenger = transaction.getEmpfaenger().toLowerCase();
        return Arrays.stream(empfaenger)
                .map(String::toLowerCase)
                .anyMatch(transactionEmpfaenger::contains);
    }

    public static boolean isUmsatztyp(Transaction transaction, EUmsatztyp eUmsatztyp) {
        return transaction.getUmsatztyp().equals(eUmsatztyp);
    }

    public static boolean containsBuchungsdatum(Transaction transaction, Calendar... buchungsdaten) {
        return Arrays.stream(buchungsdaten)
                .anyMatch(cal -> transaction.getBuchungsdatum().equals(cal));
    }


}
