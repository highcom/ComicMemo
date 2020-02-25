package com.highcom.comicmemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;

import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements ListViewAdapter.AdapterListener {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    private ListDataManager manager;
    private RecyclerView recyclerView;
    private ListViewAdapter adapter;
    private String searchViewWord;

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
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
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
        manager = ListDataManager.createInstance(getContext());

        adapter = new ListViewAdapter(
                getContext(),
                manager.getDataList(),
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

        // ドラックアンドドロップの操作を実装する
        ItemTouchHelper itemDecor = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.ACTION_STATE_IDLE) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        if (adapter.getDelbtnEnable() && TextUtils.isEmpty(searchViewWord)) {
                            final int fromPos = viewHolder.getAdapterPosition();
                            final int toPos = target.getAdapterPosition();
                            adapter.notifyItemMoved(fromPos, toPos);
                            manager.rearrangeData(fromPos, toPos);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    }
                });
        itemDecor.attachToRecyclerView(recyclerView);

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

    public void changeDelbtnEnable() {
        if (adapter.getDelbtnEnable()) {
            adapter.setDelbtnEnable(false);
        } else {
            adapter.setDelbtnEnable(true);
        }
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAdapterClicked(View view, int position) {
        // 編集状態でない場合は入力画面に遷移しない
        if (!adapter.getDelbtnEnable()) {
            return;
        }
        // 入力画面を生成
        Intent intent = new Intent(getContext(), InputMemo.class);
        // 選択アイテムを設定
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        intent.putExtra("EDIT", true);
        intent.putExtra("ID", holder.id.longValue());
        intent.putExtra("TITLE", holder.title.getText().toString());
        intent.putExtra("NUMBER", holder.number.getText().toString());
        intent.putExtra("MEMO", holder.memo.getText().toString());
        startActivityForResult(intent, 1001);
    }

    @Override
    public void onAdapterAddBtnClicked(View view) {
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        // 巻数を+1する
        Integer num = Integer.parseInt(holder.number.getText().toString());
        // 999を上限とする
        if (num < 999) {
            num++;
            holder.number.setTextColor(Color.RED);
        }
        holder.number.setText(num.toString());
        holder.inputdate.setText(manager.getNowDate());

        // データベースを更新する
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", holder.id.toString());
        data.put("title", holder.title.getText().toString());
        data.put("number", holder.number.getText().toString());
        data.put("memo", holder.memo.getText().toString());
        data.put("inputdate", holder.inputdate.getText().toString());
        manager.setData(true, data);
    }

    @Override
    public void onAdapterDelBtnClicked(View view) {
        ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
        // データベースから削除する
        manager.deleteData(holder.id.toString());
        // adapterにデータが更新された事を通知する
        adapter.notifyDataSetChanged();
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1001) {
            return;
        }

        // adapterにデータが更新された事を通知する
        adapter.notifyDataSetChanged();
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord);
    }
}