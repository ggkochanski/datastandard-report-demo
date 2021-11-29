package com.stibo.demo.report.service;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParser;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParserSettings;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stibo.demo.report.model.Datastandard;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ReportService.class, ObjectMapper.class })
public class ReportServiceTest {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ReportService reportService;

    private CsvParser csvParser;


    @Before
    public void before() throws IOException {
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CsvParserSettings csvParserSettings = new CsvParserSettings();
        csvParserSettings.setNullValue("");
        this.csvParser=new CsvParser(csvParserSettings);
    }

    @Test
    public void testReport() {
        Map.of(
                "datastandard-demo","leaf",
                "datastandard-acme","T_SHIRTS"
        ).forEach((datastandard,categoryId) -> {
            List<List<String>> report = reportService.report(loadDatastandard(datastandard), categoryId).map(row -> row.collect(toList())).collect(toList());
            Assert.assertEquals(loadReport(datastandard,categoryId).toString(), report.toString());
        });
    }

    private List<List<String>> loadReport(String datastandard, String categoryId) {
        InputStream streamCsv = getClass().getClassLoader().getResourceAsStream(datastandard+"-"+categoryId+".csv");
        return csvParser.parseAll(streamCsv).stream()
                .map(row -> Arrays.asList(row)).collect(Collectors.toList());
    }

    private Datastandard loadDatastandard(String datastandard) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(datastandard+".json");
        try {
            return objectMapper.readValue(stream, Datastandard.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
