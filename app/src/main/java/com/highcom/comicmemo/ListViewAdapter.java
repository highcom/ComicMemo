package com.highcom.comicmemo;

/**
 * Created by koichi on 2015/06/28.
 */

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> implements Filterable {

    // private Context context;
    private LayoutInflater inflater;
    private List<? extends Map<String, ?>> listData;
    private List<? extends Map<String, ?>> orig;
    private boolean delbtnEnable = false;
    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onAdapterClicked(View view, int position);
        void onAdapterAddBtnClicked(View view);
        void onAdapterDelBtnClicked(View view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Long  id;
        TextView title;
        TextView author;
        TextView number;
        TextView memo;
        TextView inputdate;
        Long status;
        Button addbtn;
        Button deletebtn;
        ImageButton rearrangebtn;

        public ViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            author = (TextView) itemView.findViewById(R.id.author);
            number = (TextView) itemView.findViewById(R.id.number);
            memo = (TextView) itemView.findViewById(R.id.memo);
            inputdate = (TextView) itemView.findViewById(R.id.inputdate);

            addbtn = (Button) itemView.findViewById(R.id.addbutton);

            // カウント追加ボタン処理
            addbtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    adapterListener.onAdapterAddBtnClicked(itemView);
                }
            });

            // 削除ボタン処理
            deletebtn = (Button) itemView.findViewById(R.id.deletebutton);
            rearrangebtn = (ImageButton) itemView.findViewById(R.id.rearrangebutton);
            if (delbtnEnable) {
                deletebtn.setVisibility(View.VISIBLE);
                rearrangebtn.setVisibility(View.VISIBLE);
                // 削除ボタンを押下された行を削除する
                deletebtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        adapterListener.onAdapterDelBtnClicked(itemView);
                    }
                });
            } else {
                deletebtn.setVisibility(View.GONE);
                rearrangebtn.setVisibility(View.GONE);
            }

        }
    }

    public ListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, AdapterListener listener) {
        inflater = LayoutInflater.from(context);
        this.listData = data;
        this.adapterListener = listener;
    }

    public void setDelbtnEnable(boolean enable) {
        delbtnEnable = enable;
    }

    public boolean getDelbtnEnable() {
        return delbtnEnable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String id = ((HashMap<?, ?>) listData.get(position)).get("id").toString();
        String title = ((HashMap<?, ?>) listData.get(position)).get("title").toString();
        String author = ((HashMap<?, ?>) listData.get(position)).get("author").toString();
        String number = ((HashMap<?, ?>) listData.get(position)).get("number").toString();
        String memo = ((HashMap<?, ?>) listData.get(position)).get("memo").toString();
        String inputdate = ((HashMap<?, ?>) listData.get(position)).get("inputdate").toString();
        String status = ((HashMap<?, ?>) listData.get(position)).get("status").toString();
        holder.id = new Long(id);
        holder.title.setText(title);
        holder.author.setText(author);
        holder.number.setText(number);
        holder.number.setTextColor(Color.GRAY);
        holder.memo.setText(memo);
        holder.inputdate.setText(inputdate);
        holder.status = new Long(status);

        holder.itemView.setTag(holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterListener.onAdapterClicked(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
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
}