package de.finanz.converter.kategorie;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ESuperCategoryType {
    EINKOMMEN("Einkommen"),
    WOHNEN("Wohnen"),
    VERSICHERUNGEN("Versicherungen"),
    SONSTIGE_VERTRAEGE("Sonstige Verträge"),
    LEBENSHALTUNG("Lebenshaltung"),
    AUTO_TANKEN("Auto und Tanken"),
    ENTERTAINMENT("Entertainment"),
    SONSTIGE("Sonstige");

    private String name;
}
