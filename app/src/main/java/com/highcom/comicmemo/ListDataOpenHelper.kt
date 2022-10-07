package com.highcom.comicmemo

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

/**
 * DBアクセス用ヘルパークラス
 *
 * @param context コンテキスト
 */
class ListDataOpenHelper(context: Context?) : SQLiteOpenHelper(context, "ComicMemoDB", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table comicdata("
                    + "id long not null,"
                    + "title text default '',"
                    + "author text default '',"
                    + "number text default '1',"
                    + "memo text default '',"
                    + "inputdate text default '',"
                    + "status long default 0"
                    + ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val targetTable = "comicdata"
        db.beginTransaction()
        try {
            // 初期化
            db.execSQL("ALTER TABLE $targetTable RENAME TO temp_$targetTable")
            // 元カラム一覧
            val columns = getColumns(db, "temp_$targetTable")
            onCreate(db)
            // 新カラム一覧
            val newColumns: List<String>? = getColumns(db, targetTable)

            // 変化しないカラムのみ抽出
            columns!!.retainAll(newColumns!!)

            // 共通データを移す。(OLDにしか存在しないものは捨てられ, NEWにしか存在しないものはNULLになる)
            val cols = join(columns, ",")
            db.execSQL(
                String.format(
                    "INSERT INTO %s (%s) SELECT %s from temp_%s", targetTable,
                    cols, cols, targetTable
                )
            )
            // 終了処理
            db.execSQL("DROP TABLE temp_$targetTable")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private fun getColumns(db: SQLiteDatabase, tableName: String): MutableList<String>? {
            var ar: MutableList<String>? = null
            var c: Cursor? = null
            try {
                c = db.rawQuery("SELECT * FROM $tableName LIMIT 1", null)
                if (c != null) {
                    ar = ArrayList(Arrays.asList(*c.columnNames))
                }
            } finally {
                c?.close()
            }
            return ar
        }

        private fun join(list: List<String>?, delim: String): String {
            val buf = StringBuilder()
            val num = list!!.size
            for (i in 0 until num) {
                if (i != 0) buf.append(delim)
                buf.append(list[i])
            }
            return buf.toString()
        }
    }
}