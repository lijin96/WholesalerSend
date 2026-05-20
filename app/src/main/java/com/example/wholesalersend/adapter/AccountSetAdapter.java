package com.example.wholesalersend.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.wholesalersend.R;
import com.example.wholesalersend.entity.AccountSet;

import java.util.List;

/**
 * @ClassName: AccountSetAdapter
 * @Description: 账套适配类
 * @Author: lijin
 * @Date: 2023/10/24 11:09
 */
public class AccountSetAdapter extends BaseAdapter {
    private Context context;
    private List<AccountSet> accountSets;

    public AccountSetAdapter(Context context, List<AccountSet> accountSets) {
        this.context = context;
        this.accountSets = accountSets;
    }

    @Override
    public int getCount() {
        return accountSets.size();
    }

    @Override
    public Object getItem(int i) {
        return accountSets.get(i);
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
            convertView = mInflater.inflate(R.layout.accountset_item, null);
            holder = new ViewHolder();
            holder.accountSetName = (TextView) convertView.findViewById(R.id.tv_accountset_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AccountSet accountSet = accountSets.get(i);

        holder.accountSetName.setText(accountSet.getAccountSetName());

        return convertView;
    }

    class ViewHolder
    {
        private TextView accountSetName;//账套名称
    }
}
