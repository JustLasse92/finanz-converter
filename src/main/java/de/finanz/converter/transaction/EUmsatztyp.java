package de.finanz.converter.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EUmsatztyp {
    AUSGANG("Ausgang"), EINGANG("Eingang");

    private String name;
}
