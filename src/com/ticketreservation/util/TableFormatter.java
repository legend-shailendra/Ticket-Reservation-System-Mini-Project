package com.ticketreservation.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to format console tables cleanly with headers and borders.
 */
public class TableFormatter {

    public static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        List<String> headers = new ArrayList<>();
        List<Integer> colWidths = new ArrayList<>();

        for (int i = 1; i <= columnCount; i++) {
            String label = meta.getColumnLabel(i);
            headers.add(label);
            colWidths.add(label.length());
        }

        List<List<String>> rows = new ArrayList<>();
        while (rs.next()) {
            List<String> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String val = rs.getString(i);
                if (val == null) val = "NULL";
                row.add(val);
                if (val.length() > colWidths.get(i - 1)) {
                    colWidths.set(i - 1, val.length());
                }
            }
            rows.add(row);
        }

        printTable(headers, rows, colWidths);
    }

    public static void printCustomTable(List<String> headers, List<List<String>> rows) {
        List<Integer> colWidths = new ArrayList<>();
        for (String h : headers) {
            colWidths.add(h.length());
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                String val = row.get(i) == null ? "" : row.get(i);
                if (i < colWidths.size() && val.length() > colWidths.get(i)) {
                    colWidths.set(i, val.length());
                }
            }
        }

        printTable(headers, rows, colWidths);
    }

    private static void printTable(List<String> headers, List<List<String>> rows, List<Integer> colWidths) {
        StringBuilder border = new StringBuilder("+");
        for (int w : colWidths) {
            border.append("-".repeat(w + 2)).append("+");
        }

        System.out.println(border);

        // Print header
        StringBuilder headerLine = new StringBuilder("|");
        for (int i = 0; i < headers.size(); i++) {
            headerLine.append(" ").append(padRight(headers.get(i), colWidths.get(i))).append(" |");
        }
        System.out.println(headerLine);
        System.out.println(border);

        // Print rows
        if (rows.isEmpty()) {
            System.out.println("| " + padRight("No records found.", border.length() - 4) + " |");
        } else {
            for (List<String> row : rows) {
                StringBuilder rowLine = new StringBuilder("|");
                for (int i = 0; i < headers.size(); i++) {
                    String val = i < row.size() ? row.get(i) : "";
                    rowLine.append(" ").append(padRight(val, colWidths.get(i))).append(" |");
                }
                System.out.println(rowLine);
            }
        }
        System.out.println(border);
    }

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        return String.format("%-" + n + "s", s);
    }
}
