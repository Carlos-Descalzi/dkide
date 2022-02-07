package io.datakitchen.ide.model;

public interface SQLKey extends Key{
    String getQueryFile();
    void setQueryFile(String queryFile);
    QueryType getQueryType();
    void setQueryType(QueryType queryType);
    CsvOptions getCsvOptions();
    void setCsvOptions(CsvOptions csvOptions);
    DumpType getDumpType();
    void setDumpType(DumpType dumpType);

}
