package com.decideme.client.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.adapter.ServiceHistoryList1Adapter;
import com.decideme.client.adapter.ServiceHistoryListAdapter;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.ExpandableHeightListView;
import com.decideme.client.attributes.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vivek_shah on 9/11/17.
 */
public class Fragment_Service_History extends Fragment {

    View view;
    private ExpandableHeightListView list_upcoming_history, list_past_history;
    private TextView tv_upcoming_no_data, tv_past_no_data;
    private TabHost tabHost;

    private String[] transaction_id, worker_name, worker_image, worker_cat, status, worker_payment,
            payment_mode, payment_status, date_time, worker_address, user_address, rate, rating,
            timer;
    private String[] transaction_id1, worker_name1, worker_image1, worker_cat1, status1, worker_payment1,
            payment_mode1, payment_status1, date_time1, worker_address1, user_address1, rate1, rating1,
            timer1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        view = inflater.inflate(R.layout.fragment_service_history,
                container, false);

            init();

            if (Utility.isNetworkAvaliable(getActivity())) {
                try {
                    getHistoryList getTask = new getHistoryList(getActivity());
                    getTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        return view;
    }

    public void init() {

        list_upcoming_history = (ExpandableHeightListView) view.findViewById(R.id.list_service_history);
        list_past_history = (ExpandableHeightListView) view.findViewById(R.id.list_service_history1);

        tv_upcoming_no_data = (TextView) view.findViewById(R.id.tv_upcoming_no_data);
        tv_past_no_data = (TextView) view.findViewById(R.id.tv_past_no_data);

        Utility.writeSharedPreferences(getActivity(), "sc_accept", "false");

        tabHost = (TabHost) view.findViewById(R.id.tabHost);
        tabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("Upcoming");
        spec.setContent(R.id.i_layout_1);
        spec.setIndicator("Upcoming");
        tabHost.addTab(spec);

        //Tab 2
        spec = tabHost.newTabSpec("Past");
        spec.setContent(R.id.i_layout_2);
        spec.setIndicator("Past");
        tabHost.addTab(spec);

    }

    public class getHistoryList extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        JSONObject jsonObjectMessage;
        int length, length1;
        private String response, message;

        public getHistoryList(final Context mContext) {
            this.mContext = mContext;
            mProgressDialog = new DME_ProgressDilog(mContext);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {

                    if (length != 0) {
                        tv_past_no_data.setVisibility(View.GONE);
                        list_past_history.setVisibility(View.VISIBLE);
                        list_past_history.setAdapter(new ServiceHistoryList1Adapter(getActivity(), transaction_id, worker_name, worker_image, worker_cat, status, worker_payment,
                                payment_mode, payment_status, date_time, worker_address, user_address
                                , rate, rating, timer));
                    } else {
                        tv_past_no_data.setVisibility(View.VISIBLE);
                        list_past_history.setVisibility(View.GONE);
                    }

                    if (length1 != 0) {
                        tv_upcoming_no_data.setVisibility(View.GONE);
                        list_upcoming_history.setVisibility(View.VISIBLE);
                        list_upcoming_history.setAdapter(new ServiceHistoryListAdapter(getActivity(), transaction_id1, worker_name1, worker_image1, worker_cat1, status1, worker_payment1,
                                payment_mode1, payment_status1, date_time1, worker_address1, user_address1
                                , rate1, rating1));
                    } else {
                        tv_upcoming_no_data.setVisibility(View.VISIBLE);
                        list_upcoming_history.setVisibility(View.GONE);
                    }
                } else {
                    tv_past_no_data.setVisibility(View.VISIBLE);
                    list_past_history.setVisibility(View.GONE);
                    tv_upcoming_no_data.setVisibility(View.VISIBLE);
                    list_upcoming_history.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_HISTORY_LIST;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(getActivity(), Constant.USER_ID)));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {

                    // Past Data
                    JSONArray pastArray = new JSONArray(jObject.getString("past_data"));
                    length = pastArray.length();

                    if (length != 0) {
                        transaction_id = new String[length];
                        worker_name = new String[length];
                        worker_image = new String[length];
                        worker_cat = new String[length];
                        status = new String[length];
                        worker_payment = new String[length];
                        payment_mode = new String[length];
                        payment_status = new String[length];
                        date_time = new String[length];
                        worker_address = new String[length];
                        user_address = new String[length];
                        rate = new String[length];
                        rating = new String[length];
                        timer = new String[length];

                        for (int a = 0; a < length; a++) {
                            jsonObjectMessage = pastArray.getJSONObject(a);
                            try {
                                transaction_id[a] = jsonObjectMessage.getString("transaction_id");
                                worker_name[a] = jsonObjectMessage.getString("client_name");
                                worker_image[a] = jsonObjectMessage.getString("user_img");
                                worker_cat[a] = jsonObjectMessage.getString("service");
                                status[a] = jsonObjectMessage.getString("status");
                                worker_payment[a] = jsonObjectMessage.getString("amount");
                                payment_mode[a] = jsonObjectMessage.getString("payment_mode");
                                payment_status[a] = jsonObjectMessage.getString("payment_status");
                                date_time[a] = jsonObjectMessage.getString("date_time");
                                worker_address[a] = jsonObjectMessage.getString("worker_address");
                                user_address[a] = jsonObjectMessage.getString("user_address");
                                rate[a] = jsonObjectMessage.getString("total_rating");
                                rating[a] = jsonObjectMessage.getString("rating");
                                timer[a] = jsonObjectMessage.getString("timer");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Upcoming Data
                    JSONArray upcomingArray = new JSONArray(jObject.getString("upcoming_data"));
                    length1 = upcomingArray.length();

                    if (length1 != 0) {
                        transaction_id1 = new String[length1];
                        worker_name1 = new String[length1];
                        worker_image1 = new String[length1];
                        worker_cat1 = new String[length1];
                        status1 = new String[length1];
                        worker_payment1 = new String[length1];
                        payment_mode1 = new String[length1];
                        payment_status1 = new String[length1];
                        date_time1 = new String[length1];
                        worker_address1 = new String[length1];
                        user_address1 = new String[length1];
                        rate1 = new String[length1];
                        rating1 = new String[length1];

                        for (int a = 0; a < length1; a++) {
                            jsonObjectMessage = upcomingArray.getJSONObject(a);
                            try {
                                transaction_id1[a] = jsonObjectMessage.getString("transaction_id");
                                worker_name1[a] = jsonObjectMessage.getString("client_name");
                                worker_image1[a] = jsonObjectMessage.getString("user_img");
                                worker_cat1[a] = jsonObjectMessage.getString("service");
                                status1[a] = jsonObjectMessage.getString("status");
                                worker_payment1[a] = jsonObjectMessage.getString("amount");
                                payment_mode1[a] = jsonObjectMessage.getString("payment_mode");
                                payment_status1[a] = jsonObjectMessage.getString("payment_status");
                                date_time1[a] = jsonObjectMessage.getString("date_time");
                                worker_address1[a] = jsonObjectMessage.getString("worker_address");
                                user_address1[a] = jsonObjectMessage.getString("user_address");
                                rate1[a] = jsonObjectMessage.getString("total_rating");
                                rating1[a] = jsonObjectMessage.getString("rating");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                return null;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
