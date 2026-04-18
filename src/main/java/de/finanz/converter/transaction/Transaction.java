package de.finanz.converter.transaction;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import lombok.Data;

import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@Data
public class Transaction {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    @CsvBindByName(column = "Buchungsdatum")
    @CsvDate("dd.MM.yy")
    private Calendar buchungsdatum;

    @CsvBindByName(column = "Wertstellung")
    @CsvDate("dd.MM.yy")
    private Calendar wertstellug;

    @CsvBindByName(column = "Zahlungspflichtige*r")
    private String sender;

    @CsvBindByName(column = "Zahlungsempfänger*in")
    private String empfaenger;

    @CsvBindByName(column = "Verwendungszweck")
    private String verwendungszweck;

    @CsvCustomBindByName(column = "Status", converter = EmptyToNullConverter.class)
    private String status;

    @CsvCustomBindByName(column = "IBAN", converter = EmptyToNullConverter.class)
    private String iban;

    @CsvCustomBindByName(column = "Gläubiger-ID", converter = EmptyToNullConverter.class)
    private String glaeubigerID;

    @CsvCustomBindByName(column = "Mandatsreferenz", converter = EmptyToNullConverter.class)
    private String mandatsreferenz;

    @CsvCustomBindByName(column = "Kundenreferenz", converter = EmptyToNullConverter.class)
    private String kundenreferenz;

    @CsvBindByName(column = "Umsatztyp")
    private EUmsatztyp umsatztyp;

    @CsvBindByName(column = "Betrag (€)", required = true)
    @CsvNumber(value = "#.##")
    private Double betrag;


    @Override
    public String toString() {
        return "Transaction{" +
                "\nbuchungsdatum=" + DATE_TIME_FORMATTER.format(this.buchungsdatum.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) +
                "\nwertstellug=" + DATE_TIME_FORMATTER.format(this.wertstellug.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) +
                "\nsender='" + sender + '\'' +
                "\nempfaenger='" + empfaenger + '\'' +
                "\nverwendungszweck='" + verwendungszweck + '\'' +
                "\nstatus='" + status + '\'' +
                "\niban='" + iban + '\'' +
                "\nglaeubigerID='" + glaeubigerID + '\'' +
                "\nmandatsreferenz='" + mandatsreferenz + '\'' +
                "\nkundenreferenz='" + kundenreferenz + '\'' +
                "\numsatztyp=" + umsatztyp +
                "\nbetrag=" + betrag +
                '}';
    }

    public boolean almostEqual(Transaction transaction) {
        if (this.equals(transaction)) {
            return true;
        }

        return transaction != null
                && (transaction.getBuchungsdatum() == null || this.buchungsdatum.equals(transaction.getBuchungsdatum()))
                && (transaction.getWertstellug() == null || this.getWertstellug().equals(transaction.getWertstellug()))
                && (transaction.getSender() == null || this.getSender().equals(transaction.getSender()))
                && (transaction.getEmpfaenger() == null || this.getEmpfaenger().equals(transaction.getEmpfaenger()))
                && (transaction.getVerwendungszweck() == null || this.getVerwendungszweck().equals(transaction.getVerwendungszweck()))
                && (transaction.getStatus() == null || this.getStatus().equals(transaction.getStatus()))
                && (transaction.getIban() == null || this.getIban().equals(transaction.getIban()))
                && (transaction.getGlaeubigerID() == null || this.getGlaeubigerID().equals(transaction.getGlaeubigerID()))
                && (transaction.getMandatsreferenz() == null || this.getMandatsreferenz().equals(transaction.getMandatsreferenz()))
                && (transaction.getKundenreferenz() == null || this.getKundenreferenz().equals(transaction.getKundenreferenz()))
                && (transaction.getUmsatztyp() == null || this.getUmsatztyp().equals(transaction.getUmsatztyp()))
                && (transaction.getBetrag() == null || this.getBetrag().equals(transaction.getBetrag()));
    }

    public YearMonth getYearMonthOfBuchungsdatum() {
        return YearMonth.of(getBuchungsdatum().get(Calendar.YEAR),
                Month.of(getBuchungsdatum().get(Calendar.MONTH) + 1));
    }
}
