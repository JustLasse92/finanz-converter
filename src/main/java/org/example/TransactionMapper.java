package org.example;

import org.example.kategorie.Categorie;
import org.example.kategorie.ECategoryType;

import java.time.Month;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TransactionMapper {

    public static Categorie mapToCategorie(Transaction transaction) {
        List<ECategoryType> categoryTypeList = Arrays.stream(ECategoryType.values())
                .filter(e -> e.matches(transaction))
                .toList();
        if (categoryTypeList.size() != 1) {
            throw new RuntimeException("Anzahl Matches von CategoryType ist " + categoryTypeList.size() + " von: \n " + transaction);
        }

        Categorie categorie = new Categorie(categoryTypeList.getFirst());
        Month month = Month.of(transaction.getBuchungsdatum().get(Calendar.MONTH) + 1);
        categorie.addValue(month, transaction.getBetrag());
        return categorie;
    }


}
