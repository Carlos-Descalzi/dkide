package io.datakitchen.ide.model;

public class CsvOptions {
    private String columnDelimiter;
    private String rowDelimiter;
    private boolean titles;

    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;
    }

    public String getRowDelimiter() {
        return rowDelimiter;
    }

    public void setRowDelimiter(String rowDelimiter) {
        this.rowDelimiter = rowDelimiter;
    }

    public boolean isTitles() {
        return titles;
    }

    public void setTitles(boolean titles) {
        this.titles = titles;
    }

}
