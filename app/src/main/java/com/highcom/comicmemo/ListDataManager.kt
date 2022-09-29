package com.highcom.comicmemo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.util.*

class ListDataManager private constructor(context: Context) {
    private val rdb: SQLiteDatabase
    private val wdb: SQLiteDatabase
    private var data: MutableMap<String, String>? = null
    private val dataList1: MutableList<Map<String, String>>
    private val dataList2: MutableList<Map<String, String>>
    private var sortKey1 = "id"
    private var sortKey2 = "id"
    var lastUpdateId = 0
    fun setData(isEdit: Boolean, data: Map<String, String>) {
        // データベースに追加or編集する
        val values = ContentValues()
        values.put("id", java.lang.Long.valueOf(data["id"]).toLong())
        values.put("title", data["title"])
        values.put("author", data["author"])
        values.put("number", data["number"])
        values.put("memo", data["memo"])
        values.put("inputdate", data["inputdate"])
        values.put("status", data["status"])
        if (isEdit) {
            // 編集の場合
            wdb.update("comicdata", values, "id=?", arrayOf(data["id"]))
        } else {
            // 新規作成の場合
            wdb.insert("comicdata", data["id"], values)
        }
        remakeAllListData()
    }

    fun deleteData(id: String?) {
        // データベースから削除する
        wdb.delete("comicdata", "id=?", arrayOf(id))
        remakeAllListData()
    }

    fun getDataList(dataIndex: Long): List<Map<String, String>> {
        val dataList: List<Map<String, String>>
        dataList = if (dataIndex == 0L) {
            dataList1
        } else {
            dataList2
        }
        return dataList
    }

    fun rearrangeData(dataIndex: Long, fromPos: Int, toPos: Int) {
        var mov: Boolean
        var values: ContentValues
        val cur = getCurrentCursor(dataIndex)
        mov = cur.moveToPosition(fromPos)
        if (!mov) return
        val fromId = cur.getLong(0)
        mov = cur.moveToPosition(toPos)
        if (!mov) return
        val toId = cur.getLong(0)
        values = ContentValues()
        values.put("id", -1)
        wdb.update("comicdata", values, "id=?", arrayOf(java.lang.Long.toString(fromId)))
        values = ContentValues()
        values.put("id", fromId)
        wdb.update("comicdata", values, "id=?", arrayOf(java.lang.Long.toString(toId)))
        values = ContentValues()
        values.put("id", toId)
        wdb.update("comicdata", values, "id=?", arrayOf(java.lang.Long.toString(-1)))
        remakeAllListData()
    }

    val newId: Long
        get() {
            var newId: Long = 0
            val cur = allCursor
            var mov = cur.moveToFirst()
            var curId: Long
            while (mov) {
                curId = java.lang.Long.valueOf(cur.getString(0)).toLong()
                if (newId < curId) {
                    newId = curId
                }
                mov = cur.moveToNext()
            }
            return newId + 1
        }

    fun closeData() {
        rdb.close()
        wdb.close()
    }

    fun remakeAllListData() {
        remakeListData(0)
        sortListData(0, sortKey1)
        remakeListData(1)
        sortListData(1, sortKey2)
    }

    fun remakeListData(dataIndex: Long) {
        val dataList: MutableList<Map<String, String>>
        dataList = if (dataIndex == 0L) {
            dataList1
        } else {
            dataList2
        }
        dataList.clear()
        val cur = getCurrentCursor(dataIndex)
        var mov = cur.moveToFirst()
        while (mov) {
            data = HashMap()
            data["id"] = cur.getString(0)
            data["title"] = cur.getString(1)
            data["author"] = cur.getString(2)
            data["number"] = cur.getString(3)
            data["memo"] = cur.getString(4)
            data["inputdate"] = cur.getString(5)
            data["status"] = cur.getString(6)
            dataList.add(data)
            mov = cur.moveToNext()
        }
    }

    fun sortListData(dataIndex: Long, key: String) {
        val dataList: List<Map<String, String>>
        if (dataIndex == 0L) {
            dataList = dataList1
            sortKey1 = key
        } else {
            dataList = dataList2
            sortKey2 = key
        }
        Collections.sort(dataList, Comparator<Map<String?, String>> { stringStringMap, t1 ->
            var result: Int
            result = if (key === "id") {
                Integer.valueOf(stringStringMap["id"]).compareTo(
                    Integer.valueOf(
                        t1["id"]
                    )
                )
            } else {
                stringStringMap[key]!!.compareTo(t1[key]!!)
            }

            // ソート順が決まらない場合には、idで比較する
            if (result == 0) {
                result = Integer.valueOf(stringStringMap["id"]).compareTo(
                    Integer.valueOf(
                        t1["id"]
                    )
                )
            }
            result
        })
    }

    private fun getCurrentCursor(dataIndex: Long): Cursor {
        return rdb.query(
            "comicdata",
            arrayOf("id", "title", "author", "number", "memo", "inputdate", "status"),
            "status=?",
            arrayOf(java.lang.Long.toString(dataIndex)),
            null,
            null,
            "id ASC"
        )
    }

    private val allCursor: Cursor
        private get() = rdb.query(
            "comicdata",
            arrayOf("id", "title", "author", "number", "memo", "inputdate", "status"),
            null,
            null,
            null,
            null,
            "id ASC"
        )

    companion object {
        var instance: ListDataManager? = null
            private set

        fun createInstance(context: Context): ListDataManager? {
            instance = ListDataManager(context)
            return instance
        }
    }

    init {
        val helper = ListDataOpenHelper(context)
        rdb = helper.readableDatabase
        wdb = helper.writableDatabase
        dataList1 = ArrayList()
        dataList2 = ArrayList()
        remakeAllListData()
    }
}