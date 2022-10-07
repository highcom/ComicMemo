package com.highcom.comicmemo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.util.*

/**
 * 巻数の一覧データ管理クラス
 *
 * @param context コンテキスト
 */
class ListDataManager private constructor(context: Context) {
    /** 読み取り用DBアクセス */
    private val rdb: SQLiteDatabase
    /** 書き込み用DBアクセス */
    private val wdb: SQLiteDatabase
    /** 巻数データ */
    private var data: MutableMap<String, String>? = null
    /** 続刊用のデータリスト */
    private val dataList1: MutableList<Map<String, String>>
    /** 完結用のデータリスト */
    private val dataList2: MutableList<Map<String, String>>
    /** 続刊用のソートキー */
    private var sortKey1 = "id"
    /** 完結用のソートキー */
    private var sortKey2 = "id"
    /** 最後に更新した巻数データID */
    var lastUpdateId = 0

    /**
     * 作成した巻数データの登録処理
     *
     * @param isEdit 編集かどうか
     * @param data 巻数データ
     */
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

    /**
     * 巻数データ削除処理
     *
     * @param id 削除対象のID
     */
    fun deleteData(id: String?) {
        // データベースから削除する
        wdb.delete("comicdata", "id=?", arrayOf(id))
        remakeAllListData()
    }

    /**
     * 巻数データ一覧取得処理
     *
     * @param dataIndex 巻数リストデータインデックス 0:続刊 1:完結
     * @return 巻数データ一覧
     */
    fun getDataList(dataIndex: Long): List<Map<String, String>> {
        val dataList: List<Map<String, String>> = if (dataIndex == 0L) {
            dataList1
        } else {
            dataList2
        }
        return dataList
    }

    /**
     * 巻数データの入れ替え処理
     *
     * @param dataIndex 巻数リストデータインデックス 0:続刊 1:完結
     * @param fromPos 入れ替え元ID
     * @param toPos 入れ替え先ID
     */
    fun rearrangeData(dataIndex: Long, fromPos: Int, toPos: Int) {
        var mov: Boolean
        val cur = getCurrentCursor(dataIndex)
        mov = cur.moveToPosition(fromPos)
        if (!mov) return
        val fromId = cur.getLong(0)
        mov = cur.moveToPosition(toPos)
        if (!mov) return
        val toId = cur.getLong(0)
        var values = ContentValues()
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

    /**
     * 新規IDの発行プロパティ
     */
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

    /**
     * データベースをクローズする
     *
     */
    fun closeData() {
        rdb.close()
        wdb.close()
    }

    /**
     * 巻数データ一覧の再作成処理
     *
     */
    private fun remakeAllListData() {
        remakeListData(0)
        sortListData(0, sortKey1)
        remakeListData(1)
        sortListData(1, sortKey2)
    }

    /**
     * 指定された巻数データ一覧の再作成処理
     *
     * @param dataIndex 巻数リストデータインデックス 0:続刊 1:完結
     */
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

    /**
     * 指定された巻数データ一覧を指定されたキーでソートする
     *
     * @param dataIndex 巻数リストデータインデックス 0:続刊 1:完結
     * @param key ソートキー
     */
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

    /**
     * 現在のDBカーソル取得処理
     *
     * @param dataIndex 巻数リストデータインデックス 0:続刊 1:完結
     * @return カーソル
     */
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

    /**
     * 全データのカーソルプロパティ
     */
    private val allCursor: Cursor
        get() = rdb.query(
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