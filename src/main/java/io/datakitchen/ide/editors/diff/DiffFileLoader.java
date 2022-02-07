package io.datakitchen.ide.editors.diff;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class DiffFileLoader {

    public static final String DIFF_LOCAL = "<<<<<<< your ";
    public static final String DIFF_CHANGE = "=======";
    public static final String DIFF_REMOTE = ">>>>>>> their ";

    private final File sourceFile;

    private final StringBuilder left = new StringBuilder();
    private final StringBuilder right = new StringBuilder();

    public DiffFileLoader(VirtualFile sourceFile){
        this.sourceFile = new File(sourceFile.getPath());
    }

    public DiffFileLoader(File sourceFile){
        this.sourceFile = sourceFile;
    }

    public String getLeft() {
        return left.toString();
    }

    public String getRight() {
        return right.toString();
    }

    public void load() throws IOException {

        String leftToken = DIFF_LOCAL+sourceFile.getName();
        String rightToken = DIFF_REMOTE+sourceFile.getName();

        String contents;
        try (Reader reader = new FileReader(sourceFile)) {
            contents = IOUtils.toString(reader);
        }

        String current = contents;

        while(current.length() > 0){

            int index = current.indexOf(leftToken);
            if (index == -1){
                break;
            }
            this.feedBoth(current.substring(0,index));
            current = current.substring(index+leftToken.length());

            index = current.indexOf(DIFF_CHANGE);
            if (index == -1){
                break;
            }
            this.feedLeft(current.substring(0,index));
            current = current.substring(index+DIFF_CHANGE.length());
            index = current.indexOf(rightToken);
            if (index == -1){
                break;
            }
            this.feedRight(current.substring(0,index));
            current = current.substring(index+rightToken.length());

        }
        feedBoth(current);

    }

    private void feedBoth(String line){
        left.append(line);
        right.append(line);
    }

    private void feedLeft(String line){
        left.append(line);
    }

    private void feedRight(String line){
        right.append(line);
    }
}
