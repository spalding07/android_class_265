package com.example.user.simpleui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/4/25.
 */
public class OrderAdapter extends BaseAdapter {
    List<Order> orders;
    LayoutInflater inflater;

    public OrderAdapter(Context context, List<Order> orders) {
        this.inflater = LayoutInflater.from(context);
        this.orders = orders;
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_item, null);
            holder = new Holder();

            holder.drinkName = (TextView) convertView.findViewById(R.id.drinkName);
            holder.note = (TextView) convertView.findViewById(R.id.note);
            holder.storeInfo = (TextView) convertView.findViewById(R.id.store);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.drinkName.setText(orders.get(position).getDrinkName());
        holder.note.setText(orders.get(position).getNote());
        holder.storeInfo.setText(orders.get(position).getStoreInfo());

        return convertView;
    }

    class Holder {
        TextView drinkName;
        TextView note;
        TextView storeInfo;
    }
}
