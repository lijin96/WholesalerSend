package com.example.wholesalersend.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.wholesalersend.R;
import com.example.wholesalersend.entity.AccountSet;
import com.example.wholesalersend.entity.ScanOrder;

import java.util.List;

/**
 * @ClassName: ScanOrderAdapter
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2024/1/25 14:25
 */
public class ScanOrderAdapter extends BaseAdapter {
    private Context context;
    private List<ScanOrder> scanOrderList;

    public ScanOrderAdapter(Context context, List<ScanOrder> scanOrders) {
        this.context = context;
        this.scanOrderList = scanOrders;
    }


    @Override
    public int getCount() {
        return scanOrderList.size();
    }

    @Override
    public Object getItem(int i) {
        return scanOrderList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;

        if (convertView == null) {
            // 使用View的对象itemView与R.layout.item关联
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.new_list_select_scanorder, null);
            holder = new ViewHolder();
            holder.scanBillNo = (TextView) convertView.findViewById(R.id.txt_list1);
            holder.scanBillNum = (TextView) convertView.findViewById(R.id.txt_list2);
            holder.scanLastScanTime = (TextView) convertView.findViewById(R.id.txt_list3);
            convertView.setTag(holder);
        } else {
            holder = (ScanOrderAdapter.ViewHolder) convertView.getTag();
        }
        ScanOrder scanOrder = scanOrderList.get(i);

        holder.scanBillNo.setText(scanOrder.getBillNo());
        holder.scanBillNum.setText(scanOrder.getBillNum());
        holder.scanLastScanTime.setText(scanOrder.getLastScanTime());


        return convertView;
    }

    class ViewHolder
    {
        private TextView scanBillNo;//扫描单号
        private TextView scanBillNum;//扫描数量
        private TextView scanLastScanTime;//扫描时间
    }
}
