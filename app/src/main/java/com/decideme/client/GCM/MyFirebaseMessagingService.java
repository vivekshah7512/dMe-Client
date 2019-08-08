package com.decideme.client.GCM;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.decideme.client.R;
import com.decideme.client.activities.ChatActivity;
import com.decideme.client.activities.HomeActivity;
import com.decideme.client.activities.MapsActivity;
import com.decideme.client.activities.ScheduleActivity;
import com.decideme.client.activities.ScheduleMapsActivity;
import com.decideme.client.attributes.Utility;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;

/**
 * Created by vivek_shah on 1/8/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    String json;
    JSONObject jobject;
    private int count = 0;
    private String title, message, message1, image, notification_type;
    private boolean isLogout = false;
    private String worker_latitude, worker_longitude, worker_id, notification_id, service_id;
    private String worker_name,
            worker_image,
            worker_rating,
            worker_total_review,
            worker_job_type,
            worker_mobile, user_id, user_image, user_name, user_category, user_mobile,
            user_latitude, user_longitude, time, type1, request_type;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "Received message " + remoteMessage.getData());
        Map data = remoteMessage.getData();

        try {
            json = remoteMessage.getData().get("data");
            jobject = new JSONObject(json);
            if (jobject.has("message")) {
                message = (String) jobject.getString("message");
            }
            message1 = (String) data.get("message");
            if (!jobject.has("notification_type")) {
                notification_type = jobject.getString("request_type");
            } else {
                notification_type = jobject.getString("notification_type");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Utility.getAppPrefString(getApplicationContext(), "login").equalsIgnoreCase("true")) {
            if (notification_type != null && notification_type.equalsIgnoreCase("Request Accepted")) {
                try {
                    request_type = jobject.getString("request_type");
                    if (request_type.equalsIgnoreCase("REQ_ACCEPT")) {
                        notification_id = jobject.getString("notification_id");
                        service_id = jobject.getString("service_id");
                    } else if (request_type.equalsIgnoreCase("ACCEPTEDREQ")) {
                        worker_latitude = jobject.getString("worker_latitude");
                        worker_longitude = jobject.getString("worker_longitude");
                        user_latitude = jobject.getString("user_latitude");
                        user_longitude = jobject.getString("user_longitude");
                        worker_id = jobject.getString("worker_id");
                        worker_name = jobject.getString("worker_name");
                        worker_image = jobject.getString("worker_image");
                        worker_rating = jobject.getString("worker_rating");
                        worker_total_review = jobject.getString("worker_total_review");
                        title = (String) jobject.getString("title");
                        worker_job_type = jobject.getString("worker_job_type");
                        worker_mobile = jobject.getString("worker_mobile");
                        notification_id = jobject.getString("notification_id");
                        service_id = jobject.getString("service_id");

                        if (Utility.getAppPrefString(getApplicationContext(), "schedule").equalsIgnoreCase("true")) {
                            if (ScheduleActivity.active_schedule) {
                                Intent intent = new Intent("unique_name1");
                                getApplicationContext().sendBroadcast(intent);
                            } else {
                                Intent intent = new Intent(this, ScheduleActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }

                    } else {
                        worker_latitude = jobject.getString("worker_latitude");
                        worker_longitude = jobject.getString("worker_longitude");
                        user_latitude = jobject.getString("user_latitude");
                        user_longitude = jobject.getString("user_longitude");
                        worker_id = jobject.getString("worker_id");
                        worker_name = jobject.getString("worker_name");
                        worker_image = jobject.getString("worker_image");
                        worker_rating = jobject.getString("worker_rating");
                        worker_total_review = jobject.getString("worker_total_review");
                        title = (String) jobject.getString("title");
                        worker_job_type = jobject.getString("worker_job_type");
                        worker_mobile = jobject.getString("worker_mobile");
                        notification_id = jobject.getString("notification_id");
                        service_id = jobject.getString("service_id");
                    }
                    createNotification(message, "Request Accepted", notification_type);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (notification_type != null && notification_type.equalsIgnoreCase("chat")) {
                if (ChatActivity.active) {
                    Utility.writeSharedPreferences(this, "chat_from", "notification");
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("chatFrom","notification");
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    try {
                        user_id = jobject.getString("user_id");
                        user_image = jobject.getString("user_image");
                        user_name = jobject.getString("user_name");
                        user_category = jobject.getString("user_category");
                        user_mobile = jobject.getString("user_mobile");
                        message = jobject.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    createNotification(message, "DME", notification_type);
                }
            } else if (notification_type != null && notification_type.equalsIgnoreCase("service start")) {
                try {
                    worker_id = jobject.getString("worker_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                createNotification(message, "Job Started", notification_type);
            } else if (notification_type != null && notification_type.equalsIgnoreCase("service end")) {
                try {
                    service_id = jobject.getString("service_id");
                    worker_id = jobject.getString("worker_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                createNotification(message, "Job Finished", notification_type);
            } else if (notification_type != null && notification_type.equalsIgnoreCase("cancelinterview")) {
                try {
                    worker_id = jobject.getString("worker_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                createNotification(message1, "Job Cancelled", notification_type);
            } else if (notification_type != null && notification_type.equalsIgnoreCase("saveservicetime")) {
                try {
                    worker_id = jobject.getString("worker_id");
                    time = jobject.getString("time");
                    type1 = jobject.getString("type");
                    Utility.writeSharedPreferences(getApplicationContext(), "timer", time);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (type1.equalsIgnoreCase("start")) {
                    createNotification("Your Job has been started", "Job Resume", notification_type);
                } else {
                    createNotification("Your Job has been paused", "Job Paused", notification_type);
                }
            } else if (notification_type != null && notification_type.equalsIgnoreCase("interview")) {
                createNotification("Your interviewed has been started", "Interviewed Started", notification_type);
            } else if (notification_type != null && notification_type.equalsIgnoreCase("Request Rejected")) {
                createNotification(message, "Request Rejected", notification_type);
            } else if (notification_type != null && notification_type.equalsIgnoreCase("special_promocode")) {
                createNotification(message, "New Code Arrived", notification_type);
            }
        } else {
            Log.v("Notification: ", "Logout");
        }
    }

    //This method is only generating push notification

    private void createNotification(String message, String nTitle, String type) {

        Intent intent = null;
        String channelId = "1057886";
        String channelName = "dMe";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (type.equalsIgnoreCase("Request Accepted")) {
            if (request_type.equalsIgnoreCase("REQ_ACCEPT")) {
                intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Utility.writeSharedPreferences(getApplicationContext(), "sc_accept", "true");
                Utility.writeSharedPreferences(getApplicationContext(), "coupon_flag", "false");
            } else if (request_type.equalsIgnoreCase("ACCEPTEDREQ")) {
                if (Utility.getAppPrefString(getApplicationContext(), "schedule").equalsIgnoreCase("true")) {
                    if (ScheduleActivity.active_schedule) {
                        intent = new Intent("unique_name1");
                        getApplicationContext().sendBroadcast(intent);
                    } else {
                        intent = new Intent(this, ScheduleActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                } else {
                    if (MapsActivity.active_map) {
                        intent = new Intent("unique_name");
                        Utility.writeSharedPreferences(getApplicationContext(), "process_type", "normal");
                        Utility.writeSharedPreferences(getApplicationContext(), "sc_accept", "false");
                        Utility.writeSharedPreferences(getApplicationContext(), "coupon_flag", "false");
                        Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "notification");
                        Utility.writeSharedPreferences(getApplicationContext(), "noti_flag", "accept");
                        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        intent.putExtra("worker_latitude", worker_latitude);
                        intent.putExtra("worker_longitude", worker_longitude);
                        intent.putExtra("user_latitude", user_latitude);
                        intent.putExtra("user_longitude", user_longitude);
                        intent.putExtra("worker_id", worker_id);
                        intent.putExtra("worker_name", worker_name);
                        intent.putExtra("worker_image", worker_image);
                        intent.putExtra("worker_rating", worker_rating);
                        intent.putExtra("worker_total_review", worker_total_review);
                        intent.putExtra("worker_job_type", worker_job_type);
                        intent.putExtra("worker_mobile", worker_mobile);
                        intent.putExtra("notification_id", notification_id);
                        intent.putExtra("service_id", service_id);

                        Utility.writeSharedPreferences(getApplicationContext(), "worker_latitude", worker_latitude);
                        Utility.writeSharedPreferences(getApplicationContext(), "worker_longitude", worker_longitude);
                        Utility.writeSharedPreferences(getApplicationContext(), "user_latitude", user_latitude);
                        Utility.writeSharedPreferences(getApplicationContext(), "user_longitude", user_longitude);
                        Utility.writeSharedPreferences(getApplicationContext(), "worker_id", worker_id);
                        Utility.writeSharedPreferences(getApplicationContext(), "worker_name", worker_name);
                        Utility.writeSharedPreferences(getApplicationContext(), "worker_image", worker_image);
                        Utility.writeSharedPreferences(getApplicationContext(), "worker_rating", worker_rating);
                        Utility.writeSharedPreferences(getApplicationContext(), "worker_total_review", worker_total_review);
                        Utility.writeSharedPreferences(getApplicationContext(), "worker_job_type", worker_job_type);
                        Utility.writeSharedPreferences(getApplicationContext(), "worker_mobile", worker_mobile);
                        Utility.writeSharedPreferences(getApplicationContext(), "notification_id", notification_id);
                        Utility.writeSharedPreferences(getApplicationContext(), "service_id", service_id);

                        getApplicationContext().sendBroadcast(intent);
                    } else {
                        intent = new Intent(this, MapsActivity.class);
                        Utility.writeSharedPreferences(getApplicationContext(), "process_type", "normal");
                        Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "true");
                        Utility.writeSharedPreferences(getApplicationContext(), "schedule", "false");
                        Utility.writeSharedPreferences(getApplicationContext(), "sc_accept", "false");
                        Utility.writeSharedPreferences(getApplicationContext(), "coupon_flag", "false");
                        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                }
            } else if (request_type.equalsIgnoreCase("SCHEDUAL_ACCEPT")) {
                Utility.writeSharedPreferences(getApplicationContext(), "process_type", "schedule");
                if (ScheduleActivity.active_schedule) {
                    intent = new Intent("unique_name1");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, ScheduleActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            } else {
                if (MapsActivity.active_map) {
                    intent = new Intent("unique_name");
                    Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "notification");
                    Utility.writeSharedPreferences(getApplicationContext(), "noti_flag", "accept");
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.putExtra("worker_latitude", worker_latitude);
                    intent.putExtra("worker_longitude", worker_longitude);
                    intent.putExtra("user_latitude", user_latitude);
                    intent.putExtra("user_longitude", user_longitude);
                    intent.putExtra("worker_id", worker_id);
                    intent.putExtra("worker_name", worker_name);
                    intent.putExtra("worker_image", worker_image);
                    intent.putExtra("worker_rating", worker_rating);
                    intent.putExtra("worker_total_review", worker_total_review);
                    intent.putExtra("worker_job_type", worker_job_type);
                    intent.putExtra("worker_mobile", worker_mobile);
                    intent.putExtra("notification_id", notification_id);
                    intent.putExtra("service_id", service_id);

                    Utility.writeSharedPreferences(getApplicationContext(), "worker_latitude", worker_latitude);
                    Utility.writeSharedPreferences(getApplicationContext(), "worker_longitude", worker_longitude);
                    Utility.writeSharedPreferences(getApplicationContext(), "user_latitude", user_latitude);
                    Utility.writeSharedPreferences(getApplicationContext(), "user_longitude", user_longitude);
                    Utility.writeSharedPreferences(getApplicationContext(), "worker_id", worker_id);
                    Utility.writeSharedPreferences(getApplicationContext(), "worker_name", worker_name);
                    Utility.writeSharedPreferences(getApplicationContext(), "worker_image", worker_image);
                    Utility.writeSharedPreferences(getApplicationContext(), "worker_rating", worker_rating);
                    Utility.writeSharedPreferences(getApplicationContext(), "worker_total_review", worker_total_review);
                    Utility.writeSharedPreferences(getApplicationContext(), "worker_job_type", worker_job_type);
                    Utility.writeSharedPreferences(getApplicationContext(), "worker_mobile", worker_mobile);
                    Utility.writeSharedPreferences(getApplicationContext(), "notification_id", notification_id);
                    Utility.writeSharedPreferences(getApplicationContext(), "service_id", service_id);

                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, MapsActivity.class);
                    Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "true");
                    Utility.writeSharedPreferences(getApplicationContext(), "schedule", "false");
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }
        } else if (type.equalsIgnoreCase("Request Rejected")) {
            intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Utility.writeSharedPreferences(getApplicationContext(), "sc_accept", "false");
            Utility.writeSharedPreferences(getApplicationContext(), "coupon_flag", "false");
        } else if (type.equalsIgnoreCase("service end")) {
            if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("schedule")) {
                if (ScheduleMapsActivity.active_schedule_map) {
                    if (worker_id.equalsIgnoreCase(Utility.getAppPrefString(getApplicationContext(), "sc_worker_id"))) {
                        intent = new Intent("unique_name2");
                        getApplicationContext().sendBroadcast(intent);
                    }
                } else if (ScheduleActivity.active_schedule) {
                    intent = new Intent("unique_name1");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, ScheduleActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            } else if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("normal")) {
                if (MapsActivity.active_map) {
                    intent = new Intent("unique_name");
                    Utility.writeSharedPreferences(getApplicationContext(), "noti_flag", "finish");
                    Utility.writeSharedPreferences(getApplicationContext(), "invoice_s_id", service_id);
                    Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "finish");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, MapsActivity.class);
                    Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "true");
                    Utility.writeSharedPreferences(getApplicationContext(), "schedule", "false");
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }
        } else if (type.equalsIgnoreCase("saveservicetime")) {
            if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("schedule")) {
                if (ScheduleMapsActivity.active_schedule_map) {
                    if (worker_id.equalsIgnoreCase(Utility.getAppPrefString(getApplicationContext(), "sc_worker_id"))) {
                        intent = new Intent("unique_name2");
                        getApplicationContext().sendBroadcast(intent);
                    }
                } else if (ScheduleActivity.active_schedule) {
                    intent = new Intent("unique_name1");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, ScheduleActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            } else if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("normal")) {
                if (MapsActivity.active_map) {
                    intent = new Intent("unique_name");
                    if (time == null || time.equalsIgnoreCase("") || time.equalsIgnoreCase("null"))
                        Utility.writeSharedPreferences(getApplicationContext(), "noti_save_time", "00:00:00");
                    else
                        Utility.writeSharedPreferences(getApplicationContext(), "noti_save_time", time);
                    Utility.writeSharedPreferences(getApplicationContext(), "noti_save_time_type", type1);
                    Utility.writeSharedPreferences(getApplicationContext(), "noti_flag", "time");
                    Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "savetime");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, MapsActivity.class);
                    Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "true");
                    Utility.writeSharedPreferences(getApplicationContext(), "schedule", "false");
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }
        } else if (type.equalsIgnoreCase("chat")) {
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("worker_id", user_id);
            intent.putExtra("user_image", user_image);
            intent.putExtra("user_name", user_name);
            intent.putExtra("user_mobile", user_mobile);
            intent.putExtra("user_category", user_category);
            intent.putExtra("chatFrom","notification");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (type.equalsIgnoreCase("service start")) {
            if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("schedule")) {
                if (ScheduleMapsActivity.active_schedule_map) {
                    if (worker_id.equalsIgnoreCase(Utility.getAppPrefString(getApplicationContext(), "sc_worker_id"))) {
                        intent = new Intent("unique_name2");
                        getApplicationContext().sendBroadcast(intent);
                    }
                } else if (ScheduleActivity.active_schedule) {
                    intent = new Intent("unique_name1");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, ScheduleActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            } else if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("normal")) {
                if (MapsActivity.active_map) {
                    intent = new Intent("unique_name");
                    Utility.writeSharedPreferences(getApplicationContext(), "noti_flag", "start");
                    Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "start");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, MapsActivity.class);
                    Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "true");
                    Utility.writeSharedPreferences(getApplicationContext(), "schedule", "false");
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }
        } else if (type.equalsIgnoreCase("cancelinterview")) {
            if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("schedule")) {
                if (ScheduleMapsActivity.active_schedule_map) {
                    if (worker_id.equalsIgnoreCase(Utility.getAppPrefString(getApplicationContext(), "sc_worker_id"))) {
                        intent = new Intent("unique_name2");
                        getApplicationContext().sendBroadcast(intent);
                    } else {
                        intent = new Intent(this, ScheduleActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    }
                } else if (ScheduleActivity.active_schedule) {
                    intent = new Intent("unique_name1");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            } else if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("normal")) {
                if (MapsActivity.active_map) {
                    intent = new Intent("unique_name");
                    Utility.writeSharedPreferences(getApplicationContext(), "noti_flag", "cancel");
                    Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "cancel");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }
        } else if (type.equalsIgnoreCase("interview")) {
            if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("schedule")) {
                if (ScheduleActivity.active_schedule) {
                    intent = new Intent("unique_name1");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, ScheduleActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            } else if (Utility.getAppPrefString(getApplicationContext(), "process_type").equalsIgnoreCase("normal")) {
                if (MapsActivity.active_map) {
                    intent = new Intent("unique_name");
                    Utility.writeSharedPreferences(getApplicationContext(), "noti_flag", "interview");
                    Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "interview");
                    getApplicationContext().sendBroadcast(intent);
                } else {
                    intent = new Intent(this, MapsActivity.class);
                    Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "true");
                    Utility.writeSharedPreferences(getApplicationContext(), "schedule", "false");
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }
        } else if (type.equalsIgnoreCase("special_promocode")) {
            Utility.writeSharedPreferences(getApplicationContext(), "sc_accept", "false");
            Utility.writeSharedPreferences(getApplicationContext(), "coupon_flag", "true");
            intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            nm.createNotificationChannel(mChannel);
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setColor(ContextCompat.getColor(this, R.color.font_color1))
                .setCategory(Notification.CATEGORY_PROMO)
                .setContentTitle(nTitle)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(nTitle).bigText(message))
                .setSmallIcon(R.mipmap.noti_small)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon))
                .setAutoCancel(true)
                .setVisibility(View.VISIBLE)
                .setContentIntent(contentIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND).build();
        nm.notify(new Random().nextInt(), notification);
    }
}

