package com.highcom.comicmemo.network

import java.io.Serializable

/**
 * 楽天ブックデータ
 *
 * @property Item　楽天ブックデータ詳細
 */
data class Item(
    val Item: ItemEntity
) : Serializable