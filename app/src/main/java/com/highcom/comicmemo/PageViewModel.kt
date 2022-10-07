package com.highcom.comicmemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 巻数一覧を制御するViewModel
 *
 */
class PageViewModel : ViewModel() {
    /** 巻数の一覧データ管理 */
    private val listDataManager: ListDataManager? = ListDataManager.instance
    /** 巻数データ一覧更新監視用のライブデータ */
    private var mListData: MutableLiveData<List<Map<String, String>>>? = null

    /**
     * 巻数一覧データ取得
     *
     * @param index 0:続刊 1:完結
     * @return 巻数一覧データ
     */
    fun getListData(index: Long): LiveData<List<Map<String, String>>>? {
        if (mListData == null) {
            mListData = MutableLiveData()
        }
        mListData!!.setValue(listDataManager!!.getDataList(index))
        return mListData
    }

    /**
     * 巻数データの設定
     *
     * @param index 0:続刊 1:完結
     * @param isEdit 編集モードかどうか
     * @param data 巻数データ
     */
    fun setData(index: Long, isEdit: Boolean, data: Map<String, String>) {
        listDataManager!!.setData(isEdit, data)
        mListData!!.value = listDataManager.getDataList(index)
    }

    /**
     * 巻数データの更新
     *
     * @param index 0:続刊 1:完結
     */
    fun updateData(index: Long) {
        mListData!!.value = listDataManager!!.getDataList(index)
    }

    /**
     * 巻数データの削除
     *
     * @param index 0:続刊 1:完結
     * @param id 対象の巻数データID
     */
    fun deleteData(index: Long, id: String?) {
        listDataManager!!.deleteData(id)
        mListData!!.value = listDataManager.getDataList(index)
    }

    /**
     * 巻数データ一覧のソート処理
     *
     * @param index 0:続刊 1:完結
     * @param key ソートキー
     */
    fun sortData(index: Long, key: String) {
        listDataManager!!.sortListData(index, key)
        mListData!!.value = listDataManager.getDataList(index)
    }

    /**
     * DBを閉じる処理
     *
     */
    fun closeData() {
        listDataManager!!.closeData()
    }

    /**
     * 巻数データ一覧の並べ替え処理
     *
     * @param dataIndex 0:続刊 1:完結
     * @param fromPos 移動元の位置
     * @param toPos 移動先の位置
     */
    fun rearrangeData(dataIndex: Long, fromPos: Int, toPos: Int) {
        listDataManager!!.rearrangeData(dataIndex, fromPos, toPos)
    }
}