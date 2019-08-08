package com.decideme.client.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.decideme.client.R;

/**
 * Created by vivek.shah on 16-Jan-16.
 */
public class Coupon1ListAdapter extends BaseAdapter {

    private String[] code, description,date;
    Context context;
    private LayoutInflater inflater = null;

    public Coupon1ListAdapter(Activity context, String[] code, String[] description,String[] date) {
        this.context = context;
        this.code = code;
        this.description = description;
        this.date = date;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return code.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return code[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {

        TextView tv_code, tv_description, tv_date;

    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        // TODO Auto-generated method stub

        final Holder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_coupon1_list_item, null);
            holder = new Holder();

            holder.tv_code = (TextView) convertView.findViewById(R.id.tv_coupon_code);
            holder.tv_description = (TextView) convertView.findViewById(R.id.tv_coupon_description);
            holder.tv_date = (TextView) convertView.findViewById(R.id.tv_coupon_date);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.tv_code.setText(code[position]);
        holder.tv_date.setText(date[position]);
        holder.tv_description.setText(description[position]);

        return convertView;
    }

}
