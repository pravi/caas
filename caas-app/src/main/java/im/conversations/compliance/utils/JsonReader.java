package im.conversations.compliance.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class JsonReader<T> {
    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    private static final Gson gson = gsonBuilder.create();
    private final Class<T> typeClass;

    public JsonReader(Class<T> typeClass) {
        this.typeClass = typeClass;
    }

    //Ignore this
    private JsonReader() {
        this.typeClass = null;
    }

    public T read(File file) {
        try {
            System.out.println("Reading json file from " + file.getAbsolutePath());
            final T t = gson.fromJson(new FileReader(file), typeClass);
            return t;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Configuration file not found");
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid syntax in " + file.getName());
        }
    }
}
