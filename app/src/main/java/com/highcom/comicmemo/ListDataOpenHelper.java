package com.highcom.comicmemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by koichi on 2016/08/11.
 */
public class ListDataOpenHelper extends SQLiteOpenHelper {
    public ListDataOpenHelper(Context context) {
        super(context, "ComicMemoDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table comicdata("
                + "id long not null,"
                + "title text,"
                + "number text,"
                + "memo text,"
                + "inputdate text"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
