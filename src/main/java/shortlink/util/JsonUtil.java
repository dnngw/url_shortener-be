package shortlink.util;

import com.google.gson.Gson;

/*
* Json Convert
* */
public class JsonUtil {
    /*
    * Gson instance
    * */
    private static final Gson gson = new Gson();

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
}
