package com.highcom.comicmemo;

/**
 * Created by koichi on 2015/06/28.
 */

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewAdapter extends SimpleAdapter {

    // private Context context;
    private LayoutInflater inflater;
    private List<? extends Map<String, ?>> listData;
    public static boolean delbtnEnable = false;

    public class ViewHolder {
        Long  id;
        TextView title;
        TextView number;
        TextView memo;
        TextView inputdate;
        Button   deletebtn;
    }

    public ListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        // this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listData = data;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        // ビューを受け取る
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.row, parent, false);
            // LayoutInflater inflater = (LayoutInflater)
            // context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // view = inflater.inflate(R.layout.raw, null);

            holder = new ViewHolder();
            holder.id = new Long(position);
            holder.title = (TextView) view.findViewById(android.R.id.title);
            holder.number = (TextView) view.findViewById(R.id.number);
            holder.memo = (TextView) view.findViewById(R.id.memo);
            holder.inputdate = (TextView) view.findViewById(R.id.inputdate);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String title = ((HashMap<?, ?>) listData.get(position)).get("title").toString();
        String number = ((HashMap<?, ?>) listData.get(position)).get("number").toString();
        String memo = ((HashMap<?, ?>) listData.get(position)).get("memo").toString();
        String inputdate = ((HashMap<?, ?>) listData.get(position)).get("inputdate").toString();
        holder.id = new Long(position);
        holder.title.setText(title);
        holder.number.setText(number);
        holder.memo.setText(memo);
        holder.inputdate.setText(inputdate);

        Button btn = (Button) view.findViewById(R.id.addbutton);
        btn.setTag(position);

        // カウント追加ボタン処理
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 巻数を+1する
                Integer num = Integer.parseInt(holder.number.getText().toString());
                // 999を上限とする
                if (num < 999) {
                    num++;
                }
                holder.number.setText(num.toString());
                // データベースを更新する
                ContentValues updateValues = new ContentValues();
                updateValues.put("number", num);
                ComicMemo.wdb.update("comicdata", updateValues, "id=?", new String[] { holder.id.toString() });
            }
        });

        // 削除ボタン処理
        holder.deletebtn = (Button) view.findViewById(R.id.deletebutton);
        if (delbtnEnable) {
            holder.deletebtn.setVisibility(View.VISIBLE);
            // 削除ボタンを押下された行を削除する
            holder.deletebtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // データベースから削除する
                    ComicMemo.wdb.delete("comicdata", "id=?", new String[] { holder.id.toString() });

                    // 行から削除する
                    ComicMemo.dataList.remove(position);
                    ComicMemo.listView.setAdapter(ComicMemo.adapter);
                }
            });
        } else {
            holder.deletebtn.setVisibility(View.GONE);
        }

        return view;
    }
}