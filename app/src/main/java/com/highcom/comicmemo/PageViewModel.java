package com.highcom.comicmemo;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PageViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            return "Hello world from section: " + input;
        }
    });

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }


    private ListDataManager mManager;
    private MutableLiveData<List<Map<String, String>>> mListData;
    public LiveData<List<Map<String, String>>> getListData(Context context, long index) {
       if (mListData == null) {
           mListData = new MutableLiveData<>();
           mManager = new ListDataManager(context, index);
           mListData.setValue(mManager.getDataList());
       }

       return mListData;
    }

    public void setData(boolean isEdit, Map<String, String> data) {
        mManager.setData(isEdit, data);
        mListData.setValue(mManager.getDataList());
    }

    public void updateData() {
        mListData.setValue(mManager.getDataList());
    }

    public String getNowDate(){
        Date date = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(date);
    }
}