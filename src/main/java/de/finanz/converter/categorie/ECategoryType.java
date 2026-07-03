package de.finanz.converter.categorie;

import de.finanz.converter.transaction.EUmsatztyp;
import de.finanz.converter.transaction.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static de.finanz.converter.TransactionCategoryMatcher.containsAnyEmpfaenger;
import static de.finanz.converter.TransactionCategoryMatcher.containsAnySender;
import static de.finanz.converter.TransactionCategoryMatcher.containsAnyVerwendungszweck;
import static de.finanz.converter.TransactionCategoryMatcher.isUmsatztyp;

@AllArgsConstructor
@Getter
public enum ECategoryType {
    NICHT_KATEGORISIERT("Nicht kategorisiert", ESuperCategoryType.SONSTIGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return false;
        }
    },
    TAGESGELDKONTO_TRANSFER("Tagesgeldkonto Transfer", ESuperCategoryType.TRANSFER) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsAnyEmpfaenger(transaction, "Lasse Ganske") && containsAnySender(transaction, "Lasse Ganske")
                    && transaction.getVerwendungszweck().isEmpty();
        }
    },
    AUSLAGEN_AUSGANG("Auslagen Ausgaben", ESuperCategoryType.AUSLAGEN) {
        @Override
        public boolean matches(Transaction transaction) {
            // Auslagen werden über expenses_transactions definiert
            return false;
        }
    },
    AUSLAGEN_EINGANG("Auslagen Eingang", ESuperCategoryType.AUSLAGEN) {
        @Override
        public boolean matches(Transaction transaction) {
            // Auslagen werden über expenses_transactions definiert
            return false;
        }
    },
    FAHRRAD("Fahrrad", ESuperCategoryType.MOBILITAET) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyEmpfaenger(transaction, "Brand.hei.e.Bikes/Bassum");
            }
            return false;
        }
    },
    AUTO_SPRIT("Auto und Sprit", ESuperCategoryType.MOBILITAET) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyEmpfaenger(transaction, "STAR..Bassum/Bassum");
            }
            return false;
        }
    },
    BAHN("Bahntickets", ESuperCategoryType.MOBILITAET) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyEmpfaenger(transaction, "Bremen.Hbf..DB/Bremen", "TDV.NWB..NDS/Leipzig")
                        || containsAnyVerwendungszweck(transaction, "DB Vertrieb GmbH");
            }
            return false;
        }
    },
    REISE("Reisen", ESuperCategoryType.ENTERTAINMENT) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyEmpfaenger(transaction, "INTERCITYHOTEL", "ALICANTE") || containsAnyVerwendungszweck(transaction, "Ryanair", "Booking.com");
            }
            return false;
        }
    },
    SPIELE("Spiele", ESuperCategoryType.ENTERTAINMENT) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyVerwendungszweck(transaction, "Blizzard Entertainment", "www.stea mpowered.com");
            }
            return false;
        }
    },
    SONSTIGES("Sonstiges", ESuperCategoryType.ENTERTAINMENT) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsAnyVerwendungszweck(transaction, "1049524567778/PP.4622.PP", "Ihr Einkauf bei GitHub, Inc", "197034, JustLasse")
                    || containsAnyEmpfaenger(transaction, "NANU.NANA", "GALERIA.BREMEN", "WK.NEO");
        }
    },
    EINTRITT_KULTUR("Eintritt Kultur", ESuperCategoryType.ENTERTAINMENT) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyEmpfaenger(transaction, "AUSSICHTSPLATTFORM", "SCHAUBURG.KINO")
                        || containsAnyVerwendungszweck(transaction, "kinoheld GmbH", "Gilde-Festhalle");
            }
            return false;
        }
    },
    GESCHENKE("Spiele", ESuperCategoryType.ENTERTAINMENT) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyEmpfaenger(transaction, "Stina") && containsAnyVerwendungszweck(transaction,
                        "Fahrrad");
            }
            return false;
        }
    },
    LITERATUR("Literatur", ESuperCategoryType.ENTERTAINMENT) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyEmpfaenger(transaction, "PundB.211/Bremerhaven", "Stadtbibliothek");
            }
            return false;
        }
    },
    AUSWAERTS_ESSEN_TRINKEN("Auswärts Essen/Trinken", ESuperCategoryType.ENTERTAINMENT) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyEmpfaenger(transaction, "SumUp...BENS.coffee.in/Bassum", "Back.Factory", "SumUp.." +
                                ".Kulturbahnhof/Bremerhaven", "Backstube.und.C/Bremerhaven", "Glenn.s.Place/Bremerhaven",
                        "LECROBAG.BREMEN.HBF.TU/BREMEN", "SANIFAIR", "STADTBAECKEREI", "BACKSTUBE", "Materna" +
                                ".Information/Dortmund", "Asiagourmet/Dortmund", "Good.Quatsch", "Backerei", "SMOKE" +
                                ".KIOSK/BREMERHAVEN", "Haferkamp", "Restaurant", "HBF.Saarbruecken", "BackWerk",
                        "Cafe.Extrablatt", "GASTSTAETTE.SCHMIEDING", "KAGI.BAR", "RONA.COCKTAILBAR", "Pizzeria",
                        "Alexandra.Ausb/Bremerhaven", "KiK.Fil..8322/Bremerhaven", "LE.CROBAG.SHOP", "BACKHAUS",
                        "Imbiss.CafeBistr", "Ditsch", "Mueller...Egerer", "YORMAS.AG", "LTLengermannTries", "MERCAPLAYA.SAN.JUAN")
                        || containsAnyVerwendungszweck(transaction, "Pizzamann", "Ihr Einkauf bei Kevin Ricke", "Sushi");
            }
            return false;
        }
    },
    BARGELDABHEBUNGEN("Bargeldabhebungen", ESuperCategoryType.TRANSFER) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyEmpfaenger(transaction, "Kreissparkasse.Diepholz/BASSUM", "SPARKASSE.BREMEN",
                    "SPARKASSE.DORTMUND/SPK.DO.91", "Sparda.Bank")
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG));
        }
    },
    MIETE("Miete", ESuperCategoryType.WOHNEN) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                return containsAnyVerwendungszweck(transaction, "Miete", "Beteiligung Nebenkosten", "Anteil " +
                        "Nebenkosten", "Nachzahlung Nebenkosten");
            }
            return false;
        }
    },
    STROM_HEIZKOSTEN("Strom und Heizkosten", ESuperCategoryType.WOHNEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnySender(transaction, "NaturStromHandel") || containsAnyEmpfaenger(transaction,
                    "NaturStromHandel"));
        }
    },
    INTERNET("Internet", ESuperCategoryType.WOHNEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsAnyEmpfaenger(transaction, "Vodafone Deutschland GmbH");
        }
    },
    ZINSEN("Zinsen", ESuperCategoryType.EINKOMMEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyVerwendungszweck(transaction, "Steuerausgleich") && isUmsatztyp(transaction,
                    EUmsatztyp.EINGANG));
        }
    },
    SPENDEN("Spenden", ESuperCategoryType.SONSTIGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyEmpfaenger(transaction, "DRK-SPENDENKONTO") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    GESUNDHEIT_ZUSATZVERSICHERUNG("Gesundheit Zusatzversicherung", ESuperCategoryType.VERSICHERUNGEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyEmpfaenger(transaction, "Barmenia Krankenvers") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    LAGERBOX("Lagerbox", ESuperCategoryType.SONSTIGE_VERTRAEGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyVerwendungszweck(transaction, "Safe-Box") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    HANDY("Handy", ESuperCategoryType.SONSTIGE_VERTRAEGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyVerwendungszweck(transaction, "Aldi Talk") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    RUNDFUNK("Rundfunkgebühren", ESuperCategoryType.WOHNEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyVerwendungszweck(transaction, "Rundfunk"));
        }
    },
    HAFTPFLICHTVERSICHERUNG("Haftpflichtversicherung", ESuperCategoryType.VERSICHERUNGEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyVerwendungszweck(transaction, "Haftpflicht") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    IGM("IGM", ESuperCategoryType.VERSICHERUNGEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyEmpfaenger(transaction, "Industriegewerkschaft Metall") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    SPORT("Sport", ESuperCategoryType.SONSTIGE_VERTRAEGE) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyEmpfaenger(transaction, "cf Fitness", "EF Fitness GmbH") && isUmsatztyp(transaction,
                    EUmsatztyp.AUSGANG));
        }
    },
    MOEBEL_EINRICHTUNG("Möbel/Einrichtung", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.EINGANG)) {
                // Rückzahlung
                return containsAnySender(transaction, "IKEA");
            } else {
                return containsAnyVerwendungszweck(transaction, "IKEA")
                        || containsAnyVerwendungszweck(transaction, "Materialkosten")
                        || containsAnyEmpfaenger(transaction, "IKEA", "BBM.Baumarkt/Bassum", "Woolworth");
            }
        }
    },
    KLEIDUNG("Kleidung", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsAnyEmpfaenger(transaction, "MODEHAUS", "Adler.Modemarkte", "DEICHMANN", "C + A Mode " +
                    "GmbH", "TJX.Europe.Ltd..Co.KG./Bremen", "Tinas Restposte", "Tinas.Restposte")
                    || containsAnyVerwendungszweck(transaction, "Tchibo Klamotten", "Hausschuhe", "CA Online Shop");
        }
    },
    LEBENSMITTEL("Lebensmittel", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyEmpfaenger(transaction, "rewe", "DM.Drogerie", "LIDL", "COMBI", "Rossmann", "Inkoop",
                    "Penny", "Netto", "SUPERBIOMARKT", "TCHIBO", "E.CENTER.KNAUER", "ALDI", "Winterberg.Einzelhandel")
                    || containsAnyVerwendungszweck(transaction, "DM Prteinpulver"))
                    && isUmsatztyp(transaction, EUmsatztyp.AUSGANG);
        }
    },
    PRIVATVERKAUF("Privatverkauf", ESuperCategoryType.EINKOMMEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return (containsAnyVerwendungszweck(transaction, "kleinanzeigen", "Wohnungsübernahme", "Hidden Games.Monika Krebs")
                    || containsAnySender(transaction, "paypal", "Valerii Malakhov"))
                    && isUmsatztyp(transaction, EUmsatztyp.EINGANG);
        }
    },
    ONLINE_KAEUFE("Online Käufe", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsAnyEmpfaenger(transaction, "Kleinanzeigen", "AMAZON", "Wigento GmbH", "FOTOSERVICE", "rebuy recommerce")
                    || containsAnyVerwendungszweck(transaction, "Kleinanzeigen Schiffe versenken", "1046755274969" +
                            "/PP.4622.PP/. , Ihr Einkauf bei", "1046753926122/PP.4622.PP", "1048948119613/PP.4622.PP",
                    "1048122384878/PP.4622.PP", "1048095263481/PP.4622.PP/", "1047868447652/PP.4622.PP/", "1047548680816/PP.4622.PP/", "Amazon Handyhülle", "181554, JustLasse, Bremen")
                    || containsAnySender(transaction, "rebuy recommerce Gmb");
        }
    },
    SONSTIGE_LEBENSHALTUNG("Sonstiges", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsAnyVerwendungszweck(transaction, "Kaputtes Einmachglas", "Versandkosten",
                    "Ihr Einkauf bei Deutsche Post AG", "Porto")
                    || containsAnyEmpfaenger(transaction, "SPIEGLEIN.SPIEG", "Deutsche.Post");
        }
    },
    GEHALT("Gehalt", ESuperCategoryType.EINKOMMEN) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsAnySender(transaction, "materna")
                    && isUmsatztyp(transaction, EUmsatztyp.EINGANG);
        }
    },
    VERRECHNUNGSKONTO_SPARPLAN("Verrechnungskonto Sparplan", ESuperCategoryType.TRANSFER) {
        @Override
        public boolean matches(Transaction transaction) {
            if (isUmsatztyp(transaction, EUmsatztyp.AUSGANG)) {
                if (containsAnyEmpfaenger(transaction, "scalable")
                        && containsAnyVerwendungszweck(transaction, "sparplan", "Einzahlung Scalable Capital")) {
                    return true;
                }
                if (containsAnyEmpfaenger(transaction, "Derdengelden Pay.nl")) {
                    return true;
                }
            }
            return false;
        }
    },
    ARZT_MEDIKAMENTE("Arzt/Medikamente", ESuperCategoryType.LEBENSHALTUNG) {
        @Override
        public boolean matches(Transaction transaction) {
            return containsAnyEmpfaenger(transaction, "apotheke");
        }
    };

    private String name;
    private ESuperCategoryType superCategoryType;

    public abstract boolean matches(Transaction transaction);
}
