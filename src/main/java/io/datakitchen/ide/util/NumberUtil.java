package io.datakitchen.ide.util;

public class NumberUtil {

    private static final float KB = 1024;
    private static final float MB = KB * 1024;
    private static final float GB = MB * 1024;
    private static final float TB = GB * 1024;

    public static String formatInBytes(long number){
        if (number < KB){
            return number+ " B";
        } else if (number < MB){
            return String.format ("%10.2f KB", number / KB).trim();
        } else if (number < GB){
            return String.format ("%10.2f MB",number / MB).trim();
        } else if (number < TB){
            return String.format ("%10.2f GB",number / GB).trim();
        } else {
            return String.format ("%10.2f TB",number / TB).trim();
        }
    }
}
