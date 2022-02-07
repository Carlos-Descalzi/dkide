package io.datakitchen.ide.service;

import java.util.EventObject;

public class LibraryServiceEvent extends EventObject {

    private final String libraryName;

    public LibraryServiceEvent(Object source, String libraryName) {
        super(source);
        this.libraryName = libraryName;
    }

    public String getLibraryName() {
        return libraryName;
    }
}
