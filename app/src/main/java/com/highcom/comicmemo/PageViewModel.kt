package com.highcom.comicmemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PageViewModel : ViewModel() {
    private val listDataManager: ListDataManager? = ListDataManager.Companion.getInstance()
    private var mListData: MutableLiveData<List<Map<String?, String?>?>?>? = null
    fun getListData(index: Long): LiveData<List<Map<String?, String?>?>?> {
        if (mListData == null) {
            mListData = MutableLiveData()
        }
        mListData!!.setValue(listDataManager!!.getDataList(index))
        return mListData
    }

    fun setData(index: Long, isEdit: Boolean, data: Map<String, String>) {
        listDataManager!!.setData(isEdit, data)
        mListData!!.value = listDataManager.getDataList(index)
    }

    fun updateData(index: Long) {
        mListData!!.value = listDataManager!!.getDataList(index)
    }

    fun deleteData(index: Long, id: String?) {
        listDataManager!!.deleteData(id)
        mListData!!.value = listDataManager.getDataList(index)
    }

    fun sortData(index: Long, key: String) {
        listDataManager!!.sortListData(index, key)
        mListData!!.value = listDataManager.getDataList(index)
    }

    fun closeData() {
        listDataManager!!.closeData()
    }

    fun rearrangeData(dataIndex: Long, fromPos: Int, toPos: Int) {
        listDataManager!!.rearrangeData(dataIndex, fromPos, toPos)
    }
}