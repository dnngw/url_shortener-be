package shortlink.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;

/*
* Json Convert
* */
public class JsonUtil {
    /*
    * Gson instance
    * */
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .create();

    /*
    * Convert object to json
    * */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /*
    * Convert json to object
    * */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /*
    * OffsetDateTime Adapter
    *
    * Serialize OffsetDateTime as ISO-8601 string (ex: 2026-11-03T14:53:59.262+07:00)
    * */
    private static class OffsetDateTimeAdapter extends TypeAdapter<OffsetDateTime> {
        @Override
        public void write(JsonWriter out, OffsetDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value.toString());
        }

        @Override
        public OffsetDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return OffsetDateTime.parse(in.nextString());
        }
    }
}
