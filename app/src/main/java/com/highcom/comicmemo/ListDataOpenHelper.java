package com.highcom.comicmemo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by koichi on 2016/08/11.
 */
public class ListDataOpenHelper extends SQLiteOpenHelper {
    public ListDataOpenHelper(Context context) {
        super(context, "ComicMemoDB", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table comicdata("
                + "id long not null,"
                + "title text default '',"
                + "author text default '',"
                + "number text default '1',"
                + "memo text default '',"
                + "inputdate text default '',"
                + "status long default 0"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String targetTable = "comicdata";
        db.beginTransaction();
        try {
            // 初期化
            db.execSQL("ALTER TABLE " + targetTable + " RENAME TO temp_" + targetTable);
            // 元カラム一覧
            final List<String> columns = getColumns(db, "temp_" + targetTable);
            onCreate(db);
            // 新カラム一覧
            final List<String> newColumns = getColumns(db, targetTable);

            // 変化しないカラムのみ抽出
            columns.retainAll(newColumns);

            // 共通データを移す。(OLDにしか存在しないものは捨てられ, NEWにしか存在しないものはNULLになる)
            final String cols = join(columns, ",");
            db.execSQL(String.format(
                    "INSERT INTO %s (%s) SELECT %s from temp_%s", targetTable,
                    cols, cols, targetTable));
            // 終了処理
            db.execSQL("DROP TABLE temp_" + targetTable);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> ar = null;
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
            if (c != null) {
                ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
            }
        } finally {
            if (c != null)
                c.close();
        }
        return ar;
    }

    private static String join(List<String> list, String delim) {
        final StringBuilder buf = new StringBuilder();
        final int num = list.size();
        for (int i = 0; i < num; i++) {
            if (i != 0)
                buf.append(delim);
            buf.append((String) list.get(i));
        }
        return buf.toString();
    }

}
