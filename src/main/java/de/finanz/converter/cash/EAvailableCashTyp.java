package de.finanz.converter.cash;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EAvailableCashTyp {
    BARGELD("Bargeld"),
    TAGESGELDKONTO("Tagesgeldkonto"),
    VERRECHNUNGSKONTO("Verrechnungskonto"),
    AKTIEN_VL("Aktien VL");
    private String bezeichnung;
}
