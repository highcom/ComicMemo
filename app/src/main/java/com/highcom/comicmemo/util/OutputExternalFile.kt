package com.highcom.comicmemo.util

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.highcom.comicmemo.R
import com.highcom.comicmemo.datamodel.Comic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.OutputStream

/**
 * 外部ファイル出力処理クラス
 * * 巻数データをCSV形式のファイルとして出力する
 *
 * @property context コンテキスト
 */
class OutputExternalFile(private val context: Context) {
    /**
     * CSVファイル出力先確認ダイアログ表示処理
     *
     * @param uri ファイル出力先URI
     * @param comicList 出力対象の巻数データリスト
     * @param onComplete 出力完了時のコールバック
     */
    fun outputSelectFolder(
        uri: Uri?,
        comicList: List<Comic>,
        onComplete: (Boolean) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.output_csv))
            .setMessage(
                context.getString(R.string.output_message_front) + uri!!.path!!
                    .replace(
                        ":",
                        "/"
                    ) + System.getProperty("line.separator") + context.getString(R.string.output_message_rear)
            )
            .setPositiveButton(R.string.output_button) { _, _ ->
                exportDatabase(uri, comicList, onComplete)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * CSVファイル出力実行処理
     * * 巻数データをCSV形式に変換してファイル出力する
     *
     * @param uri 出力先ファイルURI
     * @param comicList 出力対象の巻数データリスト
     * @param onComplete 出力完了時のコールバック
     */
    private fun exportDatabase(
        uri: Uri?,
        comicList: List<Comic>,
        onComplete: (Boolean) -> Unit
    ) {
        var result = true
        var outputStream: OutputStream? = null

        try {
            outputStream = context.contentResolver.openOutputStream(uri!!)
            // CSVヘッダーを書き込む
            val header = "TITLE,TITLE_KANA,AUTHOR,PUBLISHER,ISBN,NUMBER,MEMO,INPUTDATE,STATUS" + System.getProperty("line.separator")
            outputStream!!.write(header.toByteArray())

            // データ行を書き込む
            for (comic in comicList) {
                val record = "${comic.title},${comic.title_kana},${comic.author},${comic.publisher},${comic.isbn},${comic.number},${comic.memo.replace("\n", "  ")},${comic.inputdate},${comic.status}" + System.getProperty("line.separator")
                outputStream.write(record.toByteArray())
            }
        } catch (exc: FileNotFoundException) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.output_csv))
                .setMessage(context.getString(R.string.no_access_message))
                .setPositiveButton(R.string.move) { _, _ ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.parse("package:" + context.packageName)
                    context.startActivity(intent)
                }
                .setNegativeButton(R.string.end, null)
                .show()
            result = false
        } catch (exc: Exception) {
            val ts = Toast.makeText(
                context,
                context.getString(R.string.csv_output_failed_message),
                Toast.LENGTH_SHORT
            )
            ts.show()
            result = false
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (result) {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.output_csv))
                    .setMessage(
                        context.getString(R.string.csv_output_complete_message) + System.getProperty("line.separator") + uri!!.path!!
                            .replace(":", "/")
                    )
                    .setPositiveButton(R.string.ok, null)
                    .show()
            }
            onComplete(result)
        }
    }
}