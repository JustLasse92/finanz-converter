package de.finanz.converter.io;

import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.calculation.Calculator;
import de.finanz.converter.calculation.ECalculationType;
import de.finanz.converter.cash.AvailableCash;
import de.finanz.converter.cash.EAvailableCashTyp;
import de.finanz.converter.categorie.ECategoryType;
import de.finanz.converter.categorie.ESuperCategoryType;
import de.finanz.converter.exception.FinanzConverterException;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PDFExporter {

    public static final float[] COLUMN_DEFINITION_SIZE = new float[]{2.5F, 1F, 1.3F, 1F, 1F, 1F, 1F, 1F, 1F, 1.5F, 1.2F,
            1.5F, 1.5F};
    public static final Color HEADER_BACKGROUND_COLOR = new Color(77, 107, 221);
    private static final Font FONT_HEADER = FontFactory.getFont("Verdana", 9, 1, Color.WHITE);
    private static final Font FONT_ROW_SUPER_CATEGORY = FontFactory.getFont("Verdana", 8, 1, Color.WHITE);
    private static final Font FONT_ROW_CATEGORY = FontFactory.getFont("Verdana", 8, 0);
    private static final Font FONT_ROW_BETRAG = FontFactory.getFont("Verdana", 7, 0);
    private static final String FILE_NAME_FORMAT = "bilanz_%d.pdf";
    private static final Path OUTPUT_PATH = Path.of(System.getenv("OUTPUT_PATH"));
    private static final List<ECalculationType> CALCULATION_TYPE_EINNAHMEN = List.of(ECalculationType.EINNAMEN_GESAMT);
    private static final List<ECalculationType> CALCULATION_TYPE_AUSGABEN = List.of(ECalculationType.GIROKONTO_AUSGABEN_FIX,
            ECalculationType.AUSGABEN_VARIABEL,
            ECalculationType.BARGELD_AUSGABEN,
            ECalculationType.AUSGABEN_GESAMT);
    private static final List<ECalculationType> CALCULATION_TYPE_SPARRATE = List.of(ECalculationType.UEBERSCHUSS_MONAT, ECalculationType.SPARRATE_GESAMT);
    private static final List<ECalculationType> CALCULATION_TYPE_KONTOSTAENDE =
            List.of(ECalculationType.GIROKONTO_IST, ECalculationType.CASH);
    private static final List<EAvailableCashTyp> AVAILABLE_CASH_LIST = List.of(EAvailableCashTyp.TAGESGELDKONTO, EAvailableCashTyp.BARGELD);
    private static final List<ECalculationType> CALCULATION_TYPE_WERTPAPIERE =
            List.of(ECalculationType.VERRECHNUNGSKONTO, ECalculationType.VANGUARD_FTSE_ALL_WORLD,
                    ECalculationType.ISHARES_NASDAQ_100,
                    ECalculationType.BITCOIN);
    private static final List<EAvailableCashTyp> AVAILABLE_CASH_WERTPAPIERE = List.of(EAvailableCashTyp.AKTIEN_VL);
    private static final List<ECalculationType> CALCULATION_TYPE_BILANZ = List.of(ECalculationType.BILANZ_MONAT);
    private static final List<ECalculationType> DIFFERENZ = List.of(ECalculationType.CASH_DIFFERENZ
            , ECalculationType.GIROKONTO_DIFFERENZ);
    private final Bilanz bilanz;
    private final Calculator calculator;
    private final List<YearMonth> allGesetzteYearMonths;
    private int currentYear;
    private boolean evenRowNumber = false; //für wechselnde Hintergrundfarben

    public PDFExporter(Bilanz bilanz) {
        this.bilanz = bilanz;
        this.calculator = new Calculator(bilanz);
        this.allGesetzteYearMonths = bilanz.getYearMonthsSorted();
    }

    public void export() {
        allGesetzteYearMonths.stream()
                .map(YearMonth::getYear)
                .distinct()
                .filter(year -> year == 2026) // TODO WIEDER LÖSCHEN
                .forEach(year -> {
                    String fileName = FILE_NAME_FORMAT.formatted(year);
                    currentYear = year;

                    try (Document document = new Document()) {
                        // Sobald das Document geschlossen wird, wird auch der Writer geschlossen
                        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
                        float width = document.getPageSize().getWidth();

                        document.open();

                        PdfPTable table = createTable(width);
                        writeCategories(table);
                        document.add(table);
                        document.newPage();

                        table = createTable(width);
                        writeCalculations(table);
                        document.add(table);

                    } catch (DocumentException | IOException e) {
                        throw new FinanzConverterException(e);
                    }
                    try {
                        Files.copy(Path.of(fileName), Path.of(OUTPUT_PATH + File.separator + fileName), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private PdfPTable createTable(float width) {
        PdfPTable table = new PdfPTable(COLUMN_DEFINITION_SIZE);
        table.getDefaultCell().enableBorderSide(1);
        table.setHorizontalAlignment(20);
        table.setTotalWidth(width - 10);
        table.setLockedWidth(true);

        writeTableHeader(table);
        return table;
    }

    // "Kategorie","Januar","Februar","März" ...
    private void writeTableHeader(PdfPTable table) {
        PdfPCell pdfPCell = new PdfPCell(new Phrase("Kategorie", FONT_HEADER));
        pdfPCell.setBackgroundColor(HEADER_BACKGROUND_COLOR);
        table.addCell(pdfPCell);
        Arrays.stream(Month.values())
                .map(m -> m.getDisplayName(TextStyle.FULL, Locale.GERMAN))
                .forEach(s -> addCell(table, s, FONT_HEADER, HEADER_BACKGROUND_COLOR));
    }

    private void writeCategories(PdfPTable table) {
        for (ESuperCategoryType superCategoryType : ESuperCategoryType.values()) {
            // SuperCategory als Überschrift: "Einkommen", "Wohnen", "Versicherungen" ...
            writeGroupHeader(table, superCategoryType.getName());

            // Alle Kategorien der jeweiligen Superkategorie: Möbel/Einrichtung", "Kleidung", ...
            List<ECategoryType> categoryTypes = Arrays.stream(ECategoryType.values())
                    .filter(categoryType -> superCategoryType.equals(categoryType.getSuperCategoryType()))
                    .toList();
            for (ECategoryType categoryType : categoryTypes) {
                List<Double> values = new ArrayList<>();
                for (Month month : Month.values()) {
                    values.add(bilanz.getCategoryValue(categoryType, YearMonth.of(currentYear, month)));
                }

                if (values.stream().noneMatch(d -> d != 0)) {
                    // Wenn alle Werte 0 sind, soll die Zeile nicht geschrieben werden
                    continue;
                }

                evenRowNumber = !evenRowNumber;
                this.addCell(table, categoryType.getName(), FONT_ROW_CATEGORY);
                values.stream().map(this::formatBetrag)
//                        .map(betrag -> new Phrase(betrag, FONT_ROW_BETRAG))
                        .forEach(s -> this.addCell(table, s, FONT_ROW_BETRAG));
            }
        }
    }

    private void writeGroupHeader(PdfPTable table, String cellContent) {
        // Die erste Gruppe soll direkt unter den Header sein
        if (table.getRows().size() != 1) {
            addEmptyRow(table);
        }

        addCell(table, cellContent, FONT_ROW_SUPER_CATEGORY, HEADER_BACKGROUND_COLOR);

        // Zeilen nach der SuperCategory bleiben leer
        PdfPCell pdfPCell = new PdfPCell();
        pdfPCell.setColspan(COLUMN_DEFINITION_SIZE.length - 1);
        pdfPCell.setBackgroundColor(HEADER_BACKGROUND_COLOR);
        table.addCell(pdfPCell);

        evenRowNumber = true;
    }


    private void writeCalculations(PdfPTable table) {
        writeCalculationsOfTypes(table, "Einnahmen", CALCULATION_TYPE_EINNAHMEN);
        writeCalculationsOfTypes(table, "Ausgaben", CALCULATION_TYPE_AUSGABEN);
        writeCalculationsOfTypes(table, "Sparrate", CALCULATION_TYPE_SPARRATE);
        writeCalculationsOfTypes(table, "Kontostände", CALCULATION_TYPE_KONTOSTAENDE, AVAILABLE_CASH_LIST);
        writeCashDifferenz(table);
        writeCalculationsOfTypes(table, "Wertpapiere", CALCULATION_TYPE_WERTPAPIERE, AVAILABLE_CASH_WERTPAPIERE);
        writeCalculationsOfTypes(table, "Bilanz", CALCULATION_TYPE_BILANZ);
    }

    private void writeCalculationsOfTypes(PdfPTable writer, String header,
                                          List<ECalculationType> calculationTypes) {
        writeCalculationsOfTypes(writer, header, calculationTypes, List.of());
    }

    private void writeCalculationsOfTypes(PdfPTable table, String header,
                                          List<ECalculationType> calculationTypes,
                                          List<EAvailableCashTyp> availableCashTyps) {
        writeGroupHeader(table, header);

        for (EAvailableCashTyp availableCashTyp : availableCashTyps) {
            evenRowNumber = !evenRowNumber;
            addCell(table, availableCashTyp.getBezeichnung(), FONT_ROW_CATEGORY);
            for (Month month : Month.values()) {
                Optional<AvailableCash> availableCashOptional =
                        bilanz.getAvailableCashesInYearMonths(availableCashTyp, YearMonth.of(currentYear, month));
                double betrag = availableCashOptional.isPresent() ? availableCashOptional.get().getBetrag() : 0.0;
                addCell(table, this.formatBetrag(betrag), FONT_ROW_BETRAG);
            }
        }

        for (ECalculationType type : calculationTypes) {
            evenRowNumber = !evenRowNumber;
            addCell(table, type.getName(), FONT_ROW_CATEGORY);
            for (Month month : Month.values()) {
                addCell(table, formatBetrag(calculator.getCalculationValue(type, YearMonth.of(currentYear, month))), FONT_ROW_BETRAG);
            }

        }
    }

    private void writeCashDifferenz(PdfPTable table) {
//        if (differenceExists()) {
        // Differenz soll nur ausgegeben werden, wenn auch eine Vorhanden ist
        writeCalculationsOfTypes(table, "Differenz in den Berechnungen", DIFFERENZ);
//        }
    }

//    private boolean differenceExists() {
//        return (int) currentGesetzteYearMonths.stream()
//                .mapToDouble(yearMonth -> DIFFERENZ.stream()
//                        .mapToDouble(type -> calculator.getCalculationValue(type, yearMonth))
//                        .sum())
//                .sum() != 0;
//    }

    private void addEmptyRow(PdfPTable table) {
        PdfPCell pdfPCell = new PdfPCell();
        pdfPCell.setColspan(COLUMN_DEFINITION_SIZE.length);
        pdfPCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(pdfPCell);
    }

    private void addCell(PdfPTable table, String inhalt, Font font, Color backgroundColor) {
        Phrase phrase = new Phrase(inhalt, font);
        PdfPCell pdfPCell = new PdfPCell(phrase);
        pdfPCell.setBackgroundColor(backgroundColor);
        pdfPCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(pdfPCell);
    }

    private void addCell(PdfPTable table, String inhalt, Font font) {
        Color backgroundColor = evenRowNumber ? new Color(194, 211, 255) : new Color(137, 163, 244);
        addCell(table, inhalt, font, backgroundColor);
    }

    private String formatBetrag(Double betrag) {
        return String.format("%.0f €", betrag);
    }
}
