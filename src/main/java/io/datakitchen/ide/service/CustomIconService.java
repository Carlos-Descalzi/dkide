package io.datakitchen.ide.service;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CustomIconService {

    private static final Logger LOGGER = Logger.getInstance(CustomIconService.class);

    private final Project project;

    private final Map<String, Icon> iconCache = new HashMap<>();

    public CustomIconService(Project project){
        this.project = project;
    }

    public static CustomIconService getInstance(Project project){
        return project.getService(CustomIconService.class);
    }

    public Icon getIcon(VirtualFile path, Dimension size){
        String key = path.getPath() + "," + size;

        if (!iconCache.containsKey(key)) {
            BufferedImage image = transcodeSVGToBufferedImage(new File(path.getPath()), size.width, size.height);
            if (image != null) {
                iconCache.put(key, new ImageIcon(image));
            }
        }

        return iconCache.get(key);
    }

    private BufferedImage transcodeSVGToBufferedImage(File file, int width, int height) {
        // https://stackoverflow.com/questions/11435671/how-to-get-a-bufferedimage-from-a-svg

        // Create a PNG transcoder.
        Transcoder t = new PNGTranscoder();

        // Set the transcoding hints.
        t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
        t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            // Create the transcoder input.
            TranscoderInput input = new TranscoderInput(inputStream);

            // Create the transcoder output.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);

            // Save the image.
            t.transcode(input, output);

            // Flush and close the stream.
            outputStream.flush();
            outputStream.close();

            // Convert the byte stream into an image.
            byte[] imgData = outputStream.toByteArray();
            return ImageIO.read(new ByteArrayInputStream(imgData));

        } catch (IOException | TranscoderException e) {
            LOGGER.error(e);
        }
        return null;
    }

}
