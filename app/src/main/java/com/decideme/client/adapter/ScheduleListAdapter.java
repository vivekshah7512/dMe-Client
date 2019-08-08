package com.decideme.client.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;
import com.decideme.client.activities.ChatActivity;
import com.decideme.client.activities.InvoiceActivity;
import com.decideme.client.activities.ScheduleMapsActivity;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by vivek.shah on 16-Jan-16.
 */
public class ScheduleListAdapter extends BaseAdapter {

    private String[] worker_id, worker_name, worker_image, worker_rating, worker_status, worker_timer, service_id,
            worker_lat, worker_long, current_flag, notification_id, timer, mobile;
    Context context;
    private LayoutInflater inflater = null;
    NotificationManager notificationManager;
    private RefreshActivity listener;

    public ScheduleListAdapter(Activity context, String[] worker_id, String[] worker_name, String[] worker_image,
                               String[] worker_rating, String[] worker_status, String[] worker_timer,
                               String[] service_id, String[] worker_lat, String[] worker_long,
                               String[] current_flag, String[] notification_id, String[] timer,
                               String[] mobile) {
        this.context = context;
        this.worker_id = worker_id;
        this.worker_name = worker_name;
        this.worker_image = worker_image;
        this.worker_rating = worker_rating;
        this.worker_status = worker_status;
        this.worker_timer = worker_timer;
        this.service_id = service_id;
        this.worker_lat = worker_lat;
        this.worker_long = worker_long;
        this.current_flag = current_flag;
        this.notification_id = notification_id;
        this.timer = timer;
        this.mobile = mobile;
        listener = (RefreshActivity) this.context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return worker_id.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return worker_id[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {

        ImageView img_profile, img_task1, img_task2;
        RelativeLayout rl_button1, rl_button2, rl_button3;
        TextView tv_name, tv_total_rate, tv_task1, tv_task2, tv_list1, tv_list2, tv_list3;
        RatingBar ratingBar;
        Chronometer cm_timer;
        LinearLayout ll_chat;

    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        // TODO Auto-generated method stub

        final Holder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_schedule_list_item, null);
            holder = new Holder();

            holder.img_profile = (ImageView) convertView.findViewById(R.id.img_sc_list_pic);
            holder.img_task1 = (ImageView) convertView.findViewById(R.id.img_sc_list_task1);
            holder.img_task2 = (ImageView) convertView.findViewById(R.id.img_sc_list_task2);
            holder.rl_button1 = (RelativeLayout) convertView.findViewById(R.id.rl_sc_list1);
            holder.rl_button2 = (RelativeLayout) convertView.findViewById(R.id.rl_sc_list2);
            holder.rl_button3 = (RelativeLayout) convertView.findViewById(R.id.rl_sc_list3);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_sc_list_name);
            holder.tv_total_rate = (TextView) convertView.findViewById(R.id.tv_sc_list_total_rating);
            holder.tv_task1 = (TextView) convertView.findViewById(R.id.tv_sc_list_task1);
            holder.tv_task2 = (TextView) convertView.findViewById(R.id.tv_sc_list_task2);
            holder.ratingBar = (RatingBar) convertView.findViewById(R.id.rating_sc);
            holder.tv_list1 = (TextView) convertView.findViewById(R.id.tv_sc_list1);
            holder.tv_list2 = (TextView) convertView.findViewById(R.id.tv_sc_list2);
            holder.tv_list3 = (TextView) convertView.findViewById(R.id.tv_sc_list3);
            holder.cm_timer = (Chronometer) convertView.findViewById(R.id.cm_timer);
            holder.ll_chat = (LinearLayout) convertView.findViewById(R.id.ll_chat);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.tv_name.setText(worker_name[position]);
        holder.ratingBar.setRating(Float.parseFloat(worker_rating[position]));
        Glide.with(context).load(worker_image[position])
                .thumbnail(0.5f)
                .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)                            .error(R.mipmap.user))
                .into(holder.img_profile);

        holder.cm_timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String t = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
                chronometer.setText(t);
            }
        });
        holder.cm_timer.setBase(SystemClock.elapsedRealtime());
        holder.cm_timer.setText("00:00:00");

        if (worker_status[position].equalsIgnoreCase("none") || worker_status[position].equalsIgnoreCase("request")) {
            holder.rl_button1.setBackgroundResource(R.mipmap.greenbelt);
            holder.rl_button2.setBackgroundResource(R.mipmap.greybelt);
            holder.rl_button3.setBackgroundResource(R.mipmap.greybelt);
            holder.tv_list1.setText("Request");
            holder.tv_list2.setText("Start");
            holder.tv_list3.setText("Finish");
            holder.img_task1.setVisibility(View.INVISIBLE);
            holder.img_task2.setImageResource(R.mipmap.sc_cancle);
            holder.ll_chat.setVisibility(View.INVISIBLE);
            holder.cm_timer.setVisibility(View.GONE);
            holder.tv_task2.setVisibility(View.VISIBLE);
            holder.tv_task1.setText("");
            holder.tv_task2.setText("Cancel");
        } else if (worker_status[position].equalsIgnoreCase("accept")) {
            holder.rl_button1.setBackgroundResource(R.mipmap.greenbelt);
            holder.rl_button2.setBackgroundResource(R.mipmap.greybelt);
            holder.rl_button3.setBackgroundResource(R.mipmap.greybelt);
            holder.tv_list1.setText("Accept");
            holder.tv_list2.setText("Start");
            holder.tv_list3.setText("Finish");
            holder.img_task1.setImageResource(R.mipmap.map);
            holder.img_task2.setImageResource(R.mipmap.sc_cancle);
            holder.ll_chat.setVisibility(View.VISIBLE);
            holder.cm_timer.setVisibility(View.GONE);
            holder.tv_task2.setVisibility(View.VISIBLE);
            holder.tv_task1.setText("Map");
            holder.tv_task2.setText("Cancel");
        } else if (worker_status[position].equalsIgnoreCase("interview")) {
            holder.rl_button1.setBackgroundResource(R.mipmap.greybelt);
            holder.rl_button2.setBackgroundResource(R.mipmap.greenbelt);
            holder.rl_button3.setBackgroundResource(R.mipmap.greybelt);
            holder.tv_list1.setText("Accept");
            holder.tv_list2.setText("Interviewed");
            holder.tv_list3.setText("Finish");
            holder.img_task1.setImageResource(R.mipmap.map);
            holder.img_task2.setImageResource(R.mipmap.sc_cancle);
            holder.ll_chat.setVisibility(View.VISIBLE);
            holder.cm_timer.setVisibility(View.GONE);
            holder.tv_task2.setVisibility(View.VISIBLE);
            holder.tv_task1.setText("Map");
            holder.tv_task2.setText("Cancel");
        } else if (worker_status[position].equalsIgnoreCase("start")) {
            holder.rl_button1.setBackgroundResource(R.mipmap.greybelt);
            holder.rl_button2.setBackgroundResource(R.mipmap.greenbelt);
            holder.rl_button3.setBackgroundResource(R.mipmap.greybelt);
            holder.tv_list1.setText("Accept");
            holder.tv_list2.setText("Ongoing");
            holder.tv_list3.setText("Finish");
            holder.ll_chat.setVisibility(View.VISIBLE);
            holder.img_task1.setImageResource(R.mipmap.map);
            holder.img_task2.setImageResource(R.mipmap.sc_green_clock);
            holder.cm_timer.setVisibility(View.VISIBLE);
            holder.tv_task2.setVisibility(View.GONE);
            holder.tv_task1.setText("Map");
            holder.tv_task2.setText("");
            try {
                if (timer[position].equalsIgnoreCase("0"))
                    timer[position] = "00:00:00";
                holder.cm_timer.setText(timer[position]);
                holder.cm_timer.setBase(SystemClock.elapsedRealtime() - convert(timer[position]));
                holder.cm_timer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (worker_status[position].equalsIgnoreCase("pause")) {
            holder.rl_button1.setBackgroundResource(R.mipmap.greybelt);
            holder.rl_button2.setBackgroundResource(R.mipmap.greenbelt);
            holder.rl_button3.setBackgroundResource(R.mipmap.greybelt);
            holder.tv_list1.setText("Accept");
            holder.tv_list2.setText("Pause");
            holder.tv_list3.setText("Finish");
            holder.ll_chat.setVisibility(View.VISIBLE);
            holder.img_task1.setImageResource(R.mipmap.map);
            holder.img_task2.setImageResource(R.mipmap.sc_red_clock);
            holder.cm_timer.setVisibility(View.VISIBLE);
            holder.tv_task2.setVisibility(View.GONE);
            holder.tv_task1.setText("Map");
            holder.tv_task2.setText("");
            holder.cm_timer.setText(timer[position]);
        } else if (worker_status[position].equalsIgnoreCase("finish")) {
            holder.rl_button1.setBackgroundResource(R.mipmap.greybelt);
            holder.rl_button2.setBackgroundResource(R.mipmap.greybelt);
            holder.rl_button3.setBackgroundResource(R.mipmap.greenbelt);
            holder.tv_list1.setText("Accept");
            holder.tv_list2.setText("Start");
            holder.tv_list3.setText("Finish");
            holder.ll_chat.setVisibility(View.INVISIBLE);
            holder.img_task1.setVisibility(View.INVISIBLE);
            holder.img_task2.setVisibility(View.VISIBLE);
            holder.img_task2.setImageResource(R.mipmap.pay);
            holder.cm_timer.setVisibility(View.GONE);
            holder.tv_task2.setVisibility(View.VISIBLE);
            holder.tv_task1.setText("");
            holder.tv_task2.setText("Pay");
        }

        holder.img_task1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (worker_status[position].equalsIgnoreCase("accept")) {
                    // Map
                    Intent intent = new Intent(context, ScheduleMapsActivity.class);

                    Utility.writeSharedPreferences(context, "current_task", "accept");
                    Utility.writeSharedPreferences(context, "sc_worker_id", worker_id[position]);
                    Utility.writeSharedPreferences(context, "sc_profile", worker_image[position]);
                    Utility.writeSharedPreferences(context, "sc_name", worker_name[position]);
                    Utility.writeSharedPreferences(context, "sc_mobile", mobile[position]);
                    Utility.writeSharedPreferences(context, "sc_rating", worker_rating[position]);
                    Utility.writeSharedPreferences(context, "sc_total_review", "");
                    Utility.writeSharedPreferences(context, "sc_notification_id", notification_id[position]);
                    Utility.writeSharedPreferences(context, "sc_service_id", service_id[position]);
                    Utility.writeSharedPreferences(context, "sc_user_lat", "");
                    Utility.writeSharedPreferences(context, "sc_user_long", "");
                    Utility.writeSharedPreferences(context, "sc_worker_lat", worker_lat[position]);
                    Utility.writeSharedPreferences(context, "sc_worker_long", worker_long[position]);

                    context.startActivity(intent);
                } else if (worker_status[position].equalsIgnoreCase("interview")) {
                    // Map
                    Intent intent = new Intent(context, ScheduleMapsActivity.class);

                    Utility.writeSharedPreferences(context, "current_task", "interview");
                    Utility.writeSharedPreferences(context, "sc_worker_id", worker_id[position]);
                    Utility.writeSharedPreferences(context, "sc_profile", worker_image[position]);
                    Utility.writeSharedPreferences(context, "sc_name", worker_name[position]);
                    Utility.writeSharedPreferences(context, "sc_mobile", mobile[position]);
                    Utility.writeSharedPreferences(context, "sc_rating", worker_rating[position]);
                    Utility.writeSharedPreferences(context, "sc_total_review", "");
                    Utility.writeSharedPreferences(context, "sc_notification_id", notification_id[position]);
                    Utility.writeSharedPreferences(context, "sc_service_id", service_id[position]);
                    Utility.writeSharedPreferences(context, "sc_user_lat", "");
                    Utility.writeSharedPreferences(context, "sc_user_long", "");
                    Utility.writeSharedPreferences(context, "sc_worker_lat", worker_lat[position]);
                    Utility.writeSharedPreferences(context, "sc_worker_long", worker_long[position]);

                    context.startActivity(intent);
                } else if (worker_status[position].equalsIgnoreCase("start")) {
                    // Map
                    Intent intent = new Intent(context, ScheduleMapsActivity.class);

                    Utility.writeSharedPreferences(context, "current_task", "start");
                    Utility.writeSharedPreferences(context, "sc_worker_id", worker_id[position]);
                    Utility.writeSharedPreferences(context, "sc_profile", worker_image[position]);
                    Utility.writeSharedPreferences(context, "sc_name", worker_name[position]);
                    Utility.writeSharedPreferences(context, "sc_mobile", mobile[position]);
                    Utility.writeSharedPreferences(context, "sc_rating", worker_rating[position]);
                    Utility.writeSharedPreferences(context, "sc_total_review", "");
                    Utility.writeSharedPreferences(context, "sc_notification_id", notification_id[position]);
                    Utility.writeSharedPreferences(context, "sc_service_id", service_id[position]);
                    Utility.writeSharedPreferences(context, "sc_user_lat", "");
                    Utility.writeSharedPreferences(context, "sc_user_long", "");
                    Utility.writeSharedPreferences(context, "sc_worker_lat", worker_lat[position]);
                    Utility.writeSharedPreferences(context, "sc_worker_long", worker_long[position]);
                    Utility.writeSharedPreferences(context, "sc_timer", holder.cm_timer.getText().toString());

                    context.startActivity(intent);
                } else if (worker_status[position].equalsIgnoreCase("pause")) {
                    // Map
                    Intent intent = new Intent(context, ScheduleMapsActivity.class);

                    Utility.writeSharedPreferences(context, "current_task", "pause");
                    Utility.writeSharedPreferences(context, "sc_worker_id", worker_id[position]);
                    Utility.writeSharedPreferences(context, "sc_profile", worker_image[position]);
                    Utility.writeSharedPreferences(context, "sc_name", worker_name[position]);
                    Utility.writeSharedPreferences(context, "sc_mobile", mobile[position]);
                    Utility.writeSharedPreferences(context, "sc_rating", worker_rating[position]);
                    Utility.writeSharedPreferences(context, "sc_total_review", "");
                    Utility.writeSharedPreferences(context, "sc_notification_id", notification_id[position]);
                    Utility.writeSharedPreferences(context, "sc_service_id", service_id[position]);
                    Utility.writeSharedPreferences(context, "sc_user_lat", "");
                    Utility.writeSharedPreferences(context, "sc_user_long", "");
                    Utility.writeSharedPreferences(context, "sc_worker_lat", worker_lat[position]);
                    Utility.writeSharedPreferences(context, "sc_worker_long", worker_long[position]);
                    Utility.writeSharedPreferences(context, "sc_timer", holder.cm_timer.getText().toString());

                    context.startActivity(intent);
                }
            }
        });

        holder.img_task2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (worker_status[position].equalsIgnoreCase("none") ||
                        worker_status[position].equalsIgnoreCase("request") ||
                        worker_status[position].equalsIgnoreCase("accept") ||
                        worker_status[position].equalsIgnoreCase("interview")) {

                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("dMe");
                    alertDialog.setMessage("Are you sure you want to cancel this job?");
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    // cancel
                                    if (Utility.isNetworkAvaliable(context)) {
                                        try {
                                            cancelRequest getTask = new cancelRequest(context);
                                            getTask.execute(service_id[position]);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else if (worker_status[position].equalsIgnoreCase("finish")) {
                    Intent intent = new Intent(context, InvoiceActivity.class);
                    Utility.writeSharedPreferences(context, "sc_flag", "1");
                    Utility.writeSharedPreferences(context, "invoice_s_id", service_id[position]);
                    Utility.writeSharedPreferences(context, "coupon_flag", "0");
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }
        });

        holder.ll_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.writeSharedPreferences(context, "chat_from", "activity");
                Intent intent_chat = new Intent(context, ChatActivity.class);
                intent_chat.putExtra("worker_id", worker_id[position]);
                intent_chat.putExtra("notification_id", notification_id[position]);
                intent_chat.putExtra("user_name", worker_name[position]);
                intent_chat.putExtra("user_image", worker_image[position]);
                intent_chat.putExtra("user_category", Utility.getAppPrefString(context,
                        "sc_job_type"));
                intent_chat.putExtra("user_mobile", mobile[position]);
                intent_chat.putExtra("chatFrom","activity");
                context.startActivity(intent_chat);
            }
        });

        return convertView;
    }

    private long convert(String time) throws ParseException {
        String myDate = time;
        long milliseconds;

        String[] arrayString = myDate.split(":");
        int hours = Integer.parseInt(arrayString[0]);
        int minutes = Integer.parseInt(arrayString[1]);

        char toCheck = ':';
        int count = 0;

        for (char ch : myDate.toCharArray()) {
            if (ch == toCheck) {
                count++;
            }
        }

        if (count > 1) {
            int second = Integer.parseInt(arrayString[2]);
            milliseconds = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes) + TimeUnit.SECONDS.toSeconds(second));
        } else {
            milliseconds = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes));
        }
        return milliseconds;
    }

    // Cancel Request
    public class cancelRequest extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message;
        String response;

        public cancelRequest(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem(params[0]);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {

                    notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    listener.refresh();

                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem(String n_id) {
            String webUrl = Constant.URL_CANCEL_INTERVIEWED;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", n_id));

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public interface RefreshActivity {
        public void refresh();
    }

}
