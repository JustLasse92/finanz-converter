package org.example;


import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException {
        Reader in = new FileReader("/home/lasse/Dokumente/Finanzen/Kontoauszüge/DE57 1203 0000 1064 5451 04 DKB " +
                "Girokonto/2026/Januar/05-04-2026_Umsatzliste_Girokonto_DE57120300001064545104.csv");

        List<Transaction> transactions = new CsvToBeanBuilder(in).withType(Transaction.class).build().parse();

        Bilanz bilanz = new Bilanz();

        transactions.stream()
                .map(TransactionMapper::mapToCategorie)
                .forEach(bilanz::addCategorieValues);


        CSVExporter.export(bilanz, "bilanz.csv");

    }
}