package de.finanz.converter;

import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.kategorie.Categorie;
import de.finanz.converter.kategorie.ECategoryType;
import de.finanz.converter.kategorie.ESuperCategoryType;

import java.util.Collection;
import java.util.List;

public class Calculation {
//        - Gesamte Einnahmen
//    - Fixe Ausgaben
//    - Variable Ausgaben
//    - Gesamte Ausgaben
//    - Monatlicher Überschuss
//    - Komplette Sparrate
//    - Bilanz
//    - Cash
//    - Aktien
//    - Bitcoin
//    - Differenz zwischen berechneter Bilanz und tatsächlicher Bilanz

    double einnahmenGesamt;
    double ausgabenFix;
    double ausgabenVariable;
    double ausgabenGesamt;
    double sparrateGesamt; // (Monatliche Sparrate + Monatlicher Überschuss)
    double monaterlicherUeberschuss;
    double cash;
    double[] aktien;
    double bitcoin;
    double differenzBerechnungen;
    double gesamtvermoegen;


    public Calculation(Bilanz bilanz) {
        Collection<Categorie> categories = bilanz.getAllCategories();
        einnahmenGesamt = calculateEinnahmenGesamt(categories);
        ausgabenFix = calculateAusgabenFix(categories);
        ausgabenVariable = calculateAusgabenVariable(categories);
        ausgabenGesamt = ausgabenFix + ausgabenVariable;
        monaterlicherUeberschuss = einnahmenGesamt - ausgabenGesamt;
        sparrateGesamt = calculateMonatlicheSparrates(categories) + monaterlicherUeberschuss;
    }

    private double calculateMonatlicheSparrates(Collection<Categorie> categories) {
        return sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARRATE));
    }

    private double calculateAusgabenVariable(Collection<Categorie> categories) {
        double sumValues = sumValuesOfSuperCategoryTypes(categories, List.of(ESuperCategoryType.LEBENSHALTUNG, ESuperCategoryType.AUTO_TANKEN,
                ESuperCategoryType.ENTERTAINMENT, ESuperCategoryType.SONSTIGE));
        double sparrate = sumValuesOfCategoryTypes(categories, List.of(ECategoryType.SPARRATE));
        return sumValues - sparrate;
    }

    private double calculateAusgabenFix(Collection<Categorie> categories) {
        return sumValuesOfSuperCategoryTypes(categories, List.of(ESuperCategoryType.WOHNEN, ESuperCategoryType.SONSTIGE_VERTRAEGE, ESuperCategoryType.VERSICHERUNGEN));
    }

    private double calculateEinnahmenGesamt(Collection<Categorie> categories) {
        return sumValuesOfSuperCategoryTypes(categories, List.of(ESuperCategoryType.EINKOMMEN));
    }


    private double sumValuesOfSuperCategoryTypes(Collection<Categorie> categories,
                                                 Collection<ESuperCategoryType> superCategoryTypes) {
        return sumValuesOfAllTypes(categories, superCategoryTypes, List.of());
    }

    private double sumValuesOfCategoryTypes(Collection<Categorie> categories, Collection<ECategoryType> categoryTypes) {
        return sumValuesOfAllTypes(categories, List.of(), categoryTypes);
    }

    private double sumValuesOfAllTypes(Collection<Categorie> categories, Collection<ESuperCategoryType> superCategoryTypes, Collection<ECategoryType> categoryTypes) {
        return categories.stream()
                .filter(categorie -> superCategoryTypes.contains(categorie.getType().getSuperCategoryType()) || categoryTypes.contains(categorie.getType()))
                .map(categorie -> categorie.getValues().values())
                .flatMap(Collection::stream)
                .mapToDouble(e -> e)
                .sum();
    }

}
