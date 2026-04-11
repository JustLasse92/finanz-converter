package de.finanz.converter.stocks;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import lombok.Data;

import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@Data
public class SharedHeld {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    @CsvBindByName(column = "Datum")
    @CsvDate("dd.MM.yy")
    private Calendar datum;

    @CsvBindByName(column = "Name")
    private String name;


    @CsvBindByName(column = "Gehaltene Anteile")
    @CsvNumber(value = "#.##")
    private Double gehalteneAnteile;

    @Override
    public String toString() {
        return "Transaction{" +
                "\ndatum=" + DATE_TIME_FORMATTER.format(this.datum.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) +
                "\nname='" + name + '\'' +
                "\ngehalteneAnteile='" + gehalteneAnteile + '\'' +
                '}';
    }

    public YearMonth getYearMonth() {
        return YearMonth.of(getDatum().get(Calendar.YEAR),
                Month.of(getDatum().get(Calendar.MONTH) + 1));
    }
}
