package me.fliife.colbert.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static me.fliife.colbert.utils.DatabaseStructure.*;

public class PronoteSQLOpener extends SQLiteOpenHelper {
    public PronoteSQLOpener(Context context) {
        super(context, "pronote", null, 2);
    }

    public static void clearDatabase(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_DROP + TABLE_TAF);
        sqLiteDatabase.execSQL(SQL_DROP + TABLE_MARKS);
        sqLiteDatabase.execSQL(SQL_DROP + TABLE_AVERAGES);
        sqLiteDatabase.execSQL(SQL_DROP + TABLE_SCHEDULE);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Set up database structure
        sqLiteDatabase.execSQL("CREATE TABLE taf (content TEXT, date TEXT, sub TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE marks (subject TEXT, coef TEXT, title TEXT, value TEXT, bareme TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE averages (subject TEXT, average TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE schedule (classroom TEXT, date TEXT, hour TEXT, isTeacherMissing BOOLEAN, sub TEXT, teacherName TEXT, notice TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        clearDatabase(sqLiteDatabase);
        onCreate(sqLiteDatabase);
    }
}
