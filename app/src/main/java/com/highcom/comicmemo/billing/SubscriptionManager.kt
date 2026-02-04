package com.highcom.comicmemo.billing

import android.content.Context
import android.content.SharedPreferences

/**
 * サブスクリプション（有料会員）ステータス管理クラス
 */
object SubscriptionManager {
    private const val PREFS_NAME = "subscription_prefs"
    private const val KEY_IS_PREMIUM_MONTHLY = "is_premium_monthly"
    private const val KEY_IS_PREMIUM_YEARLY = "is_premium_yearly"
    private const val KEY_IS_HIDE_ADS = "is_hide_ads"

    /**
     * 広告非表示かどうかを取得
     *
     * @param context コンテキスト
     * @return 広告非表示の場合true
     */
    fun isHideAds(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_HIDE_ADS, false)
    }

    /**
     * 広告非表示ステータスを設定
     *
     * @param context コンテキスト
     * @param isHideAds 広告非表示の場合true
     */
    fun setHideAds(context: Context, isHideAds: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_HIDE_ADS, isHideAds).apply()
    }

    /**
     * 有料会員かどうかを取得
     *
     * @param context コンテキスト
     * @return 有料会員の場合true
     */
    fun isPremium(context: Context): Boolean {
        return isPremiumMonthly(context) || isPremiumYearly(context) || isHideAds(context)
    }

    /**
     * 月額有料会員かどうかを取得
     *
     * @param context コンテキスト
     * @return 月額有料会員の場合true
     */
    fun isPremiumMonthly(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_PREMIUM_MONTHLY, false)
    }

    /**
     * 年額有料会員かどうかを取得
     *
     * @param context コンテキスト
     * @return 年額有料会員の場合true
     */
    fun isPremiumYearly(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_PREMIUM_YEARLY, false)
    }

    /**
     * 月額有料会員ステータスを設定
     *
     * @param context コンテキスト
     * @param isPremium 月額有料会員の場合true
     */
    fun setPremiumMonthly(context: Context, isPremium: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_PREMIUM_MONTHLY, isPremium).apply()
    }

    /**
     * 年額有料会員ステータスを設定
     *
     * @param context コンテキスト
     * @param isPremium 年額有料会員の場合true
     */
    fun setPremiumYearly(context: Context, isPremium: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_PREMIUM_YEARLY, isPremium).apply()
    }
}
