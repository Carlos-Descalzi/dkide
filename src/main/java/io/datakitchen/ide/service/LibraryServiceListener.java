package io.datakitchen.ide.service;

import java.util.EventListener;

public interface LibraryServiceListener extends EventListener {

    void libraryAdded(LibraryServiceEvent event);
}
