package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class DataLoader {

    public static float[][] loadDataFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            JSONArray jsonArray = new JSONArray(json);
            float[][] data = new float[jsonArray.length()][3];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray row = jsonArray.getJSONArray(i);
                data[i][0] = (float) row.getDouble(0); // 时间
                data[i][1] = (float) row.getDouble(1); // 第二列，可能是其他数据
                data[i][2] = (float) row.getDouble(2); // 睡眠状态
            }
            return data;

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
