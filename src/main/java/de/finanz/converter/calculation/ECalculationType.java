package de.finanz.converter.calculation;

import de.finanz.converter.exception.FinanzConverterException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ECalculationType {
    EINNAMEN_GESAMT("Einnahmen Gesamt"),
    AUSLAGEN_EINGANG_GESAMT("Auslagen Eingang Gesamt"),
    AUSLAGEN_AUSGANG_GESAMT("Auslagen Ausgang Gesamt"),
    AUSGABEN_FIX("Fixe Ausgaben"),
    AUSGABEN_VARIABEL("Variable Ausgaben"),
    AUSGABEN_GESAMT("Gesamte Ausgaben"),
    UEBERSCHUSS_MONAT("Monatlicher Überschuss"),
    SPARRATE_GESAMT("Komplette Sparrate"),
    BILANZ_MONAT("Monatliche Bilanz"),
    CASH("Cash"),
    VANGUARD_FTSE_ALL_WORLD("Vanguard FTSE All-World"),
    ISHARES_NASDAQ_100("iShares Nasdaq 100"),
    BITCOIN("Bitcoin"),
    GIROKONTO_IST("Girokonto Ist"),
    GIROKONTO_DIFFERENZ("Girokonto Differenz");
    private String name;

    public static ECalculationType findByName(String name) {
        return Arrays.stream(ECalculationType.values())
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new FinanzConverterException("ECalculationType nicht gefunden mit name " + name));
    }
}
