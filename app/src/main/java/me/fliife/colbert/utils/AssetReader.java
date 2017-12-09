package me.fliife.colbert.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AssetReader {
    public static String readAsset(String path, Context context) {
        StringBuilder result;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(path)));
            result = new StringBuilder();
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                result.append(mLine);
                result.append("\n");
            }
        } catch (IOException e) {
            Log.e("readAsset:catch", "Error reading asset:");
            e.printStackTrace();
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("readAsset:finally", "Error closing bufferedReader:");
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }
}
