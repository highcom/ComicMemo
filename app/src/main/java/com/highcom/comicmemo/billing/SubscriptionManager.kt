package com.highcom.comicmemo.billing

import android.content.Context
import android.content.SharedPreferences

/**
 * サブスクリプション（有料会員）ステータス管理クラス
 */
object SubscriptionManager {
    private const val PREFS_NAME = "subscription_prefs"
    private const val KEY_IS_PREMIUM = "is_premium"

    /**
     * 有料会員かどうかを取得
     *
     * @param context コンテキスト
     * @return 有料会員の場合true
     */
    fun isPremium(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_PREMIUM, false)
    }

    /**
     * 有料会員ステータスを設定
     *
     * @param context コンテキスト
     * @param isPremium 有料会員の場合true
     */
    fun setPremium(context: Context, isPremium: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
    }
}
