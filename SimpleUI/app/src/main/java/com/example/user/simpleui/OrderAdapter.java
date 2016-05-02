package com.example.user.simpleui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

            holder.drinkNumber = (TextView) convertView.findViewById(R.id.drinkNumber);
            holder.note = (TextView) convertView.findViewById(R.id.note);
            holder.storeInfo = (TextView) convertView.findViewById(R.id.store);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        int total = 0;
        try {
            JSONArray jsonArray = new JSONArray(orders.get(position).getMenuResults());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject menu = jsonArray.getJSONObject(i);
                total += menu.getInt("m");
                total += menu.getInt("l");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder.drinkNumber.setText(String.valueOf(total));
        holder.note.setText(orders.get(position).getNote());
        holder.storeInfo.setText(orders.get(position).getStoreInfo());

        return convertView;
    }

    class Holder {
        TextView drinkNumber;
        TextView note;
        TextView storeInfo;
    }
}
