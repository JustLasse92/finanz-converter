package de.finanz.converter.io;

import de.finanz.converter.bilanz.Bilanz;
import de.finanz.converter.calculation.Calculator;
import de.finanz.converter.categorie.ECategoryType;
import de.finanz.converter.categorie.ESuperCategoryType;
import de.finanz.converter.exception.FinanzConverterException;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

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

public class PDFExporter {

    public static final float[] COLUMN_DEFINITION_SIZE = new float[]{2.5F, 1F, 1F, 1F, 1F, 1F, 1F, 1F, 1F, 1.3F, 1F,
            1.3F, 1.3F};
    private static final Font FONT_HEADER = FontFactory.getFont("Verdana", 8, 1);
    private static final Font FONT_ROW_SUPER_CATEGORY = FontFactory.getFont("Verdana", 8, 1);
    private static final Font FONT_ROW_CATEGORY = FontFactory.getFont("Verdana", 8, 0);
    private static final Font FONT_ROW_BETRAG = FontFactory.getFont("Verdana", 7, 0);
    private static final String FILE_NAME_FORMAT = "bilanz_%d.pdf";
    private static final Path OUTPUT_PATH = Path.of(System.getenv("OUTPUT_PATH"));

    private final Bilanz bilanz;
    private final Calculator calculator;
    private final List<YearMonth> allGesetzteYearMonths;
    private int currentYear;

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
//                    currentYear = allGesetzteYearMonths.stream()
//                            .filter(yearMonth -> yearMonth.getYear() == year)
//                            .map(f -> f.getYear())
//                            .toList();

                    // step 1: creation of a document-object
                    try (Document document = new Document()) {
                        // step 2
                        // we create a writer that listens to the document
                        // and directs a PDF-stream to a file
                        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
                        float width = document.getPageSize().getWidth();
                        float height = document.getPageSize().getHeight();
                        // step 3
                        document.open();

                        // step 4
//                        DoubleStream.generate(() -> 1F)
//                                .limit(12)
//                                .collect(Collectors.toList());

                        //                        float[] columnDefinitionSize = {1.33F, 1.33F, 1.33F, 1.33F};

                        float pos = height / 2;
                        final PdfPTable table = new PdfPTable(COLUMN_DEFINITION_SIZE);
//                        final PdfPTable table = new PdfPTable(13);
                        PdfPCell cell;


                        table.getDefaultCell().enableBorderSide(1);
                        table.setHorizontalAlignment(5);
                        table.setTotalWidth(width - 20);
//                        table.setTotalWidth(width);
                        table.setLockedWidth(true);

//                        cell = new PdfPCell(new Phrase("Table added with document.add()"));
//                        cell.setColspan(columnDefinitionSize.length);
//                        table.addCell(cell);

                        writeHeader(table);
                        writeCategories(table);

                        document.add(table);

                        Files.copy(Path.of(fileName), Path.of(OUTPUT_PATH + File.separator + fileName), StandardCopyOption.REPLACE_EXISTING);
                    } catch (DocumentException | IOException e) {
                        throw new FinanzConverterException(e);
                    }
                });
    }

    // "Kategorie","Januar","Februar","März" ...
    private void writeHeader(PdfPTable table) {
        table.addCell(new Phrase("Kategorie", FONT_HEADER));
        Arrays.stream(Month.values())
                .map(m -> m.getDisplayName(TextStyle.FULL, Locale.GERMAN))
                .map(s -> new Phrase(s, FONT_HEADER))
                .forEach(c -> {
                    System.out.println(table.isSplitRows());
                    table.addCell(c);
                });
    }

    private void writeCategories(PdfPTable table) {
        for (ESuperCategoryType superCategoryType : ESuperCategoryType.values()) {
            // SuperCategory als Überschrift: "Einkommen", "Wohnen", "Versicherungen" ...
            Phrase phrase = new Phrase(superCategoryType.getName(), FONT_ROW_SUPER_CATEGORY);
            PdfPCell pdfPCell = new PdfPCell(phrase);
            table.addCell(pdfPCell);

            // Zeilen nach der SuperCategory bleiben leer
            pdfPCell = new PdfPCell();
            pdfPCell.setColspan(COLUMN_DEFINITION_SIZE.length - 1);
            table.addCell(pdfPCell);

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

                table.addCell(new Phrase(categoryType.getName(), FONT_ROW_CATEGORY));
                values.stream().map(this::formatBetrag)
                        .map(betrag -> new Phrase(betrag, FONT_ROW_BETRAG))
                        .forEach(table::addCell);
            }
        }
    }

    private String formatBetrag(Double betrag) {
        return String.format("%.2f", betrag);
    }
}
