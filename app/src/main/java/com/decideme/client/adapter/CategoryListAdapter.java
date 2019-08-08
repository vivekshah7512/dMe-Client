package com.decideme.client.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;
import com.decideme.client.model.CategoryList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vivek.shah on 16-Jan-16.
 */
public class CategoryListAdapter extends BaseAdapter {

    private List<CategoryList> listSharedDevices = new ArrayList<CategoryList>();
    private List<CategoryList> listSharedDevicesOriginal;
    Context context;
    private LayoutInflater inflater = null;

    public CategoryListAdapter(Activity context, List<CategoryList> list) {
        this.context = context;
        listSharedDevices = list;
        listSharedDevicesOriginal = new ArrayList<CategoryList>(list);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listSharedDevices.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return listSharedDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {

        TextView tv_name;
        ImageView img_category;

    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        // TODO Auto-generated method stub

        final Holder holder;

        final CategoryList items = (CategoryList) getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_category_list_item, null);
            holder = new Holder();

            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_category_title);
            holder.img_category = (ImageView) convertView.findViewById(R.id.img_category);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.tv_name.setText(items.getCat_name());

        Glide.with(context).load(items.getCat_img())
                .thumbnail(0.5f)
                .apply(new RequestOptions().placeholder(R.mipmap.placeholder))
                .into(holder.img_category);

        return convertView;
    }
}
