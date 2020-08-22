package com.highcom.comicmemo;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Map;

public class PageViewModel extends ViewModel {

    private ListDataManager listDataManager = ListDataManager.getInstance();
    private MutableLiveData<List<Map<String, String>>> mListData;
    public LiveData<List<Map<String, String>>> getListData(long index) {
        if (mListData == null) {
            mListData = new MutableLiveData<>();
        }
        mListData.setValue(listDataManager.getDataList(index));
        return mListData;
    }

    public void setData(long index, boolean isEdit, Map<String, String> data) {
        listDataManager.setData(isEdit, data);
        mListData.setValue(listDataManager.getDataList(index));
    }

    public void updateData(long index) {
        mListData.setValue(listDataManager.getDataList(index));
    }

    public void deleteData(long index, String id) {
        listDataManager.deleteData(id);
        mListData.setValue(listDataManager.getDataList(index));
    }

    public void closeData() {
        listDataManager.closeData();
    }

    public void rearrangeData(long dataIndex, int fromPos, int toPos) {
        listDataManager.rearrangeData(dataIndex, fromPos, toPos);
    }
}