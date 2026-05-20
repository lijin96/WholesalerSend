package com.example.wholesalersend.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.wholesalersend.R;

import java.util.List;
import java.util.Map;

/**
 * 未满盒盒标列表适配器
 */
public class UnFillBoxAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> dataList;

    public UnFillBoxAdapter(Context context, List<Map<String, Object>> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.item_unfill_box, null);
            holder = new ViewHolder();
            holder.txtBoxNo = convertView.findViewById(R.id.txt_list1);
            holder.txtSetNum = convertView.findViewById(R.id.txt_list2);
            holder.txtActNum = convertView.findViewById(R.id.txt_list3);
            holder.txtModelColor = convertView.findViewById(R.id.txt_list4);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Map<String, Object> item = dataList.get(i);
        holder.txtBoxNo.setText(String.valueOf(item.get("BoxNo")));
        holder.txtSetNum.setText(String.valueOf(item.get("SetNum")));
        holder.txtActNum.setText(String.valueOf(item.get("ActNum")));
        String model = String.valueOf(item.get("Modelm"));
        String colors = String.valueOf(item.get("Colors"));
        holder.txtModelColor.setText(model + " / " + colors);
        return convertView;
    }

    static class ViewHolder {
        TextView txtBoxNo;
        TextView txtSetNum;
        TextView txtActNum;
        TextView txtModelColor;
    }
}
