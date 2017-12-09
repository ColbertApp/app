package me.fliife.colbert.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import me.fliife.colbert.utils.CredentialsHolder;
import me.fliife.colbert.utils.DatabaseStructure;
import me.fliife.colbert.utils.PronoteObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static me.fliife.colbert.utils.DateUtils.isPast;

/**
 * Copyright 2016-2017 FliiFe (Théophile Cailliau)
 * This file is part of pronote-app.
 * <p>
 * pronote-app is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * pronote-app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with pronote-app.  If not, see <http://www.gnu.org/licenses/>.
 */

public class StorageUtils {
    private static String preferencesFile = "xyz.fliife.pronote.sp";

    public static void saveCredentials(Context context, CredentialsHolder credentialsHolder) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", credentialsHolder.username);
        editor.putString("password", credentialsHolder.password);
        editor.putString("url", credentialsHolder.url);
        editor.apply();
    }

    public static CredentialsHolder getCredentials(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        return new CredentialsHolder(context,
                sharedPreferences.getString("username", ""),
                sharedPreferences.getString("password", ""),
                sharedPreferences.getString("url", ""));
    }

    public static boolean firstTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("first2", true);
    }

    public static void setFirstTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("first2", false);
        editor.apply();
    }

    public static void removeCredentials(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username");
        editor.remove("password");
        editor.remove("url");
        editor.apply();
    }

    public static long getLastChecked(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        return sharedPreferences.getLong("lastchecked", 0);
    }

    public static void setLastChecked(long lastChecked, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastchecked", lastChecked);
        editor.apply();
    }

    public static PronoteObject getPronoteObjectFromDatabase(Context context) {
        PronoteObject pronoteObject = new PronoteObject();
        SQLiteDatabase sqLiteDatabase = new PronoteSQLOpener(context).getReadableDatabase();
        Cursor tafCursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DatabaseStructure.TABLE_TAF, null);
        if (tafCursor.getCount() != 0) {
            tafCursor.moveToFirst();
            do {
                ContentValues tafElement = new ContentValues();
                tafElement.put("content", tafCursor.getString(0));
                tafElement.put("date", tafCursor.getString(1));
                tafElement.put("sub", tafCursor.getString(2));
                if (!isPast(tafElement.getAsString("date"))) {
                    pronoteObject.insertTafElement(tafElement);
                }
            } while (tafCursor.moveToNext());
        }
        tafCursor.close();
        Cursor schedule = sqLiteDatabase.rawQuery("SELECT * FROM " + DatabaseStructure.TABLE_SCHEDULE, null);
        if (schedule.getCount() != 0) {
            schedule.moveToFirst();
            do {
                ContentValues scheduleElement = new ContentValues();
                scheduleElement.put("classroom", schedule.getString(0));
                scheduleElement.put("date", schedule.getString(1));
                scheduleElement.put("hour", schedule.getString(2));
                scheduleElement.put("isTeacherMissing", schedule.getString(3));
                scheduleElement.put("sub", schedule.getString(4));
                scheduleElement.put("teacherName", schedule.getString(5));
                scheduleElement.put("notice", schedule.getString(6));
                if (!isPast(scheduleElement.getAsString("date"), scheduleElement.getAsString("hour"))) {
                    pronoteObject.insertScheduleElement(scheduleElement);
                }
            } while (schedule.moveToNext());
        }
        schedule.close();
        Cursor averages = sqLiteDatabase.rawQuery("SELECT * FROM " + DatabaseStructure.TABLE_AVERAGES, null);
        if (averages.getCount() != 0) {
            averages.moveToFirst();
            do {
                ContentValues averageElement = new ContentValues();
                averageElement.put("subject", averages.getString(0));
                averageElement.put("average", averages.getString(1));
                pronoteObject.insertAverageElement(averageElement);
            } while (averages.moveToNext());
        }
        averages.close();
        Cursor marks = sqLiteDatabase.rawQuery("SELECT * FROM " + DatabaseStructure.TABLE_MARKS, null);
        if (marks.getCount() != 0) {
            marks.moveToFirst();
            do {
                ContentValues markElement = new ContentValues();
                markElement.put("subject", marks.getString(0));
                markElement.put("coef", marks.getString(1));
                markElement.put("title", marks.getString(2));
                markElement.put("value", marks.getString(3));
                markElement.put("bareme", "N/A");
                if (marks.getColumnCount() == 5) {
                    markElement.put("bareme", marks.getString(4));
                }
                pronoteObject.insertMarkElement(markElement);
            } while (marks.moveToNext());
        }
        marks.close();
        return pronoteObject;
    }

    public static PronoteObject jsonToPronoteObject(JSONObject object) {
        PronoteObject pronoteObject = new PronoteObject();
        try {
            // See https://gitlab.com/FliiFe/pronote-api/#fetch-post for json structure
            JSONArray schedule = object.getJSONArray("schedule");
            for (int i = 0; i < schedule.length(); i++) {
                JSONObject jsonElement = schedule.getJSONObject(i);
                ContentValues scheduleElement = new ContentValues();
                scheduleElement.put("classroom", jsonElement.getString("classroom"));
                scheduleElement.put("date", jsonElement.getString("date"));
                scheduleElement.put("hour", jsonElement.getString("hour"));
                scheduleElement.put("sub", jsonElement.getString("sub"));
                scheduleElement.put("teacherName", jsonElement.getString("teacherName"));
                scheduleElement.put("isTeacherMissing", jsonElement.getBoolean("isTeacherMissing"));
                scheduleElement.put("notice", jsonElement.has("notice") ? jsonElement.getString("notice") : "");
                if (!isPast(scheduleElement.getAsString("date"), scheduleElement.getAsString("hour"))) {
                    pronoteObject.insertScheduleElement(scheduleElement);
                }
            }
            JSONArray taf = object.getJSONArray("taf");
            for (int i = 0; i < taf.length(); i++) {
                JSONObject jsonElement = taf.getJSONObject(i);
                ContentValues tafElement = new ContentValues();
                tafElement.put("date", jsonElement.getString("date"));
                tafElement.put("sub", jsonElement.getString("sub"));
                tafElement.put("content", jsonElement.getString("content"));
                if (!isPast(tafElement.getAsString("date"))) {
                    pronoteObject.insertTafElement(tafElement);
                }
            }
            JSONArray averages = object.getJSONArray("marks");
            for (int i = 0; i < averages.length(); i++) {
                JSONObject jsonElement = averages.getJSONObject(i);
                ContentValues averageElement = new ContentValues();
                averageElement.put("subject", jsonElement.getString("subject"));
                averageElement.put("average", jsonElement.getString("average"));
                JSONArray marks = jsonElement.getJSONArray("marks");
                for (int j = 0; j < marks.length(); j++) {
                    JSONObject jsonMarkElement = marks.getJSONObject(j);
                    ContentValues markElement = new ContentValues();
                    markElement.put("subject", jsonElement.getString("subject"));
                    markElement.put("coef", jsonMarkElement.getString("coef"));
                    markElement.put("bareme", jsonMarkElement.getString("bareme"));
                    markElement.put("title", jsonMarkElement.getString("title"));
                    markElement.put("value", jsonMarkElement.getString("value"));
                    pronoteObject.insertMarkElement(markElement);
                }
                pronoteObject.insertAverageElement(averageElement);
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return pronoteObject;
    }

    public static void putPronoteObjectIntoDatabase(Context context, PronoteObject pronoteObject) {
        SQLiteDatabase database = new PronoteSQLOpener(context).getWritableDatabase();
        ArrayList<ContentValues> taf = pronoteObject.getTaf();
        for (ContentValues tafElement : taf) {
            database.insert("taf", null, tafElement);
        }
        ArrayList<ContentValues> marks = pronoteObject.getMarks();
        for (ContentValues markElement : marks) {
            database.insert("marks", null, markElement);
        }
        ArrayList<ContentValues> averages = pronoteObject.getAverages();
        for (ContentValues averageElement : averages) {
            database.insert("averages", null, averageElement);
        }
        ArrayList<ContentValues> schedule = pronoteObject.getSchedule();
        for (ContentValues scheduleElement : schedule) {
            database.insert("schedule", null, scheduleElement);
        }
        database.close();
    }

    public static void putJSONIntoDatabase(Context context, JSONObject jsonObject) {
        putPronoteObjectIntoDatabase(context, jsonToPronoteObject(jsonObject));
    }

    public static void clearDatabase(Context context) {
        PronoteSQLOpener pronoteSQLOpener = new PronoteSQLOpener(context);
        pronoteSQLOpener.onUpgrade(pronoteSQLOpener.getWritableDatabase(), 0, 0);
    }
}
