package de.finanz.converter.cash;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import lombok.Getter;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@Getter
public class AvailableCash {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    @CsvBindByName(column = "Datum")
    @CsvDate("dd.MM.yy")
    private Calendar datum;

    @CsvCustomBindByName(column = "Art", converter = AvailableCashTypConverter.class)
    private EAvailableCashTyp typ;

    @CsvBindByName(column = "Betrag")
    @CsvNumber(value = "#.##")
    private Double betrag;

    @Override
    public String toString() {
        return "AvailableCash{" +
                "\ndatum=" + DATE_TIME_FORMATTER.format(this.datum.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) +
                "\ntyp=" + typ +
                "\nbetrag='" + betrag +
                '}';
    }
}