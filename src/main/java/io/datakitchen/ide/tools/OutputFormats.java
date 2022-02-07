package io.datakitchen.ide.tools;

import java.util.Set;

public class OutputFormats {
    public static final String CSV = "csv";
    public static final String BINARY = "binary";
    public static final String ZIP = "zip";
    public static final String TAR = "tar";
    public static final String TAR_GZ = "tar_gz";
    public static final String TAR_BZ2 = "tar_bz2";
    public static final String FOLDER = "folder";

    public final static Set<String> ARCHIVE_FORMATS = Set.of(TAR, ZIP, TAR_GZ, TAR_BZ2);

    public static String getByExtension(String filename){
        filename = filename.toLowerCase();
        if (filename.endsWith(".csv")) {
            return OutputFormats.CSV;
        } else if (filename.endsWith(".zip")) {
            return OutputFormats.ZIP;
        } else if (filename.endsWith(".tar")) {
            return OutputFormats.TAR;
        } else if (filename.endsWith(".tar.gz")){
            return OutputFormats.TAR_GZ;
        } else if (filename.endsWith(".tar.bz2")){
            return OutputFormats.TAR_BZ2;
        }
        return null;
    }
}
