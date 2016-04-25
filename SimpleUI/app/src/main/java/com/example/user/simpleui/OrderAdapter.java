package com.example.user.simpleui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by user on 2016/4/25.
 */
public class OrderAdapter extends BaseAdapter{
    ArrayList<Order> orders;
    LayoutInflater inflater;

    public OrderAdapter(Context context, ArrayList<Order> orders){
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

        if(convertView == null){
            convertView = inflater.inflate(R.layout.listview_item , null);
        }
        TextView drinkName = (TextView)convertView.findViewById(R.id.drinkName);
        TextView note = (TextView)convertView.findViewById(R.id.note);

        drinkName.setText(orders.get(position).drinkName);
        note.setText(orders.get(position).note);

        return convertView;
    }
}
