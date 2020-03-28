package com.highcom.comicmemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListDataManager {
    private static ListDataManager manager;

    private SQLiteDatabase rdb;
    private SQLiteDatabase wdb;
    private Map<String, String> data;
    private List<Map<String, String>> dataList;
    private int dataIndex;

    public ListDataManager(Context context, int index) {
        ListDataOpenHelper helper = new ListDataOpenHelper(context);
        rdb = helper.getReadableDatabase();
        wdb = helper.getWritableDatabase();
        dataList = new ArrayList<Map<String, String>>();
        dataIndex = index;
        remakeListData();
    }

    public void setData(boolean isEdit, Map<String, String> data) {
        // データベースに追加or編集する
        ContentValues values = new ContentValues();
        values.put("id", Long.valueOf(data.get("id")).longValue());
        values.put("title", data.get("title"));
        values.put("author", data.get("author"));
        values.put("number", data.get("number"));
        values.put("memo", data.get("memo"));
        values.put("inputdate", data.get("inputdate"));
        values.put("status", data.get("status"));
        if (isEdit) {
            // 編集の場合
            wdb.update("comicdata", values, "id=?", new String[] { data.get("id") });
            remakeListData();
        } else {
            // 新規作成の場合
            wdb.insert("comicdata", data.get("id"), values);
            dataList.add(data);
        }

    }

    public void deleteData(String id) {
        // データベースから削除する
        wdb.delete("comicdata", "id=?", new String[] { id });

        remakeListData();
    }

    public List<Map<String, String>> getDataList() {
        return dataList;
    }

    public void rearrangeData(int fromPos, int toPos) {
        boolean mov;
        ContentValues values;

        Cursor cur = getCursor();

        mov = cur.moveToPosition(fromPos);
        if (!mov) return;
        long fromId = cur.getLong(0);

        mov = cur.moveToPosition(toPos);
        if (!mov) return;
        long toId = cur.getLong(0);

        values = new ContentValues();
        values.put("id", -1);
        wdb.update("comicdata", values, "id=?", new String[] { Long.toString(fromId) });

        values = new ContentValues();
        values.put("id", fromId);
        wdb.update("comicdata", values, "id=?", new String[] { Long.toString(toId) });

        values = new ContentValues();
        values.put("id", toId);
        wdb.update("comicdata", values, "id=?", new String[] { Long.toString(-1) });

        remakeListData();
    }

    public long getNewId() {
        long newId = 0;
        Cursor cur = getCursor();

        boolean mov = cur.moveToFirst();
        long curId;
        while (mov) {
            curId = Long.valueOf(cur.getString(0)).longValue();
            if (newId < curId) {
                newId = curId;
            }
            mov = cur.moveToNext();
        }

        return newId + 1;
    }

    public void closeData() {
        rdb.close();
        wdb.close();
    }

    private void remakeListData() {
        dataList.clear();

        Cursor cur = getCursor();

        boolean mov = cur.moveToFirst();
        while (mov) {
            data = new HashMap<String, String>();
            data.put("id", cur.getString(0));
            data.put("title", cur.getString(1));
            data.put("author", cur.getString(2));
            data.put("number", cur.getString(3));
            data.put("memo", cur.getString(4));
            data.put("inputdate", cur.getString(5));
            data.put("status", cur.getString(6));
            dataList.add(data);
            mov = cur.moveToNext();
        }
    }

    private Cursor getCursor() {
        return rdb.query("comicdata", new String[] { "id", "title", "author", "number", "memo", "inputdate", "status" }, "status=?", new String[]{Integer.toString(dataIndex)}, null, null, "id ASC");
    }

    public String getNowDate(){
        Date date = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(date);
    }
}
