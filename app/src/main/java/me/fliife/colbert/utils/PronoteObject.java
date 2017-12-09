package me.fliife.colbert.utils;

import android.content.ContentValues;

import java.util.ArrayList;

public class PronoteObject {
    public ArrayList<ContentValues> schedule = new ArrayList<>();
    public ArrayList<ContentValues> taf = new ArrayList<>();
    public ArrayList<ContentValues> marks = new ArrayList<>();
    public ArrayList<ContentValues> averages = new ArrayList<>();

    public ArrayList<ContentValues> getSchedule() {
        return schedule;
    }

    public ArrayList<ContentValues> getTaf() {
        return taf;
    }

    public ArrayList<ContentValues> getMarks() {
        return marks;
    }

    public ArrayList<ContentValues> getAverages() {
        return averages;
    }

    public void insertScheduleElement(ContentValues scheduleElement) {
        schedule.add(scheduleElement);
    }

    public void insertMarkElement(ContentValues markElement) {
        marks.add(markElement);
    }

    public void insertAverageElement(ContentValues averageElement) {
        averages.add(averageElement);
    }

    public void insertTafElement(ContentValues tafElement) {
        taf.add(tafElement);
    }

    public ArrayList<ContentValues> getMarksBySubject(String subject) {
        ArrayList<ContentValues> result = new ArrayList<>();
        for (ContentValues contentValues : marks) {
            if (contentValues.getAsString("subject").equals(subject)) {
                result.add(contentValues);
            }
        }
        return result;
    }
}
