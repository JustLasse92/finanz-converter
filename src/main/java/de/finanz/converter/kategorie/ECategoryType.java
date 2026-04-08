package de.finanz.converter.kategorie;

import de.finanz.converter.transaction.EUmsatztyp;
import de.finanz.converter.transaction.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static de.finanz.converter.TransactionCategoryMatcher.containsOneEmpfaenger;
import static de.finanz.converter.TransactionCategoryMatcher.containsOneVerwendungszweck;
import static de.finanz.converter.TransactionCategoryMatcher.containsSender;
import static de.finanz.converter.TransactionCategoryMatcher.isUmsatztyp;

@AllArgsConstructor
@Getter
public enum ECategoryType {
    BARGELDAUSGABEN("Bargeldausgaben", ESuperCategoryType.SONSTIGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneVerwendungszweck(transaction, "Bargeldausgaben")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG));
        }
    },
    MIETE("Miete", ESuperCategoryType.WOHNEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneVerwendungszweck(transaction, "Miete")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG));
        }
    },
    ZINSEN("Zinsen", ESuperCategoryType.EINKOMMEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneVerwendungszweck(transaction, "Steuerausgleich") && isUmsatztyp(transaction,
                    EUmsatztyp.EINGANG));
        }
    },
    SPENDEN("Spenden", ESuperCategoryType.SONSTIGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "DRK-SPENDENKONTO") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    GESUNDHEIT_ZUSATZVERSICHERUNG("Gesundheit Zusatzversicherung", ESuperCategoryType.VERSICHERUNGEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "Barmenia Krankenvers") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    LAGERBOX("Lagerbox", ESuperCategoryType.SONSTIGE_VERTRAEGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneVerwendungszweck(transaction, "Safe-Box") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    HANDY("Handy", ESuperCategoryType.SONSTIGE_VERTRAEGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneVerwendungszweck(transaction, "Aldi Talk") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    HAFTPFLICHTVERSICHERUNG("Haftpflichtversicherung", ESuperCategoryType.VERSICHERUNGEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneVerwendungszweck(transaction, "Haftpflicht") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    IGM("IGM", ESuperCategoryType.VERSICHERUNGEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "Industriegewerkschaft Metall") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    SPORT("Sport", ESuperCategoryType.SONSTIGE_VERTRAEGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "cf Fitness", "EF Fitness GmbH") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    VERSANDKOSTEN("Versandkosten", ESuperCategoryType.SONSTIGE) {
        @Override
        public boolean matches(Transaction transaction) {
            // Käufe und Rückzahlungen
            return ((containsOneEmpfaenger(transaction, "Deutsche.Post") ||
                    containsOneVerwendungszweck(transaction, "Versandkosten Paket"
                    )) && isUmsatztyp(transaction, EUmsatztyp.AUSGANG));
        }
    },
    MOEBEL_EINRICHTUNG("Möbel/Einrichtung", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            // Käufe und Rückzahlungen
            return (containsOneEmpfaenger(transaction, "IKEA") && isUmsatztyp(transaction, EUmsatztyp.AUSGANG)
                    || containsSender(transaction, "IKEA") && isUmsatztyp(transaction, EUmsatztyp.EINGANG)
                    || (containsOneVerwendungszweck(transaction, "IKEA") && isUmsatztyp(transaction, EUmsatztyp.AUSGANG)));
        }
    },
    KLEIDUNG("Kleidung", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "MODEHAUS", "Adler.Modemarkte")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    LEBENSMITTEL("Lebensmittel", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneEmpfaenger(transaction, "rewe", "DM.Drogerie", "LIDL", "COMBI", "Rossmann", "Inkoop")
                    || containsOneVerwendungszweck(transaction, "DM Prteinpulver"))
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    PRIVATVERKAUF("Privatverkauf", ESuperCategoryType.EINKOMMEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsOneVerwendungszweck(transaction, "kleinanzeigen")
                    || containsSender(transaction, "paypal"))
                    && isUmsatztyp(transaction, EUmsatztyp.EINGANG);
        }
    },
    KLEINANZEIGEN("Kleinanzeigen", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "Kleinanzeigen")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    SONSTIGES("Sonstiges", ESuperCategoryType.SONSTIGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "paypal")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    GEHALT("Gehalt", ESuperCategoryType.EINKOMMEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsSender(transaction, "materna")
                    && isUmsatztyp(transaction, EUmsatztyp.EINGANG);
        }
    },
    SPARRATE("Sparrate", ESuperCategoryType.SONSTIGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "scalable")
                    && containsOneVerwendungszweck(transaction, "sparplan")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    ARZT_MEDIKAMENTE("Arzt/Medikamente", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsOneEmpfaenger(transaction, "apotheke");
        }
    };

    private String name;
    private ESuperCategoryType superCategoryType;

    public abstract boolean matches(Transaction transaction);
}
