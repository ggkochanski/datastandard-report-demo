package com.stibo.demo.report.service;

import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportFormatter {
    private static final String INDENT = "  ";

    private String indent = "";

    private StringBuilder sb = new StringBuilder(512);

    public ReportFormatter printWithMandatory(String name, boolean mandatory) {
        sb.append(indent);
        if ( null != name ) {
            sb.append(name);
        }
        if ( mandatory ) {
            sb.append('*');
        }
        return this;
    }

    public ReportFormatter print(String name) {
        return printWithMandatory(name, false);
    }

    public ReportFormatter printList(Stream<String> list) {
        list.forEach(s -> sb.append(s).append('\n'));
        return this;
    }

    public String end() {
        if ( sb.length() > 0 && '\n' == sb.charAt(sb.length() - 1) ) {
            sb.setLength(sb.length() - 1);
        }
        String res = sb.toString();
        sb.setLength(0);
        return res;
    }

    public ReportFormatter startSubsection() {
        sb.append("{\n");
        indent += INDENT;
        return this;
    }

    public ReportFormatter endSubsection() {
        indent = indent.substring(0, indent.length() - INDENT.length());
        sb.append(indent).append("}");
        return this;
    }

    public ReportFormatter multivalue(boolean multivalue) {
        if ( multivalue ) {
            sb.append("[]");
        }
        sb.append('\n');
        return this;
    }

    public ReportFormatter printType(String type) {
        sb.append(": ").append(type);
        return this;
    }
}
