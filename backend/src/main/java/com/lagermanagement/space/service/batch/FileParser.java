package com.lagermanagement.space.service.batch;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Liest CSV- und Excel-Dateien ein und gibt eine normalisierte Liste von Zeilen-Maps zurück.
 * Schlüssel sind lowercase-normalisierte Spaltenköpfe.
 * Pflicht-Spalten werden anhand des Dateinamens validiert.
 */
@Component
@Slf4j
public class FileParser {

    private static final Map<String, Set<String>> PFLICHT_SPALTEN = Map.of(
            "artikel_", Set.of("artikelnummer", "bezeichnung", "mengeneinheit", "warengruppe", "lieferant_id"),
            "lieferanten_", Set.of("lieferant_id", "name", "lead_time_tage"),
            "eingang_", Set.of("datum", "artikelnummer", "menge"),
            "ausgang_", Set.of("datum", "artikelnummer", "menge", "buchungstyp")
    );

    /**
     * Erkennt das Format anhand der Dateiendung (.csv oder .xlsx) und gibt eine
     * normalisierte Liste von Zeilen-Maps zurück. Leere Zeilen werden übersprungen.
     *
     * @throws ParseException wenn Pflicht-Spalten fehlen oder die Datei nicht gelesen werden kann
     */
    public List<Map<String, String>> parse(Path file) throws ParseException {
        String filename = file.getFileName().toString().toLowerCase();
        if (filename.endsWith(".xlsx")) {
            return parseExcel(file);
        } else if (filename.endsWith(".csv")) {
            return parseCsv(file);
        }
        throw new ParseException("Unbekanntes Dateiformat: " + file.getFileName());
    }

    private List<Map<String, String>> parseCsv(Path file) throws ParseException {
        List<Map<String, String>> result = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            String[] headerRow = csvReader.readNext();
            if (headerRow == null) {
                return result;
            }
            String[] headers = normalizeHeaders(headerRow);
            validatePflichtSpalten(headers, file.getFileName().toString());

            List<String[]> rows = csvReader.readAll();
            for (String[] row : rows) {
                Map<String, String> rowMap = rowToMap(headers, row);
                if (!isBlankRow(rowMap)) {
                    result.add(rowMap);
                }
            }
        } catch (IOException | CsvException e) {
            log.error("CSV-Datei konnte nicht gelesen werden: {} – {}", file.getFileName(), e.getMessage());
            throw new ParseException("CSV-Lesefehler: " + file.getFileName(), e);
        }
        return result;
    }

    private List<Map<String, String>> parseExcel(Path file) throws ParseException {
        List<Map<String, String>> result = new ArrayList<>();
        try (var inputStream = Files.newInputStream(file);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return result;
            }

            String[] headers = readExcelHeaders(headerRow);
            validatePflichtSpalten(headers, file.getFileName().toString());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> rowMap = excelRowToMap(headers, row);
                if (!isBlankRow(rowMap)) {
                    result.add(rowMap);
                }
            }
        } catch (IOException e) {
            log.error("Excel-Datei konnte nicht gelesen werden: {} – {}", file.getFileName(), e.getMessage());
            throw new ParseException("Excel-Lesefehler: " + file.getFileName(), e);
        }
        return result;
    }

    private String[] readExcelHeaders(Row headerRow) {
        int numCells = headerRow.getLastCellNum();
        String[] headers = new String[numCells];
        for (int i = 0; i < numCells; i++) {
            Cell cell = headerRow.getCell(i);
            headers[i] = cell != null ? cell.getStringCellValue().toLowerCase().trim() : "";
        }
        return headers;
    }

    private Map<String, String> excelRowToMap(String[] headers, Row row) {
        Map<String, String> rowMap = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.getCell(i);
            rowMap.put(headers[i], cellToString(cell));
        }
        return rowMap;
    }

    private String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                BigDecimal bd = BigDecimal.valueOf(cell.getNumericCellValue());
                try {
                    yield String.valueOf(bd.longValueExact());
                } catch (ArithmeticException e) {
                    yield bd.toPlainString();
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> new DataFormatter().formatCellValue(cell);
            default -> "";
        };
    }

    private void validatePflichtSpalten(String[] headers, String filename) throws ParseException {
        String filenameLower = filename.toLowerCase();
        Set<String> pflichtSpalten = PFLICHT_SPALTEN.entrySet().stream()
                .filter(e -> filenameLower.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(Set.of());

        if (pflichtSpalten.isEmpty()) {
            return;
        }

        Set<String> vorhandene = new HashSet<>(Arrays.asList(headers));
        Set<String> fehlend = new HashSet<>(pflichtSpalten);
        fehlend.removeAll(vorhandene);

        if (!fehlend.isEmpty()) {
            throw new ParseException(
                    "Pflicht-Spalten fehlen in %s: %s".formatted(filename, fehlend)
            );
        }
    }

    private String[] normalizeHeaders(String[] headers) {
        return Arrays.stream(headers)
                .map(h -> h != null ? h.toLowerCase().trim() : "")
                .toArray(String[]::new);
    }

    private Map<String, String> rowToMap(String[] headers, String[] values) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String value = (i < values.length && values[i] != null) ? values[i].trim() : "";
            map.put(headers[i], value);
        }
        return map;
    }

    private boolean isBlankRow(Map<String, String> row) {
        return row.values().stream().allMatch(String::isBlank);
    }
}
