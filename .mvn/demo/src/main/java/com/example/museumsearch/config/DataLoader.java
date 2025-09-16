package com.example.museumsearch.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.example.museumsearch.model.Museum;
import com.example.museumsearch.model.MuseumStatus;
import com.example.museumsearch.repository.MuseumRepository;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader {
    
    private final MuseumRepository museumRepository;

    private final String normalizeNewLine(String value) {
        if (value == null) return null;
        
        String replaced = value.replace("\\n", "\n");
        return replaced;
    }

    private String getField(String[] fields, int index) {
        return index < fields.length ? fields[index] : "";
    }

    private double parseDoubleSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(value.trim());
    }

    private java.time.LocalDate parseLocalDateSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            String cleaned = value.replaceAll("\\(.*?\\)", "").trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu年M月d日");
            return LocalDate.parse(cleaned, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    @PostConstruct
    public void loadMuseumData() {

        if (museumRepository.count() > 0) {
            return;
        }

        InputStream inputStream = getClass().getResourceAsStream("/data/museums.csv");

        if (inputStream == null) {
            throw new IllegalArgumentException("CSVファイルが見つかりません: /data/museums.csv");
        }
        
        try (CSVReader csvReader = new CSVReaderBuilder(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .withCSVParser(new CSVParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .withEscapeChar('\\')
                .withStrictQuotes(false)
                .withIgnoreQuotations(false)
                .build())
            .build()) {

                String[] fields;
                boolean isFirstLine = true;
                
                while ((fields = csvReader.readNext()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    Museum museum = Museum.builder()
                        .name(getField(fields, 0))
                        .address(getField(fields, 1))
                        .prefecture(getField(fields, 2))
                        .phoneNumber(getField(fields, 3))
                        .exhibition(getField(fields, 4))
                        .imageProvider(getField(fields, 5))
                        .exhibitionImage(getField(fields, 6))
                        .museumUrl(getField(fields, 7))
                        .exhibitionUrl(getField(fields, 8))
                        .startDate(parseLocalDateSafely(getField(fields, 9)))
                        .endDate(parseLocalDateSafely(getField(fields, 10)))
                        .description(normalizeNewLine(getField(fields, 11)))
                        .openingHours(normalizeNewLine(getField(fields,12)))
                        .closingDays(normalizeNewLine(getField(fields, 13)))
                        .admissionFee(normalizeNewLine(getField(fields, 14)))
                        .access(normalizeNewLine(getField(fields, 15)))
                        .latitude(parseDoubleSafely(getField(fields, 16)))
                        .longitude(parseDoubleSafely(getField(fields, 17)))
                        .category(getField(fields, 18))
                        .build();

                    museum.updateStatus(MuseumStatus.APPROVED);
                    museumRepository.save(museum);
                }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("CSV読み込み失敗" + e.getMessage(), e);
        }
    }

    @PostConstruct
    public void addMuseumsFromCsv() {
        InputStream inputStream = getClass().getResourceAsStream("/data/museums.csv");

        if (inputStream == null) {
            throw new IllegalArgumentException("CSVファイルが見つかりません: /data/museums.csv");
        }

        try (CSVReader csvReader = new CSVReaderBuilder(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .withCSVParser(new CSVParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .withEscapeChar('\\')
                .build())
            .build()) {

            String[] fields;
            boolean isFirstLine = true;

            while ((fields = csvReader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }

                if (museumRepository.existsByName(fields[0])) continue;

                Museum museum = Museum.builder()
                    .name(fields[0])
                    .address(fields[1])
                    .prefecture(fields[2])
                    .phoneNumber(fields[3])
                    .exhibition(fields[4])
                    .imageProvider(fields[5])
                    .exhibitionImage(fields[6])
                    .museumUrl(fields[7])
                    .exhibitionUrl(fields[8])
                    .startDate(parseLocalDateSafely(fields[9]))
                    .endDate(parseLocalDateSafely(fields[10]))
                    .description(normalizeNewLine(fields[11]))
                    .openingHours(normalizeNewLine(fields[12]))
                    .closingDays(normalizeNewLine(fields[13]))
                    .admissionFee(normalizeNewLine(fields[14]))
                    .access(normalizeNewLine(fields[15]))
                    .latitude(parseDoubleSafely(fields[16]))
                    .longitude(parseDoubleSafely(fields[17]))
                    .category(fields[18])
                    .build();

                museum.updateStatus(MuseumStatus.APPROVED);
                museumRepository.save(museum);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("CSV読み込み失敗: " + e.getMessage(), e);
        }
    }
}
