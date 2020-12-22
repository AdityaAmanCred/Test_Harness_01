package TEST_HARNESS.utils;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonUtils {
    public static boolean isNotNull(Object obj) {
        if (obj == null || obj.toString().toLowerCase().equals("null")) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasKeyWithNonNullValue(JsonObject jsonObject, String key) {
        JsonElement jsonElement = getJsonElement(jsonObject, key);
        if (jsonElement == null) {
            return false;
        }
        return true;
    }

    public static JsonElement getJsonElement(JsonObject jsonObject, String key) {
        List<String> keys = Arrays.asList(key.split("\\."));
        JsonObject parentObj = jsonObject;
        JsonObject childObj = null;
        JsonArray parentArr = null;
        JsonArray childArr = null;
        Integer type = null;
        int i = 0;
        int n = keys.size();
        do {
            if (!StringUtils.isNumeric(keys.get(i))) {
                if (!isNotNull(parentObj.get(keys.get(i)))) {
                    return null;
                }
                if (parentObj.get(keys.get(i)) instanceof JsonPrimitive && i == n - 1) {
                    return parentObj.get(keys.get(i));
                }
                if ((parentObj.get(keys.get(i)) instanceof JsonObject)) {
                    childObj = parentObj.get(keys.get(i)).getAsJsonObject();
                    type = 1;
                } else {
                    childArr = parentObj.get(keys.get(i)).getAsJsonArray();
                    type = 2;
                }
            } else {
                if (!isNotNull(parentArr.get(Integer.parseInt(keys.get(i))))) {
                    return null;
                }
                if (parentArr.get(Integer.parseInt(keys.get(i))) instanceof JsonPrimitive && i == n - 1) {
                    return parentArr.get(Integer.parseInt(keys.get(i)));
                }
                if (parentArr.get(Integer.parseInt(keys.get(i))) instanceof JsonObject) {
                    childObj = parentArr.get(Integer.parseInt(keys.get(i))).getAsJsonObject();
                    type = 1;
                } else {
                    childArr = parentArr.get(Integer.parseInt(keys.get(i))).getAsJsonArray();
                    type = 2;
                }
            }
            parentObj = childObj;
            parentArr = childArr;
            i++;
        } while (i < n && (childObj != null || childArr != null));
        if (childObj == null && childArr == null) {
            return null;
        } else {
            if (type.equals(1)) {
                return childObj;
            } else {
                return childArr;
            }
        }
    }
}
