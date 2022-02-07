package io.datakitchen.ide.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectUtil {

    /**
     * The only 100% effective way to deep copy an object
     * is serializing and deserializing it.
     */
    @SuppressWarnings("unchecked")
    public static <T> T copy(T object) throws Exception{
        ByteArrayOutputStream s = new ByteArrayOutputStream();

        ObjectOutputStream out = new ObjectOutputStream(s);
        out.writeObject(object);
        out.flush();

        return (T)new ObjectInputStream(new ByteArrayInputStream(s.toByteArray())).readObject();
    }

    /**
     * Just avoids some annoying unchecked cast warnings
     * @param o the object to cast
     * @param <T> desired type
     * @return the same object
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o){
        return (T)o;
    }

}
