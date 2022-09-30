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
        val dataList: List<Map<String, String>> = if (dataIndex == 0L) {
            dataList1
        } else {
            dataList2
        }
        return dataList
    }

    fun rearrangeData(dataIndex: Long, fromPos: Int, toPos: Int) {
        var mov: Boolean
        val cur = getCurrentCursor(dataIndex)
        mov = cur.moveToPosition(fromPos)
        if (!mov) return
        val fromId = cur.getLong(0)
        mov = cur.moveToPosition(toPos)
        if (!mov) return
        val toId = cur.getLong(0)
        var values: ContentValues = ContentValues()
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

    private fun remakeAllListData() {
        remakeListData(0)
        sortListData(0, sortKey1)
        remakeListData(1)
        sortListData(1, sortKey2)
    }

    private fun remakeListData(dataIndex: Long) {
        val dataList: MutableList<Map<String, String>> = if (dataIndex == 0L) {
            dataList1
        } else {
            dataList2
        }
        dataList.clear()
        val cur = getCurrentCursor(dataIndex)
        var mov = cur.moveToFirst()
        while (mov) {
            data = HashMap()
            (data as HashMap<String, String>)["id"] = cur.getString(0)
            (data as HashMap<String, String>)["title"] = cur.getString(1)
            (data as HashMap<String, String>)["author"] = cur.getString(2)
            (data as HashMap<String, String>)["number"] = cur.getString(3)
            (data as HashMap<String, String>)["memo"] = cur.getString(4)
            (data as HashMap<String, String>)["inputdate"] = cur.getString(5)
            (data as HashMap<String, String>)["status"] = cur.getString(6)
            dataList.add(data as HashMap<String, String>)
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

        val comparator = Comparator<Map<String, String>> { t1, t2 ->
            var result = if (key === "id") {
                t1["id"]!!.compareTo(t2["id"]!!)
            } else {
                t1[key]!!.compareTo(t2[key]!!)
            }

            // ソート順が決まらない場合には、idで比較する
            if (result == 0) {
                result = t1["id"]!!.compareTo(t2["id"]!!)
            }
            return@Comparator result
        }
        dataList.sortedWith(comparator)
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