package com.stibo.demo.report.service;

import com.stibo.demo.report.logging.LogTime;
import com.stibo.demo.report.model.*;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReportService {

    @LogTime
    public Stream<Stream<String>> report(Datastandard datastandard, String categoryId) {
        return new ReportBuilder(datastandard, new ReportFormatter()).build(categoryId);
    }

}
