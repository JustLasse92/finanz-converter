package org.example;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import lombok.Data;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@Data
public class Transaction {

    @CsvBindByName(column = "\uFEFFBuchungsdatum", required = true)
    @CsvDate("dd.MM.yy")
    private Calendar buchungsdatum;

    @CsvBindByName(column = "Zahlungspflichtige*r", required = true)
    private String sender;

    @CsvBindByName(column = "Zahlungsempfänger*in", required = true)
    private String empfaenger;

    @CsvBindByName(column = "Verwendungszweck", required = true)
    private String verwendungszweck;

    @CsvBindByName(column = "Umsatztyp", required = true)
    private EUmsatztyp umsatztyp;

    @CsvBindByName(column = "Betrag (€)", required = true)
    @CsvNumber(value = "#.##")
    private Double betrag;

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");

        String formattedBuchungsdatum = formatter.format(
                this.buchungsdatum.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        );

        return "Transaction(" +
//                "\nbuchungsdatum=" + this.buchungsdatum +
                "\nbuchungsdatum=" + formattedBuchungsdatum +
                "\nsender=" + this.getSender() +
                "\nempfaenger=" + this.getEmpfaenger() +
                "\nverwendungszweck=" + this.getVerwendungszweck() +
                "\numsatztyp=" + this.getUmsatztyp() +
                "\nbetrag=" + this.getBetrag() + ")";
    }


}
