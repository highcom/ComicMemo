package com.highcom.comicmemo;

/**
 * Created by koichi on 2015/06/28.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ListViewAdapter extends SimpleAdapter implements Filterable {

    // private Context context;
    private LayoutInflater inflater;
    private List<? extends Map<String, ?>> listData;
    private List<? extends Map<String, ?>> orig;
    public static boolean delbtnEnable = false;
    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onAdapterAddBtnClicked(ViewHolder holder);
        void onAdapterDelBtnClicked(ViewHolder holder);
    }

    public class ViewHolder {
        Long  id;
        TextView title;
        TextView number;
        TextView memo;
        TextView inputdate;
        Button   deletebtn;
    }

    public ListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, AdapterListener listener) {
        super(context, data, resource, from, to);
        // this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listData = data;
        this.adapterListener = listener;
    }

    @Override
    public int getCount() {
        if (listData != null) {
            return listData.size();
        } else {
            return 0;
        }
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Map<String, ?>> results = new ArrayList<Map<String, ?>>();
                if (orig == null)
                    orig = listData;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final Map<String, ?> g : orig) {
                            if (g.get("title").toString().toLowerCase().contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                } else {
                    oReturn.values = orig;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                listData = (ArrayList<Map<String, String>>) results.values;
                notifyDataSetChanged();
            }
        };
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

        String id = ((HashMap<?, ?>) listData.get(position)).get("id").toString();
        String title = ((HashMap<?, ?>) listData.get(position)).get("title").toString();
        String number = ((HashMap<?, ?>) listData.get(position)).get("number").toString();
        String memo = ((HashMap<?, ?>) listData.get(position)).get("memo").toString();
        String inputdate = ((HashMap<?, ?>) listData.get(position)).get("inputdate").toString();
        holder.id = new Long(id);
        holder.title.setText(title);
        holder.number.setText(number);
        holder.number.setTextColor(Color.GRAY);
        holder.memo.setText(memo);
        holder.inputdate.setText(inputdate);

        Button addbtn = (Button) view.findViewById(R.id.addbutton);
        addbtn.setTag(position);

        // カウント追加ボタン処理
        addbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                adapterListener.onAdapterAddBtnClicked(holder);
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
                    adapterListener.onAdapterDelBtnClicked(holder);
                }
            });
        } else {
            holder.deletebtn.setVisibility(View.GONE);
        }

        return view;
    }
}