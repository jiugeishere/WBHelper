package number.nine.wbhelper.wbdata;

import android.content.Context;

import android.content.SharedPreferences;

import java.io.Serializable;


public class SharedPreferencesUtil {

    //存储的sharedpreferences文件名
    private static final String FILE_NAME = "WifiHelper";

    private static SharedPreferencesUtil sharedPreferencesUtil;
    private static SharedPreferences sp;
    private Context sharecontext;

    public SharedPreferencesUtil(Context context){
        this.sharecontext=context;
        sp=context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
    }

    public static SharedPreferencesUtil getInstance(Context context){
        if (sharedPreferencesUtil==null){
            sharedPreferencesUtil=new SharedPreferencesUtil(context);
        }
        return sharedPreferencesUtil;
    }

    /**
     * 保存数据到SharedPreferences
     *
     * @param key   键
     * @param value 需要保存的数据
     * @return 保存结果
     */
    public  <T extends Serializable> boolean putData(String key, T value) {
        boolean result;
        SharedPreferences.Editor editor = sp.edit();
        String type = value.getClass().getSimpleName();
        try {
            switch (type) {
                case "Boolean":
                    editor.putBoolean(key, (Boolean) value);
                    break;
                case "Long":
                    editor.putLong(key, (Long) value);
                    break;
                case "Float":
                    editor.putFloat(key, (Float) value);
                    break;
                case "String":
                    editor.putString(key, (String) value);
                    break;
                case "Integer":
                    editor.putInt(key, (Integer) value);
                    break;
            }
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        editor.apply();
        return result;
    }

    /**
     * 获取SharedPreferences中保存的数据
     *
     * @param key          键
     * @param defaultValue 获取失败默认值
     * @return 从SharedPreferences读取的数据
     */
    public Object getData(String key, Object defaultValue) {
        Object result = new Object();
        String type = defaultValue.getClass().getSimpleName();
        try {
            switch (type) {
                case "Boolean":
                    result = sp.getBoolean(key, (Boolean) defaultValue);
                    break;
                case "Long":
                    result = sp.getLong(key, (Long) defaultValue);
                    break;
                case "Float":
                    result = sp.getFloat(key, (Float) defaultValue);
                    break;
                case "String":
                    result = sp.getString(key, (String) defaultValue);
                    break;
                case "Integer":
                    result = sp.getInt(key, (Integer) defaultValue);
                    break;
            }
        } catch (Exception e) {
            result = null;
            e.printStackTrace();
        }
        return result;
    }


}