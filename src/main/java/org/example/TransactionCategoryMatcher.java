package org.example;

import java.util.Arrays;
import java.util.Calendar;

public class TransactionCategoryMatcher {

    public static boolean containsSender(Transaction transaction, String sender) {
        return transaction.getSender().toLowerCase().contains(sender.toLowerCase());
    }

    public static boolean containsVerwendungszweck(Transaction transaction, String verwendungszweck) {
        return transaction.getVerwendungszweck().toLowerCase().contains(verwendungszweck.toLowerCase());
    }

    public static boolean containsOneEmpfaenger(Transaction transaction, String... empfaenger) {
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
