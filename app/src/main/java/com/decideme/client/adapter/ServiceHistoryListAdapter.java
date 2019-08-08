package com.decideme.client.adapter;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by vivek.shah on 16-Jan-16.
 */
public class ServiceHistoryListAdapter extends BaseAdapter {

    Context context;
    private LayoutInflater inflater = null;
    private String[] transaction_id, worker_name, worker_image, worker_cat, status, worker_payment,
            payment_mode, payment_status, date_time, worker_address, user_address, rate, rating;

    public ServiceHistoryListAdapter(Activity context, String[] transaction_id,
                                     String[] worker_name, String[] worker_image, String[] worker_cat,
                                     String[] status, String[] worker_payment, String[] payment_mode,
                                     String[] payment_status, String[] date_time, String[] worker_address,
                                     String[] user_address,String[] rate, String[] rating) {
        this.context = context;
        this.transaction_id = transaction_id;
        this.worker_name = worker_name;
        this.worker_image = worker_image;
        this.worker_cat = worker_cat;
        this.status = status;
        this.worker_payment = worker_payment;
        this.payment_mode = payment_mode;
        this.payment_status = payment_status;
        this.date_time = date_time;
        this.worker_address = worker_address;
        this.user_address = user_address;
        this.rate = rate;
        this.rating = rating;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return worker_name.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return worker_name[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {

        ImageView img_job_type, img_profile;
        TextView tv_name, tv_job_type, tv_total_rate, tv_date_time, tv_total_hours, tv_source_address,
                tv_destination_address, tv_total;
        RatingBar ratingBar;
        LinearLayout ll_total_time;

    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        // TODO Auto-generated method stub

        final Holder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_service_history_list_item, null);
            holder = new Holder();

            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_history_worker_name);
            holder.tv_job_type = (TextView) convertView.findViewById(R.id.tv_history_worker_cat);
            holder.tv_total_rate = (TextView) convertView.findViewById(R.id.tv_history_rating_total);
            holder.tv_date_time = (TextView) convertView.findViewById(R.id.tv_history_date_time);
            holder.tv_total_hours = (TextView) convertView.findViewById(R.id.tv_history_total_hours);
            holder.tv_source_address = (TextView) convertView.findViewById(R.id.tv_history_source);
            holder.tv_destination_address = (TextView) convertView.findViewById(R.id.tv_history_destination);
            holder.tv_total = (TextView) convertView.findViewById(R.id.tv_history_total);
            holder.ll_total_time = (LinearLayout) convertView.findViewById(R.id.ll_total_time);

            holder.img_job_type = (ImageView) convertView.findViewById(R.id.img_history_type);
            holder.img_profile = (ImageView) convertView.findViewById(R.id.img_history_profile);

            holder.ratingBar = (RatingBar) convertView.findViewById(R.id.rating_history);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.tv_name.setText(worker_name[position]);
        holder.tv_job_type.setText(worker_cat[position]);
        holder.tv_total_rate.setText("("+rate[position]+")");
        holder.tv_date_time.setText(date_time[position]);
        holder.tv_total_hours.setText("");

        // Source location
        if (user_address[position].equalsIgnoreCase("") ||
                user_address[position].equalsIgnoreCase("Not found"))
            holder.tv_source_address.setText("Location not found");
        else
            holder.tv_source_address.setText(user_address[position]);

        // Destination location
        if (worker_address[position].equalsIgnoreCase("") ||
                worker_address[position].equalsIgnoreCase("Not found"))
            holder.tv_destination_address.setText("Location not found");
        else
            holder.tv_destination_address.setText(worker_address[position]);

        holder.tv_total.setText("");
        holder.ratingBar.setRating(Float.parseFloat(rating[position]));
        holder.ll_total_time.setVisibility(View.GONE);

        Glide.with(context).load(worker_image[position])
                .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)                            .error(R.mipmap.user))
                .thumbnail(0.5f)
                .into(holder.img_profile);

        return convertView;
    }

    public String parseDateToddMMyyyy(String time) {

        //2018-01-31 05:40:03

        String inputPattern = "yyyy-MM-dd hh:mm:ss";
        String outputPattern = "MM/dd/yyyy, hh:mm";
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

    public String addressFromLatLang(String lat, String lang) {

        String address = null;

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses;
            addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lang), 1);
            if (addresses != null || addresses.size() != 0) {
                address = addresses.get(0).getAddressLine(0);
            } else {
                address = "No location found";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return address;
    }
}
