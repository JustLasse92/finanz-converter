package de.finanz.converter.stocks;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import lombok.Data;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@Data
public class StockPrice {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    @CsvBindByName(column = "Datum")
    @CsvDate("dd.MM.yy")
    private Calendar datum;

    @CsvBindByName(column = "Name")
    private String name;


    @CsvBindByName(column = "Kurs")
    @CsvNumber(value = "#.##")
    private Double kurs;

    @Override
    public String toString() {
        return "Transaction{" +
                "\ndatum=" + DATE_TIME_FORMATTER.format(this.datum.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) +
                "\nname='" + name + '\'' +
                "\nkurs='" + kurs + '\'' +
                '}';
    }
}
