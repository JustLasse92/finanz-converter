package de.finanz.converter.calculation;

import de.finanz.converter.exception.FinanzConverterException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ECalculationType {
    // Auslagen
    AUSLAGEN_EINGANG_GESAMT("Auslagen Eingang Gesamt"),
    AUSLAGEN_AUSGANG_GESAMT("Auslagen Ausgang Gesamt"),
    // Einnahmen
    EINNAMEN_GESAMT("Einnahmen Gesamt"),
    // Ausgaben
    GIROKONTO_AUSGABEN_FIX("Girokonto fixe Ausgaben"),
    GIROKONTO_AUSGABEN_VARIABEL("Girokonto variable Ausgaben"),
    BARGELD_AUSGABEN("Bargeldausgaben"),
    AUSGABEN_GESAMT("Gesamte Ausgaben"),
    // Sparrate
    UEBERSCHUSS_MONAT("Monatlicher Überschuss"),
    SPARPLAN("Sparplan"),
    RENDITE_DURCHSCHNITT("Erwartete Rendite (Durchschnitt)"),
    SPARRATE_GESAMT("Komplette Sparrate"),
    // Differenz in den Berechnungen
    CASH_DIFFERENZ("Cash Differenz"),
    GIROKONTO_DIFFERENZ("Girokonto Differenz"),
    // Bilanz
    BILANZ_MONAT("Monatliche Bilanz"),
    // Kontostände
    GIROKONTO_IST("Girokonto (Monatsende)"),
    GIROKONTO_MIN("Girokonto (Minimum)"),
    CASH_IST("Summe Cash (Monatsende)"),
    CASH_MIN("Summe Cash (Minimum)"),
    // Wertpapiere
    VERRECHNUNGSKONTO("Verrechnungskonto"),
    VANGUARD_FTSE_ALL_WORLD("Vanguard FTSE All-World"),
    ISHARES_NASDAQ_100("iShares Nasdaq 100"),
    BITCOIN("Bitcoin"),
    // Jahreswerte
    SPARPLAN_DURCHSCHNITT("Durchschnittlicher Sparplan"),
    SPARRATE_DURCHSCHNITT("Durchschnittlicher Sparrate"),
    SPARRATE_DURCHSCHNITT_MIT_RENDITE("Durchschnittlicher Sparrate mit Rendite");
    private String name;

    public static ECalculationType findByName(String name) {
        return Arrays.stream(ECalculationType.values())
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new FinanzConverterException("ECalculationType nicht gefunden mit name " + name));
    }
}
