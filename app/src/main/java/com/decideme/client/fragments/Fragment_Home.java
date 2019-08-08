package com.decideme.client.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.activities.InvoiceActivity;
import com.decideme.client.activities.LoginActivity;
import com.decideme.client.activities.MapsActivity;
import com.decideme.client.activities.ScheduleActivity;
import com.decideme.client.adapter.CategoryListAdapter;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.Utility;
import com.decideme.client.model.CategoryList;
import com.decideme.client.model.CategoryListModel;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vivek.shah on 12-Jan-16.
 */
public class Fragment_Home extends Fragment {

    View view;
    private ListView listView;
    private CategoryListAdapter categoryListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        view = inflater.inflate(R.layout.content_home,
                container, false);

        init();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                categoryListAdapter = ((CategoryListAdapter) listView.getAdapter());

                Utility.writeSharedPreferences(getActivity(), "activity_from", "activity");
                Utility.writeSharedPreferences(getActivity(), "schedule", "false");
                Utility.writeSharedPreferences(getActivity(), "map_flag", "0");
                Utility.writeSharedPreferences(getActivity(), "rate", ((CategoryList) categoryListAdapter.getItem(position)).getRate());
                Utility.writeSharedPreferences(getActivity(), "cat", ((CategoryList) categoryListAdapter.getItem(position)).getCat_name());
                Utility.writeSharedPreferences(getActivity(), "cat_id", ((CategoryList) categoryListAdapter.getItem(position)).getCat_id());

                if (Utility.isNetworkAvaliable(getActivity())) {
                    try {
                        getCurrentData getTask = new getCurrentData(getActivity());
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(), "No Internet Connection!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    public void init() {

        listView = (ListView) view.findViewById(R.id.list_category);
        if (Utility.isNetworkAvaliable(getActivity())) {
            try {
                getCategoryList getTask = new getCategoryList(getActivity());
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), "No Internet Connection!", Toast.LENGTH_LONG).show();
        }

    }

    public class getCategoryList extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        private String url;
        CategoryListModel categoryListModel;

        public getCategoryList(final Context mContext) {
            this.mContext = mContext;
            mProgressDialog = new DME_ProgressDilog(mContext);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }

        @Override
        protected Object doInBackground(String... params) {
            CategoryListModel categoryListModel = (CategoryListModel) getAboutMeListItem();
            if (categoryListModel != null)
                return categoryListModel.getCat_data();
            else
                return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                if (result != null) {
                    Utility.writeSharedPreferences(mContext,"help_url",url);
                    if (categoryListModel.getResponse().equalsIgnoreCase("true")) {
                        if (result != null && result instanceof List) {
                            categoryListAdapter = new CategoryListAdapter(getActivity(), (List<CategoryList>) result);
                            listView.setAdapter(categoryListAdapter);
                        }
                    } else {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_CATEGORY;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("deviceID", Utility.getAppPrefString(mContext, "device_id")));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");
                url = jObject.getString("help_url");
                categoryListModel = (CategoryListModel) new Gson().fromJson(jObject.toString(), CategoryListModel.class);

                return categoryListModel;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class getCurrentData extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        String response;
        String message, type;
        private final DME_ProgressDilog mProgressDialog;
        String service_type, current_flag, service_id;

        public getCurrentData(final Context mContext) {
            this.mContext = mContext;
            mProgressDialog = new DME_ProgressDilog(mContext);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            try {
                if(type.equalsIgnoreCase("authorized")) {
                    getActivity().finish();
                    if (response.equalsIgnoreCase("true")) {
                        if (service_type.equalsIgnoreCase("schedual")) {
                            Utility.writeSharedPreferences(mContext, "schedule", "true");
                            Intent intent = new Intent(getActivity(), ScheduleActivity.class);
                            startActivity(intent);
                        } else if (current_flag.equalsIgnoreCase("finish")) {
                            Intent intent = new Intent(getActivity(), InvoiceActivity.class);
                            Utility.writeSharedPreferences(mContext, "invoice_s_id", service_id);
                            Utility.writeSharedPreferences(mContext, "sc_flag", "0");
                            Utility.writeSharedPreferences(mContext, "coupon_flag", "0");
                            startActivity(intent);
                        } else {
                            Utility.writeSharedPreferences(mContext, "data_flag", "true");
                            Utility.writeSharedPreferences(mContext, "schedule", "false");
                            Intent intent = new Intent(getActivity(), MapsActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Utility.writeSharedPreferences(mContext, "data_flag", "true");
                        Utility.writeSharedPreferences(mContext, "schedule", "false");
                        Intent intent = new Intent(getActivity(), MapsActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Utility.writeSharedPreferences(mContext, "login", "false");
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setTitle("Authentication Error");
                    alertDialog.setMessage("Authentication error occur. Please login again.");
                    alertDialog.setCancelable(false);

                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                            Intent intent = new Intent(mContext,
                                    LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                    alertDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_CURRENT_DATA;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("deviceID", Utility.getAppPrefString(mContext, "device_id")));
                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");
                type = jObject.getString("type");
                //authorized

                if (response.equalsIgnoreCase("true")) {
                    service_type = jObject.getString("service_type");
                    current_flag = jObject.getString("current_flag");
                    service_id = jObject.getString("service_id");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}