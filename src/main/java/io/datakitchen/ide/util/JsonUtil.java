package io.datakitchen.ide.util;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.json.VariableReference;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import net.minidev.json.writer.JsonReaderI;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes","unchecked"})
public class JsonUtil {

    private static final JsonReaderI READER = JSONValue.defaultReader.DEFAULT_ORDERED;

    public static <T>  T read(URL url) throws IOException, ParseException {
        return (T) newParser().parse(url.openStream(),READER);
    }
    public static <T> T read(VirtualFile file) throws IOException, ParseException {
        return (T) newParser().parse(file.getInputStream(),READER);
    }
    public static <T> T read(String text) throws IOException, ParseException {
        return (T) newParser().parse(text,READER);
    }
    public static <T> T read(InputStream content) throws UnsupportedEncodingException, ParseException {
        return (T) newParser().parse(content,READER);
    }
    private static JSONParser newParser(){
        return new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
    }
    public static void write(Map<String,Object> jsonObject, VirtualFile file) throws IOException {
        write(jsonObject, jsonObject, file);
    }
    public static void write(Object requester, Map<String,Object> jsonObject, VirtualFile file) throws IOException {
        try (OutputStream out = file.getOutputStream(requester)){
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(toJsonString(jsonObject));
            writer.flush();
        }
    }

    public static void write(Map<String, Object> jsonObject, OutputStream output) throws IOException{
        OutputStreamWriter writer = new OutputStreamWriter(output);
        writer.write(toJsonString(jsonObject));
        writer.flush();
    }

    public static void write(Map<String,Object> jsonObject, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)){
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(toJsonString(jsonObject));
            writer.flush();
        }
    }

    public static String toJsonString(Object obj){
        return writeObj(new StringBuilder(), obj, 0).toString();
    }

    private static StringBuilder writeObj(StringBuilder builder, Object obj, int spacing){
        if (obj instanceof Map){
            return writeJsonObj(builder, ((Map<String,Object>) obj),spacing);
        } else if (obj instanceof List){
            return writeJsonArray(builder, ((List<Object>)obj),spacing);
        } else if (obj instanceof Number || obj instanceof Boolean){
            return builder.append(obj);
        } else if (obj instanceof String){
            return builder.append('"').append(
                escape((String)obj)
            ).append('"');
        } else if (obj instanceof VariableReference){
            return builder
                .append("{{")
                .append(((VariableReference)obj).getVariable())
                .append("}}");
        }
        return builder.append("null");
    }

    private static StringBuilder escape(String s) {
        // this code has been copied from the json parser.
        StringBuilder out = new StringBuilder();

        int len = s.length();

        for(int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            switch(ch) {
                case '\b':
                    out.append("\\b");
                    continue;
                case '\t':
                    out.append("\\t");
                    continue;
                case '\n':
                    out.append("\\n");
                    continue;
                case '\f':
                    out.append("\\f");
                    continue;
                case '\r':
                    out.append("\\r");
                    continue;
                case '"':
                    out.append("\\\"");
                    continue;
                case '\\':
                    out.append("\\\\");
                    continue;
            }

            if ((ch < 0 || ch > 31) && (ch < 127 || ch > 159) && (ch < 8192 || ch > 8447)) {
                out.append(ch);
            } else {
                out.append("\\u");
                String hex = "0123456789ABCDEF";
                out.append(hex.charAt(ch >> 12 & 15));
                out.append(hex.charAt(ch >> 8 & 15));
                out.append(hex.charAt(ch >> 4 & 15));
                out.append(hex.charAt(ch >> 0 & 15));
            }
        }

        return out;
    }

    private static StringBuilder writeJsonObj(StringBuilder builder, Map<String, Object> obj, int spacing){
        builder.append("{\n");

        for (Iterator<Map.Entry<String, Object>> i = obj.entrySet().iterator();i.hasNext();){

            Map.Entry<String, Object> entry = i.next();

            indent(builder,spacing+1).append('"').append(entry.getKey()).append('"').append(" : ");

            writeObj(builder,entry.getValue(),spacing+1);
            if (i.hasNext()){
                builder.append(",\n");
            }
        }
        return indent(builder.append("\n"),spacing).append("}");
    }
    private static StringBuilder writeJsonArray(StringBuilder builder, List<Object> obj, int spacing){
        builder.append("[\n");

        for (Iterator<Object> i = obj.iterator(); i.hasNext();){
            Object item = i.next();

            writeObj(indent(builder,spacing+1), item, spacing+1);

            if (i.hasNext()){
                builder.append(",\n");
            }
        }

        return indent(builder.append("\n"),spacing).append("]");
    }

    private static StringBuilder indent(StringBuilder builder, int count){
        return builder.append(" ".repeat(count*4));
    }

}
