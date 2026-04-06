package org.example;

import lombok.Getter;
import org.example.kategorie.Categorie;
import org.example.kategorie.ECategoryType;

import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Bilanz {
    private Map<ECategoryType, Categorie> categories = new HashMap<>();

    public void addCategorieValues(Categorie k) {
        Categorie currentCategorie = getCategory(k.getType());
        k.getValues().forEach((currentCategorie::addValue));
        categories.put(currentCategorie.getType(), currentCategorie);
    }

    public Categorie getCategory(ECategoryType eCategoryType) {
        return categories.getOrDefault(eCategoryType, new Categorie(eCategoryType));
    }

    public List<Month> getMonthsSorted() {
        return getCategories().values()
                .stream()
                .map(k -> k.getValues().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}