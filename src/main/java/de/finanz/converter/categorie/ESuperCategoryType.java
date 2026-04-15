package de.finanz.converter.categorie;

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
    MOBILITAET("Mobilität"),
    ARBEIT_STUDIUM("Arbeit und Studium"),
    ENTERTAINMENT("Entertainment"),
    SONSTIGE("Sonstige");

    private String name;
}
