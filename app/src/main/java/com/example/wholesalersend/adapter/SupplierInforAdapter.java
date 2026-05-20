package com.example.wholesalersend.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wholesalersend.R;
import com.example.wholesalersend.entity.SupplierInfor;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: SupplierInforAdapter
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2023/10/26 10:31
 */
public class SupplierInforAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<SupplierInfor> supplierInforList;

    MyClick click;

    private int normalType = 0;     // 第一种ViewType，正常的item
    private int footType = 1;       // 第二种ViewType，底部的提示View

    private boolean hasMore = true;   // 变量，是否有更多数据
    private boolean fadeTips = false; // 变量，是否隐藏了底部的提示

    public SupplierInforAdapter(List<SupplierInfor> datas, Context context, boolean hasMore) {
        // 初始化变量
        this.supplierInforList = datas;
        this.mContext = context;
        this.hasMore = hasMore;
    }

    // 获取条目数量，之所以要加1是因为增加了一条footView
    @Override
    public int getItemCount() {
        return supplierInforList.size() + 1;
    }

    // 自定义方法，获取列表中数据源的最后一个位置，比getItemCount少1，因为不计上footView
    public int getRealLastPosition() {
        return supplierInforList.size();
    }


    // 根据条目位置返回ViewType，以供onCreateViewHolder方法内获取不同的Holder
    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return footType;
        } else {
            return normalType;
        }
    }

    // 正常item的ViewHolder，用以缓存findView操作
    class NormalHolder extends RecyclerView.ViewHolder {
        private TextView tv_id;
        private TextView tv_name;

        public NormalHolder(View itemView) {
            super(itemView);
            tv_id = (TextView) itemView.findViewById(R.id.txt_list1);
            tv_name = (TextView) itemView.findViewById(R.id.txt_list2);
        }
    }

    // // 底部footView的ViewHolder，用以缓存findView操作
    class FootHolder extends RecyclerView.ViewHolder {
        private TextView tips;

        public FootHolder(View itemView) {
            super(itemView);
            tips = (TextView) itemView.findViewById(R.id.foot_tv);

        }
    }

    public void setClick(MyClick click){
        this.click = click;
    }

    public interface MyClick{
        void click(View v);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 根据返回的ViewType，绑定不同的布局文件，这里只有两种
        if (viewType == normalType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.new_list_select_supplier, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(click != null){
                        click.click(v);
                    }
                }
            });
            return new NormalHolder(view);
        } else {
            return new FootHolder(LayoutInflater.from(mContext).inflate(R.layout.foot_view, parent,false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        // 如果是正常的imte，直接设置TextView的值
        if (holder instanceof NormalHolder) {
            ((NormalHolder) holder).tv_id.setText(supplierInforList.get(position).getSupplierId().toString());
            ((NormalHolder) holder).tv_name.setText(supplierInforList.get(position).getSupplierName().toString());


        } else {
            if(getItemCount() < 50){
                ((FootHolder) holder).tips.setVisibility(View.GONE);
            }else {
                // 之所以要设置可见，是因为我在没有更多数据时会隐藏了这个footView
                ((FootHolder) holder).tips.setVisibility(View.VISIBLE);
               // 只有获取数据为空时，hasMore为false，所以当我们拉到底部时基本都会首先显示“正在加载更多...”
                if (hasMore == true) {
                    // 不隐藏footView提示
                    fadeTips = false;
                    if (supplierInforList.size() > 0) {
                        // 如果查询数据发现增加之后，就显示正在加载更多
                        ((FootHolder) holder).tips.setText("正在加载更多...");
                    }
                } else {
                    if (supplierInforList.size() > 0) {
                        // 如果查询数据发现并没有增加时，就显示没有更多数据了
                        ((FootHolder) holder).tips.setText("没有更多数据了");

                        // 将fadeTips设置true
                        fadeTips = true;
                        // hasMore设为true是为了让再次拉到底时，会先显示正在加载更多
                        hasMore = true;
                    }
                }
            }
        }
    }



    // 暴露接口，改变fadeTips的方法
    public boolean isFadeTips() {
        return fadeTips;
    }

    // 暴露接口，下拉刷新时，通过暴露方法将数据源置为空
    public void resetDatas() {
        supplierInforList = new ArrayList<SupplierInfor>();
    }

    // 暴露接口，更新数据源，并修改hasMore的值，如果有增加数据，hasMore为true，否则为false
    public void updateList(List<SupplierInfor> newDatas, boolean hasMore) {
        // 在原有的数据之上增加新数据
        if (newDatas != null) {
            supplierInforList.addAll(newDatas);
        }
        this.hasMore = hasMore;
        notifyDataSetChanged();
    }

    /**
     * 获取List列表数据
     * @return
     */
    public List<SupplierInfor> getDataList() {
        return supplierInforList;
    }
}


