package com.highcom.comicmemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListDataManager {
    private static ListDataManager listDataManager;
    private SQLiteDatabase rdb;
    private SQLiteDatabase wdb;
    private Map<String, String> data;
    private List<Map<String, String>> dataList1;
    private List<Map<String, String>> dataList2;
    private String sortKey1;
    private String sortKey2;
    private int lastUpdateId;

    public static ListDataManager createInstance(Context context) {
        listDataManager = new ListDataManager(context);
        return listDataManager;
    }

    public static ListDataManager getInstance() {
        return listDataManager;
    }

    private ListDataManager(Context context) {
        lastUpdateId = 0;
        sortKey1 = "id";
        sortKey2 = "id";
        ListDataOpenHelper helper = new ListDataOpenHelper(context);
        rdb = helper.getReadableDatabase();
        wdb = helper.getWritableDatabase();
        dataList1 = new ArrayList<Map<String, String>>();
        dataList2 = new ArrayList<Map<String, String>>();
        remakeAllListData();
    }

    public void setLastUpdateId(int id) {
        lastUpdateId = id;
    }

    public int getLastUpdateId() {
        return lastUpdateId;
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
        } else {
            // 新規作成の場合
            wdb.insert("comicdata", data.get("id"), values);
        }
        remakeAllListData();
    }

    public void deleteData(String id) {
        // データベースから削除する
        wdb.delete("comicdata", "id=?", new String[] { id });

        remakeAllListData();
    }

    public List<Map<String, String>> getDataList(long dataIndex) {
        List<Map<String, String>> dataList;
        if (dataIndex == 0) {
            dataList = dataList1;
        } else {
            dataList = dataList2;
        }
        return dataList;
    }

    public void rearrangeData(long dataIndex, int fromPos, int toPos) {
        boolean mov;
        ContentValues values;

        Cursor cur = getCurrentCursor(dataIndex);

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

        remakeAllListData();
    }

    public long getNewId() {
        long newId = 0;
        Cursor cur = getAllCursor();

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

    public void remakeAllListData() {
        remakeListData(0);
        sortListData(0, sortKey1);
        remakeListData(1);
        sortListData(1, sortKey2);
    }

    public void remakeListData(long dataIndex) {
        List<Map<String, String>> dataList;
        if (dataIndex == 0) {
            dataList = dataList1;
        } else {
            dataList = dataList2;
        }
        dataList.clear();

        Cursor cur = getCurrentCursor(dataIndex);

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

    public void sortListData(long dataIndex, final String key) {
        List<Map<String, String>> dataList;
        if (dataIndex == 0) {
            dataList = dataList1;
            sortKey1 = key;
        } else {
            dataList = dataList2;
            sortKey2 = key;
        }
        Collections.sort(dataList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> stringStringMap, Map<String, String> t1) {
                int result;
                if (key == "id") {
                   result = Integer.valueOf(stringStringMap.get("id")).compareTo(Integer.valueOf(t1.get("id")));
                } else {
                    result = stringStringMap.get(key).compareTo(t1.get(key));
                }

                // ソート順が決まらない場合には、idで比較する
                if (result == 0) {
                    result = Integer.valueOf(stringStringMap.get("id")).compareTo(Integer.valueOf(t1.get("id")));
                }
                return result;
            }
        });
    }

    private Cursor getCurrentCursor(long dataIndex) {
        return rdb.query("comicdata", new String[] { "id", "title", "author", "number", "memo", "inputdate", "status" }, "status=?", new String[]{Long.toString(dataIndex)}, null, null, "id ASC");
    }

    private Cursor getAllCursor() {
        return rdb.query("comicdata", new String[] { "id", "title", "author", "number", "memo", "inputdate", "status" }, null, null, null, null, "id ASC");
    }
}
