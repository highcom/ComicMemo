package com.highcom.comicmemo;

/**
 * Created by koichi on 2015/06/28.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> implements Filterable {

    // private Context context;
    private LayoutInflater inflater;
    private List<? extends Map<String, ?>> listData;
    private List<? extends Map<String, ?>> orig;
    private boolean editEnable = false;
    private AdapterListener adapterListener;
    private PopupWindow popupWindow;
    private View popupView;

    public interface AdapterListener {
        void onAdapterClicked(View view, int position);
        void onAdapterStatusSelected(View view, long status);
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
        ToggleButton popupContinue;
        ToggleButton popupComplete;

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
            if (editEnable) {
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

            popupWindow = new PopupWindow(itemView.getContext());

            // PopupWindowに表示するViewを生成
            final View contentView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.popupmenu, null);
            popupWindow.setContentView(contentView);
            popupContinue = (ToggleButton)contentView.findViewById(R.id.popupContinue);
            popupComplete = (ToggleButton)contentView.findViewById(R.id.popupComplete);
            popupContinue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setEnableLayoutContinue(buttonView.getResources());
                    adapterListener.onAdapterStatusSelected(popupView, 0);
                    popupWindow.dismiss();
                }
            });
            popupComplete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setEnableLayoutComplete(buttonView.getResources());
                    adapterListener.onAdapterStatusSelected(popupView, 1);
                    popupWindow.dismiss();
                }
            });

            // PopupWindowに表示するViewのサイズを設定
            float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, itemView.getContext().getResources().getDisplayMetrics());
            popupWindow.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setWidth((int) width);
            // PopupWindowの外をタッチしたらPopupWindowが閉じるように設定
            popupWindow.setOutsideTouchable(true);
            // PopupWindow外のUIのタッチイベントが走らないようにフォーカスを持っておく
            popupWindow.setFocusable(true);
            // PopupWindow内のクリックを可能にしておく
            popupWindow.setTouchable(true);
            // レイアウトファイルで設定した背景のさらに背景(黒とか)が生成される為、ここで好みの背景を設定しておく
            popupWindow.setBackgroundDrawable(new ColorDrawable(itemView.getContext().getResources().getColor(android.R.color.white)));
        }

        public void setEnableLayoutContinue(Resources resources) {
            popupContinue.setTextColor(resources.getColor(R.color.white));
            popupContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.toggle_select_button));
            popupComplete.setTextColor(resources.getColor(R.color.blue));
            popupComplete.setBackgroundDrawable(resources.getDrawable(R.drawable.toggle_unselect_button));
        }

        public void setEnableLayoutComplete(Resources resources) {
            popupContinue.setTextColor(resources.getColor(R.color.blue));
            popupContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.toggle_unselect_button));
            popupComplete.setTextColor(resources.getColor(R.color.white));
            popupComplete.setBackgroundDrawable(resources.getDrawable(R.drawable.toggle_select_button));
        }
    }

    public ListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, AdapterListener listener) {
        inflater = LayoutInflater.from(context);
        this.listData = data;
        this.adapterListener = listener;
    }

    public void setEditEnable(boolean enable) {
        editEnable = enable;
    }

    public boolean getEditEnable() {
        return editEnable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
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
        if (holder.id.intValue() == ListDataManager.getInstance().getLastUpdateId()) {
            holder.number.setTextColor(Color.RED);
        } else {
            holder.number.setTextColor(Color.GRAY);
        }
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
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (editEnable) return true;
                // PopupWindowの実装をする　続刊と完結を選択できるようにする
                popupWindow.showAsDropDown(view, view.getWidth(), -view.getHeight());
                // PopupWindowで選択したViewに対して更新できるようにViewを保持する
                popupView = view;
                return true;
            }
        });

        if (holder.status.longValue() == 0) {
            holder.setEnableLayoutContinue(holder.itemView.getResources());
        } else {
            holder.setEnableLayoutComplete(holder.itemView.getResources());
        }
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