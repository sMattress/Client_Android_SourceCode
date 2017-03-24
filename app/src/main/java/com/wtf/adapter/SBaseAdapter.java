package com.wtf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter抽象基类
 * Created by Hailey on 2016/3/24.
 */
public abstract class SBaseAdapter<T> extends BaseAdapter {
    protected Context context;
    protected LayoutInflater inflater;
    protected List<T> itemList = new ArrayList<>();

    public SBaseAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    /**
     * 判断数据是否为空
     *
     * @return 为空返回true，不为空返回false
     */
    public boolean isEmpty() {
        return itemList.isEmpty();
    }

    /**
     * 在原有的数据上添加新数据
     *
     * @param itemList
     */
    public void addItems(List<T> itemList) {
        this.itemList.addAll(itemList);
        notifyDataSetChanged();
    }

    /**
     * 设置数据
     *
     * @param itemList
     */
    public void setItems(List<T> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    /**
     * 清空数据
     */
    public void clearItems() {
        itemList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (itemList != null ? itemList.size() : 0);
    }

    @Override
    public Object getItem(int pos) {
        return (itemList != null ? itemList.get(pos) : null);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    abstract public View getView(int pos, View view, ViewGroup viewGroup);
}
