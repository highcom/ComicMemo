package com.highcom.comicmemo;

/**
 * Created by koichi on 2015/06/28.
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ListViewAdapter extends SimpleAdapter {

    // private Context context;
    private LayoutInflater inflater;
    private List<? extends Map<String, ?>> listData;
    public static boolean delbtnEnable = false;

    public class ViewHolder {
        TextView title;
        TextView comment;
        TextView number;
        Button   deletebtn;
    }

    public ListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        // TODO 自動生成されたコンストラクター・スタブ
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
            holder.title = (TextView) view.findViewById(android.R.id.title);
            holder.comment = (TextView) view.findViewById(R.id.comment);
            holder.number = (TextView) view.findViewById(R.id.number);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String title = ((HashMap<?, ?>) listData.get(position)).get("title").toString();
        String comment = ((HashMap<?, ?>) listData.get(position)).get("comment").toString();
        String number = ((HashMap<?, ?>) listData.get(position)).get("number").toString();
        holder.title.setText(title);
        holder.comment.setText(comment);
        holder.number.setText(number);

        Button btn = (Button) view.findViewById(R.id.addbutton);
        btn.setTag(position);

        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO 自動生成されたメソッド・スタブ
                Log.v("buttonクリック", "ポジション：　" + position);
                if (holder.comment.getVisibility() == android.view.View.GONE) {
                    holder.comment.setVisibility(View.VISIBLE);
                } else
                    holder.comment.setVisibility(View.GONE);
            }
        });

        holder.deletebtn = (Button) view.findViewById(R.id.deletebutton);
        if (delbtnEnable) {
            holder.deletebtn.setVisibility(View.VISIBLE);
            // 削除ボタンを押下された行を削除する
            holder.deletebtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO 自動生成されたメソッド・スタブ
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