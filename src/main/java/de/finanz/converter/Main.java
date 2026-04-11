package de.finanz.converter;


import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.io.CSVExporter;

import java.io.IOException;


public class Main {


    public static final String OUTPUT_FILE_NAME = "bilanz.csv";

    public static void main(String[] args) throws IOException {


        Bilanz bilanz = new Bilanz();

        CSVExporter.export(bilanz, OUTPUT_FILE_NAME);

    }
}