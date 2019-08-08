package com.decideme.client.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;
import com.decideme.client.model.ChatList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vivek.shah on 16-Jan-16.
 */
public class ChatListAdapter extends BaseAdapter {

    private List<ChatList> listSharedDevices = new ArrayList<ChatList>();
    private List<ChatList> listSharedDevicesOriginal;
    Context context;
    private LayoutInflater inflater = null;
    private boolean date_flag = false;

    public ChatListAdapter(Activity context, List<ChatList> list) {
        this.context = context;
        listSharedDevices = list;
        listSharedDevicesOriginal = new ArrayList<ChatList>(list);
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

        LinearLayout ll_to, ll_from;
        TextView tv_message_from, tv_date_from, tv_message_to, tv_date_to, tv_main_date;
        ImageView img_form, img_to;
        RelativeLayout rl_chat_date;

    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        // TODO Auto-generated method stub

        final Holder holder;

        final ChatList items = (ChatList) getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_chat_list_item, null);
            holder = new Holder();

            holder.ll_from = (LinearLayout) convertView.findViewById(R.id.ll_chat_from);
            holder.ll_to = (LinearLayout) convertView.findViewById(R.id.ll_chat_to);
            holder.rl_chat_date = (RelativeLayout) convertView.findViewById(R.id.rl_chat_date);
            holder.tv_message_from = (TextView) convertView.findViewById(R.id.tv_chat_from_message);
            holder.tv_date_from = (TextView) convertView.findViewById(R.id.tv_chat_from_date);
            holder.tv_message_to = (TextView) convertView.findViewById(R.id.tv_chat_to_message);
            holder.tv_date_to = (TextView) convertView.findViewById(R.id.tv_chat_to_date);
            holder.tv_main_date = (TextView) convertView.findViewById(R.id.tv_date);
            holder.img_form = (ImageView) convertView.findViewById(R.id.img_chat_from);
            holder.img_to = (ImageView) convertView.findViewById(R.id.img_chat_to);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        if (!date_flag) {
            date_flag = true;
            holder.rl_chat_date.setVisibility(View.VISIBLE);
            holder.tv_main_date.setText(parseDateToddMMyyyy(listSharedDevices.get(position).getDate()));
        } else {
            if (position != 0) {
                if (listSharedDevices.get(position - 1).getDate().equalsIgnoreCase(listSharedDevices.get(position).getDate())) {
                    holder.rl_chat_date.setVisibility(View.GONE);
                } else {
                    holder.rl_chat_date.setVisibility(View.VISIBLE);
                    holder.tv_main_date.setText(parseDateToddMMyyyy(listSharedDevices.get(position).getDate()));
                }
            }
        }

        if (items.getUser_type().equalsIgnoreCase("recruit")) {
            holder.ll_from.setVisibility(View.VISIBLE);
            holder.ll_to.setVisibility(View.GONE);
            holder.tv_message_from.setText(items.getMessage());
            holder.tv_date_from.setText(parseTime(items.getTime().toString()));
            Glide.with(context).load(items.getProfile_pic())
                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)                            .error(R.mipmap.user))
                    .thumbnail(0.5f)
                    .into(holder.img_form);
        } else if (items.getUser_type().equalsIgnoreCase("client")) {
            holder.ll_from.setVisibility(View.GONE);
            holder.ll_to.setVisibility(View.VISIBLE);
            holder.tv_message_to.setText(items.getMessage());
            holder.tv_date_to.setText(parseTime(items.getTime().toString()));
            Glide.with(context).load(items.getProfile_pic())
                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)                            .error(R.mipmap.user))
                    .thumbnail(0.5f)
                    .into(holder.img_to);
        }

        return convertView;
    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "MM/dd/yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public String parseTime(String time) {
        String inputPattern = "HH:mm:ss";
        String outputPattern = "hh:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
}