package cn.rongcloud.im.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * json转换工具类
 *
 */
public class JsonUtils {

    /**
     * 将对象转换为json字符串
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    /**
     * 将json字符串转为指定的对象
     *
     * @param json json字符串
     * @return
     */
    public static <T> T objectFromJson(String json, Class<T> classOfT) {
        Gson gson = new Gson();
        return gson.fromJson(json, classOfT);
    }

    /**
     * 将json字符串转为指定的ArrayList
     *
     * @param json
     * @return
     */
    public static <T> ArrayList<T> listFromJson(String json, Class<T> classOfT) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();
        ArrayList<JsonObject> jsonObjs = gson.fromJson(json, type);
        ArrayList<T> listOfT = new ArrayList<T>();
        for (JsonObject jsonObj : jsonObjs) {
            listOfT.add(gson.fromJson(jsonObj, classOfT));
        }
        return listOfT;
    }

    public static <T> T objectFromJson(JsonObject jsonObject, Class<T> classOfT) {
        Gson gson = new Gson();
        return gson.fromJson(jsonObject, classOfT);
    }

    public static <T> T objectFromJson(JsonElement jsonElement, Class<T> classOfT) {
        Gson gson = new Gson();
        return gson.fromJson(jsonElement, classOfT);
    }

    public static <T> ArrayList<T> listFromJson(JsonElement jsonElement, Class<T> classOfT) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();
        ArrayList<JsonObject> jsonObjs = gson.fromJson(jsonElement, type);
        ArrayList<T> listOfT = new ArrayList<T>();
        for (JsonObject jsonObj : jsonObjs) {
            listOfT.add(gson.fromJson(jsonObj, classOfT));
        }
        return listOfT;
    }

    public static ArrayList<String> stringListFromJson(JsonElement jsonElement){
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> strings = gson.fromJson(jsonElement, type);
        return strings;
    }

    public static ArrayList<String> StringListFromJson(JsonElement jsonElement){

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> strings = gson.fromJson(jsonElement, type);
        return strings;
    }
}
