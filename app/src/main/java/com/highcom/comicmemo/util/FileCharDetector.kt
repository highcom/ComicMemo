package com.highcom.comicmemo.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import org.mozilla.universalchardet.UniversalDetector
import java.io.IOException

/**
 * ファイルの文字コード判定クラス
 *
 * @property mContext コンテキスト
 * @property mUri ファイルのURI
 */
class FileCharDetector(private val mContext: Context, private val mUri: Uri?) {
    /**
     * 文字コード検出処理
     *
     * @return 文字コード
     */
    @SuppressLint("Recycle")
    @Throws(IOException::class)
    fun detect(): String? {
        val buf = ByteArray(4096)
        val inputStream = mContext.contentResolver.openInputStream(mUri!!)

        // 文字コード判定ライブラリの実装
        val detector = UniversalDetector(null)

        // 判定開始
        var nread: Int
        while (inputStream!!.read(buf).also { nread = it } > 0 && !detector.isDone) {
            detector.handleData(buf, 0, nread)
        }
        // 判定終了
        detector.dataEnd()

        // 文字コード判定
        val encType = detector.detectedCharset
        if (encType != null) {
            Log.d("DETECTOR", "文字コード = $encType")
        } else {
            Log.d("DETECTOR", "文字コードを判定できませんでした")
        }

        // 判定の初期化
        detector.reset()
        return encType
    }
}
