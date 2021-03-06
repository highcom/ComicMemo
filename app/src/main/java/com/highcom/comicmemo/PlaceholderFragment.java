package com.highcom.comicmemo;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements ListViewAdapter.AdapterListener, SimpleCallbackHelper.SimpleCallbackListener {

    private static final String ARG_SECTION_NUMBER = "section_number";

    PageViewModel pageViewModel;

    private RecyclerView recyclerView;
    private ListViewAdapter adapter;
    private SimpleCallbackHelper simpleCallbackHelper;
    private String searchViewWord = "";
    private int index = 0;

    private List<Map<String, String>> mListData;

    public int getIndex() {
        return index;
    }

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        mListData = pageViewModel.getListData(index).getValue();
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comic_memo, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new ListViewAdapter(
                getContext(),
                mListData,
                R.layout.row,
                new String[] { "title", "comment" },
                new int[] { android.R.id.text1,
                        android.R.id.text2 },
                this);

        recyclerView = (RecyclerView) view.findViewById(R.id.comicListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        // セル間に区切り線を実装する
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        final float scale = getResources().getDisplayMetrics().density;
        // ドラックアンドドロップの操作を実装する
        simpleCallbackHelper = new SimpleCallbackHelper(getContext(), recyclerView, scale, this) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        "削除",
                        0,
                        Color.parseColor("#FF3C30"),
                        (ListViewAdapter.ViewHolder) viewHolder,
                        new SimpleCallbackHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(ListViewAdapter.ViewHolder holder, int pos) {
                                ListDataManager.getInstance().setLastUpdateId(0);
                                // データベースから削除する
                                pageViewModel.deleteData(index, holder.id.toString());
                                // フィルタしている場合はフィルタデータの一覧も更新する
                                setSearchWordFilter(searchViewWord);
                            }
                        }
                ));
                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        "編集",
                        0,
                        Color.parseColor("#C7C7CB"),
                        (ListViewAdapter.ViewHolder) viewHolder,
                        new SimpleCallbackHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(ListViewAdapter.ViewHolder holder, int pos) {
                                ListDataManager.getInstance().setLastUpdateId(0);
                                // 入力画面を生成
                                Intent intent = new Intent(getContext(), InputMemo.class);
                                // 選択アイテムを設定
                                intent.putExtra("EDIT", true);
                                intent.putExtra("ID", holder.id.longValue());
                                intent.putExtra("TITLE", holder.title.getText().toString());
                                intent.putExtra("AUTHOR", holder.author.getText().toString());
                                intent.putExtra("NUMBER", holder.number.getText().toString());
                                intent.putExtra("MEMO", holder.memo.getText().toString());
                                intent.putExtra("STATUS", holder.status.longValue());
                                startActivityForResult(intent, 1001);
                            }
                        }
                ));
            }
        };

        pageViewModel.getListData(index).observe(getViewLifecycleOwner(), new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(@Nullable List<Map<String, String>> map) {
                mListData = map;
                setSearchWordFilter(searchViewWord);
            }
        });
    }

    public void updateData() {
        pageViewModel.updateData(index);
    }

    public void setSearchWordFilter(String word) {
        searchViewWord = word;
        Filter filter = ((Filterable) recyclerView.getAdapter()).getFilter();
        if (TextUtils.isEmpty(searchViewWord)) {
            filter.filter(null);
        } else {
            filter.filter(searchViewWord);
        }
    }

    public void changeEditEnable() {
        if (adapter.getEditEnable()) {
            adapter.setEditEnable(false);
            simpleCallbackHelper.setSwipeEnable(true);
        } else {
            adapter.setEditEnable(true);
            simpleCallbackHelper.setSwipeEnable(false);
        }
        recyclerView.setAdapter(adapter);
    }

    public void setEditEnable(boolean enable) {
        if (!enable && adapter.getEditEnable()) {
            adapter.setEditEnable(false);
            simpleCallbackHelper.setSwipeEnable(true);
            recyclerView.setAdapter(adapter);
        } else if (enable && !adapter.getEditEnable()) {
            adapter.setEditEnable(true);
            simpleCallbackHelper.setSwipeEnable(false);
            recyclerView.setAdapter(adapter);
        }
    }

    public void sortData(String key) {
        pageViewModel.sortData(index, key);
    }

    @Override
    public boolean onSimpleCallbackMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (adapter.getEditEnable() && TextUtils.isEmpty(searchViewWord)) {
            final int fromPos = viewHolder.getAdapterPosition();
            final int toPos = target.getAdapterPosition();
            adapter.notifyItemMoved(fromPos, toPos);
            pageViewModel.rearrangeData(index, fromPos, toPos);
            return true;
        }
        return false;
    }

    @Override
    public void clearSimpleCallbackView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        ListDataManager.getInstance().setLastUpdateId(0);
        // 項目入れ替え後にAdapterを再設定する事で＋ボタンを動作させる
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAdapterClicked(View view, int position) {
        // 編集状態でない場合は入力画面に遷移しない
        if (!adapter.getEditEnable()) {
            return;
        }
        ListDataManager.getInstance().setLastUpdateId(0);
        // 入力画面を生成
        Intent intent = new Intent(getContext(), InputMemo.class);
        // 選択アイテムを設定
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        intent.putExtra("EDIT", true);
        intent.putExtra("ID", holder.id.longValue());
        intent.putExtra("TITLE", holder.title.getText().toString());
        intent.putExtra("AUTHOR", holder.author.getText().toString());
        intent.putExtra("NUMBER", holder.number.getText().toString());
        intent.putExtra("MEMO", holder.memo.getText().toString());
        intent.putExtra("STATUS", holder.status.longValue());
        startActivityForResult(intent, 1001);
    }

    @Override
    public void onAdapterStatusSelected(View view, long status) {
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        if (holder.status.longValue() == status) return;
        ListDataManager.getInstance().setLastUpdateId(0);
        holder.status = status;
        holder.inputdate.setText(getNowDate());

        // データベースを更新する
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", holder.id.toString());
        data.put("title", holder.title.getText().toString());
        data.put("author", holder.author.getText().toString());
        data.put("number", holder.number.getText().toString());
        data.put("memo", holder.memo.getText().toString());
        data.put("inputdate", holder.inputdate.getText().toString());
        data.put("status", holder.status.toString());
        pageViewModel.setData(index, true, data);
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord);
    }

    @Override
    public void onAdapterAddBtnClicked(View view) {
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        // 巻数を+1する
        Integer num = Integer.parseInt(holder.number.getText().toString());
        // 999を上限とする
        if (num < 999) {
            num++;
            ListDataManager.getInstance().setLastUpdateId(holder.id.intValue());
        }
        holder.number.setText(num.toString());
        holder.inputdate.setText(getNowDate());

        // データベースを更新する
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", holder.id.toString());
        data.put("title", holder.title.getText().toString());
        data.put("author", holder.author.getText().toString());
        data.put("number", holder.number.getText().toString());
        data.put("memo", holder.memo.getText().toString());
        data.put("inputdate", holder.inputdate.getText().toString());
        data.put("status", holder.status.toString());
        pageViewModel.setData(index, true, data);
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord);
    }

    @Override
    public void onAdapterDelBtnClicked(View view) {
        ListDataManager.getInstance().setLastUpdateId(0);
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        // データベースから削除する
        pageViewModel.deleteData(index, holder.id.toString());
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord);
    }

    @Override
    public void onPause() {
        super.onPause();
        ListDataManager.getInstance().setLastUpdateId(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pageViewModel.closeData();
    }

    private String getNowDate(){
        Date date = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(date);
    }
}