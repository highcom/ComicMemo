package com.highcom.comicmemo.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.os.HandlerCompat
import com.highcom.comicmemo.R
import com.highcom.comicmemo.datamodel.Comic
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Objects
import java.util.concurrent.Executors

/**
 * 外部ファイル取込処理クラス
 * * CSV形式のファイルを取り込む
 *
 * @property activity ダイアログ表示用アクティビティ
 * @property onImportComplete インポート完了時のコールバック
 */
class InputExternalFile(
    private val activity: Activity,
    private val onImportComplete: (List<Comic>) -> Unit
) {
    /** 巻数データ一覧 */
    private var comicList: MutableList<Comic>? = null
    /** 巻数データID */
    private var id = 0L
    /** 取込ファイルURI */
    private var uri: Uri? = null
    /** 取込処理中のプログレスダイアログ */
    private var progressDialog: ProgressDialog? = null
    /** 上書きモードか */
    private var isOverride = false

    /**
     * ファイル取込処理用バックグラウンドタスク
     *
     * @property _handler 処理ハンドラ
     */
    private inner class BackgroundTask(private val _handler: Handler) : Runnable {
        /**
         * ファイル取込実行処理
         *
         */
        @WorkerThread
        override fun run() {
            val postExecutor = PostExecutor()
            _handler.post(postExecutor)
        }
    }

    /**
     * バックグラウンド実行後のランナークラス
     *
     */
    private inner class PostExecutor : Runnable {
        /**
         * バックグラウンド実行後処理
         *
         */
        @UiThread
        override fun run() {
            progressDialog?.dismiss()
            val title = if (isOverride) activity.getString(R.string.input_csv_override) else activity.getString(R.string.input_csv_add)
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(
                    activity.getString(R.string.csv_input_complete_message) + System.getProperty(
                        "line.separator"
                    ) + getFileNameByUri(activity, uri)
                )
                .setPositiveButton(R.string.ok, null)
                .show()
            onImportComplete(comicList ?: emptyList())
        }
    }

    /**
     * CSVファイル取込元フォルダ選択確認ダイアログ表示処理
     *
     * @param uri 取込元ファイルURI
     * @param isOverride 上書きモードか
     */
    fun confirmInputDialog(uri: Uri?, isOverride: Boolean) {
        this.uri = uri
        this.isOverride = isOverride
        val title: String
        val messageFront: String
        val messageRear: String
        if (isOverride) {
            title = activity.getString(R.string.input_csv_override)
            messageFront = activity.getString(R.string.input_override_message_front)
            messageRear = activity.getString(R.string.input_override_message_rear)
        } else {
            title = activity.getString(R.string.input_csv_add)
            messageFront = activity.getString(R.string.input_add_message_front)
            messageRear = activity.getString(R.string.input_add_message_rear)
        }
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(
                messageFront + getFileNameByUri(
                    activity,
                    uri
                ) + System.getProperty("line.separator") + messageRear

            )
            .setPositiveButton(R.string.input_button) { _, _ ->
                if (importDatabase(uri)) {
                    execImportDatabase()
                } else {
                    failedImportDatabase()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * CSVファイル取込処理
     * * CSVファイルを1行ずつ読み取りエンティティデータへ変換してリストデータを作成する
     *
     * @param uri 取込元ファイルURI
     * @return 取込完了可否
     */
    @SuppressLint("SimpleDateFormat")
    private fun importDatabase(uri: Uri?): Boolean {
        var inputStream: InputStream? = null
        try {
            // 文字コードを判定し、判定できなければデフォルトをutf8とする
            val fd = FileCharDetector(activity, uri)
            var encType = fd.detect()
            if (encType == null) encType = "UTF-8"

            // 判定された文字コードを指定してファイル読み込みを行う
            inputStream = activity.contentResolver.openInputStream(uri!!)
            val reader =
                BufferedReader(InputStreamReader(Objects.requireNonNull(inputStream), encType))
            var line: String
            var isHeaderCorrect = false
            comicList = ArrayList()
            id = HEADER_RECORD
            while (reader.readLine().also { line = it ?: "" } != null) {
                val result = line.split(",".toRegex()).toTypedArray()

                // ヘッダが正しく設定されているか
                if (!isHeaderCorrect) {
                    if (result.size == COLUMN_COUNT_9 &&
                        result[0] == "TITLE" && result[1] == "TITLE_KANA" && result[2] == "AUTHOR" &&
                        result[3] == "PUBLISHER" && result[4] == "ISBN" && result[5] == "NUMBER" &&
                        result[6] == "MEMO" && result[7] == "INPUTDATE" && result[8] == "STATUS") {
                        isHeaderCorrect = true
                        continue
                    } else {
                        // ヘッダが正しくないので取込を中止する
                        return false
                    }
                }

                // 入力最大レコード数を100000件とする
                if (id > MAX_RECORD) return false

                // データ行をパースして Comic エンティティを作成
                if (isHeaderCorrect && result.size >= COLUMN_COUNT_9) {
                    try {
                        val comic = Comic(
                            id = if (isOverride) id else 0,
                            title = result[0],
                            title_kana = result[1],
                            author = result[2],
                            publisher = result[3],
                            isbn = result[4],
                            number = result[5],
                            memo = result[6].replace("  ", "\n"),
                            inputdate = result[7],
                            status = result[8].toLong()
                        )
                        comicList?.add(comic)
                    } catch (e: Exception) {
                        // パースエラーが発生した場合は取込を中止する
                        return false
                    }
                }
                id++
            }

            // ヘッダが正しく設定されていなければ取り込みを行わない
            if (!isHeaderCorrect) return false
        } catch (exc: Exception) {
            val ts = Toast.makeText(
                activity,
                activity.getString(R.string.csv_input_failed_message),
                Toast.LENGTH_SHORT
            )
            ts.show()
            return false
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    /**
     * CSVファイル取込実行処理
     * * エンティティへ変換されたデータリストをDBへ取込を実行する
     *
     */
    private fun execImportDatabase() {
        val title: String
        val message: String
        if (isOverride) {
            title = activity.getString(R.string.input_csv_override)
            message = activity.getString(R.string.csv_input_override_confirm_message)
        } else {
            title = activity.getString(R.string.input_csv_add)
            message = activity.getString(R.string.csv_input_add_confirm_message)
        }

        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.execute) { _, _ ->
                // 取込み中のプログレスダイアログを表示する
                progressDialog = ProgressDialog(activity).apply {
                    setTitle(R.string.csv_input_processing)
                    setMessage(activity.getString(R.string.csv_input_processing))
                    setCancelable(false)
                    show()
                }

                // ワーカースレッドで取込みを開始する
                val mainLooper = Looper.getMainLooper()
                val handler = HandlerCompat.createAsync(mainLooper)
                val backgroundTask = BackgroundTask(handler)
                val executorService = Executors.newSingleThreadExecutor()
                executorService.submit(backgroundTask)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * CSVファイル取込失敗ダイアログ表示処理
     *
     */
    private fun failedImportDatabase() {
        val title = if (isOverride) activity.getString(R.string.input_csv_override) else activity.getString(R.string.input_csv_add)
        if (id == HEADER_RECORD) {
            // ヘッダが正しくないエラーを表示する
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(activity.getString(R.string.csv_input_failed_header_message))
                .setPositiveButton(R.string.ok, null)
                .show()
        } else if (id > MAX_RECORD) {
            // 入力上限を超えたエラーを表示する
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(activity.getString(R.string.csv_input_failed_counts_message))
                .setPositiveButton(R.string.ok, null)
                .show()
        } else {
            // 指定行がエラーであるエラーを表示する
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(activity.getString(R.string.csv_input_failed_body_message) + id)
                .setPositiveButton(R.string.ok, null)
                .show()
        }
    }

    /**
     * URIからファイルパス名を取得する処理
     *
     * @param activity コンテキスト
     * @param uri ファイルURI
     * @return ファイルパス名
     */
    private fun getFileNameByUri(activity: Context, uri: Uri?): String {
        var fileName = ""
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val cursor = activity.contentResolver
            .query(uri!!, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                )
            }
            cursor.close()
        }
        return fileName
    }

    companion object {
        /** ヘッダーレコード行 */
        private const val HEADER_RECORD = 1L
        /** 取込最大レコード数 */
        private const val MAX_RECORD = 100000
        /** カラム数が9 */
        private const val COLUMN_COUNT_9 = 9
    }
}