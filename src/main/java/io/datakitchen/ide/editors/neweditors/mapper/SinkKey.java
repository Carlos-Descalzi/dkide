package io.datakitchen.ide.editors.neweditors.mapper;

import io.datakitchen.ide.model.DumpType;
import io.datakitchen.ide.model.Key;

/**
 * Keys in data sinks are associated to a key
 * in data sources
 */
public interface SinkKey extends Key{

    Key getSourceKey();
    DumpType getDumpType();
    void setDumpType(DumpType dumpType);

}
