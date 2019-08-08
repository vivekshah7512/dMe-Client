package com.decideme.client.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.adapter.Coupon1ListAdapter;
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
public class Fragment_Coupon extends Fragment {

    View view;
    private ExpandableHeightListView expandableHeightListView;
    private LinearLayout ll_no_data;
    private String[] coupon_code, coupon_description, coupon_expire_date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        view = inflater.inflate(R.layout.fragment_coupon,
                container, false);

            init();

        return view;
    }

    public void init() {

        ll_no_data = (LinearLayout) view.findViewById(R.id.ll_no_data);
        expandableHeightListView = (ExpandableHeightListView) view.findViewById(R.id.list_menu_coupon);
        Utility.writeSharedPreferences(getActivity(), "coupon_flag", "false");

        if (Utility.isNetworkAvaliable(getActivity())) {
            try {
                getCouponList getTask = new getCouponList(getActivity());
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class getCouponList extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;
        JSONArray jsonArray;
        JSONObject jsonObjectMessage;

        public getCouponList(final Context mContext) {
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
                    if (coupon_code.length > 0) {
                        expandableHeightListView.setVisibility(View.VISIBLE);
                        ll_no_data.setVisibility(View.GONE);
                        expandableHeightListView.setAdapter(new Coupon1ListAdapter(getActivity(), coupon_code, coupon_description, coupon_expire_date));
                    } else {
                        expandableHeightListView.setVisibility(View.GONE);
                        ll_no_data.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_COUPON_LIST;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", ""));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {

                    jsonArray = jObject.getJSONArray("coupon_data");
                    int lenth = jsonArray.length();

                    coupon_code = new String[lenth];
                    coupon_description = new String[lenth];
                    coupon_expire_date = new String[lenth];

                    for (int a = 0; a < lenth; a++) {
                        jsonObjectMessage = jsonArray.getJSONObject(a);

                        try {
                            coupon_code[a] = jsonObjectMessage.getString("coupon_code");
                            coupon_description[a] = jsonObjectMessage.getString("coupon_description");
                            coupon_expire_date[a] = jsonObjectMessage.getString("coupon_expire_date");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
