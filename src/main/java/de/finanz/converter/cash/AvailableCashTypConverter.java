package de.finanz.converter.cash;

import com.opencsv.bean.AbstractBeanField;
import de.finanz.converter.exception.FinanzConverterException;

import java.util.Arrays;

public class AvailableCashTypConverter extends AbstractBeanField<EAvailableCashTyp, String> {
    @Override
    protected Object convert(String value) {
        return Arrays.stream(EAvailableCashTyp.values())
                .filter(e -> e.getBezeichnung().equals(value))
                .findFirst()
                .orElseThrow(() -> new FinanzConverterException("Konnte " + value + " nicht auf " + EAvailableCashTyp.class + " mappen"));
    }
}
