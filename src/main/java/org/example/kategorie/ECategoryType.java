package org.example.kategorie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.EUmsatztyp;
import org.example.Transaction;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.example.TransactionCategoryMatcher.containsBuchungsdatum;
import static org.example.TransactionCategoryMatcher.containsOneEmpfaenger;
import static org.example.TransactionCategoryMatcher.containsSender;
import static org.example.TransactionCategoryMatcher.containsVerwendungszweck;
import static org.example.TransactionCategoryMatcher.isUmsatztyp;

@AllArgsConstructor
@Getter
public enum ECategoryType {
    ZINSEN("Zinsen") {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsVerwendungszweck(transaction, "Steuerausgleich") && isUmsatztyp(transaction,
                    EUmsatztyp.EINGANG));
        }
    },
    SPENDEN("Spenden") {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "DRK-SPENDENKONTO") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    GESUNDHEIT_ZUSATZVERSICHERUNG("Gesundheit Zusatzversicherung") {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "Barmenia Krankenvers") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    LAGERBOX("Lagerbox") {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsVerwendungszweck(transaction, "Safe-Box") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    HANDY("Handy") {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsVerwendungszweck(transaction, "Aldi Talk") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    HAFTPFLICHTVERSICHERUNG("Haftpflichtversicherung") {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsVerwendungszweck(transaction, "Haftpflicht") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    IGM("IGM") {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "Industriegewerkschaft Metall") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    SPORT("Sport") {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "cf Fitness", "EF Fitness GmbH") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    VERSANDKOSTEN("Versandkosten") {
        @Override
        public boolean matches(Transaction transaction) {
            // Käufe und Rückzahlungen
            return (containsOneEmpfaenger(transaction, "Deutsche.Post") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    MOEBEL_EINRICHTUNG("Möbel/Einrichtung") {
        @Override
        public boolean matches(Transaction transaction) {
            // Käufe und Rückzahlungen
            return (containsOneEmpfaenger(transaction, "IKEA") && isUmsatztyp(transaction, EUmsatztyp.AUSGANG)
                    || containsSender(transaction, "IKEA") && isUmsatztyp(transaction, EUmsatztyp.EINGANG));
        }
    },
    KLEIDUNG("Kleidung") {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "MODEHAUS", "Adler.Modemarkte")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    LEBENSMITTEL("Lebensmittel") {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "rewe", "DM.Drogerie", "LIDL", "COMBI", "Rossmann", "Inkoop")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    // Einkäufe/Rechnungen die ich für Stina getätigt habe
    VORRAUSZAHLUNGEN_STINA("Vorrauszahlungen Stina") {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "STAR..Bassum/Bassum")
                    && containsBuchungsdatum(transaction,
                    new GregorianCalendar(2026, Calendar.JANUARY, 26),
                    new GregorianCalendar(2026, Calendar.JANUARY, 12));
        }
    },
    // Geld das Stina für mich ausgelegt hat und hiermit zurückgezahlt wird
    AUSGLEICHZAHLUNGEN_STINA("Ausgleichzahlungen Stina") {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "stina kolander") && isUmsatztyp(transaction, EUmsatztyp.AUSGANG)
                    || containsSender(transaction, "stina kolander") && isUmsatztyp(transaction, EUmsatztyp.EINGANG));
        }
    },
    PRIVATVERKAUF("Privatverkauf") {
        @Override
        public boolean matches(Transaction transaction) {
            return containsVerwendungszweck(transaction, "kleinanzeigen")
                    && isUmsatztyp(transaction, EUmsatztyp.EINGANG);
        }
    },
    KLEINANZEIGEN("Kleinanzeigen") {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "Kleinanzeigen")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    SONSTIGES("Sonstiges") {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "paypal")
                    || containsSender(transaction, "paypal");
        }
    },
    GEHALT("Gehalt") {
        @Override
        public boolean matches(Transaction transaction) {
            return containsSender(transaction, "materna")
                    && isUmsatztyp(transaction, EUmsatztyp.EINGANG);
        }
    },
    SPARRATE("Sparrate") {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "scalable")
                    && containsVerwendungszweck(transaction, "sparplan")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    ARZT_MEDIKAMENTE("Arzt/Medikamente") {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "apotheke");
        }
    };

    private String name;

    public abstract boolean matches(Transaction transaction);
}
