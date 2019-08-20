package com.decideme.client.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.adapter.WalletTopupAdapter;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.RandomString;
import com.decideme.client.attributes.RecyclerItemClickListener;
import com.decideme.client.attributes.Utility;
import com.paymaya.sdk.android.PayMayaConfig;
import com.paymaya.sdk.android.checkout.PayMayaCheckout;
import com.paymaya.sdk.android.checkout.PayMayaCheckoutCallback;
import com.paymaya.sdk.android.checkout.models.Buyer;
import com.paymaya.sdk.android.checkout.models.Checkout;
import com.paymaya.sdk.android.checkout.models.Contact;
import com.paymaya.sdk.android.checkout.models.Item;
import com.paymaya.sdk.android.checkout.models.RedirectUrl;
import com.paymaya.sdk.android.checkout.models.TotalAmount;
import com.paymaya.sdk.android.payment.PayMayaPayment;
import com.paymaya.sdk.android.payment.PayMayaPaymentException;
import com.paymaya.sdk.android.payment.models.Card;
import com.paymaya.sdk.android.payment.models.PaymentToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by vivek_shah on 9/11/17.
 */
public class Fragment_Wallet extends Fragment implements View.OnClickListener,
        PayMayaCheckoutCallback {

    View view;
    String cardNumber, cardCVV, cardType, cardMonth, cardYear;
    private TextView tv_wallet_amount, tv_note;
    private RecyclerView recyclerView;
    private EditText et_amount;
    private Button btn_add_amount;
    private String[] topup_amount;
    private String order_id, payment_status;
    // Pay Maya
    private PayMayaPayment payMayaPayment;
    private Card card;
    private PayMayaCheckout mPayMayaCheckout;
    private String requestReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PayMayaConfig.setEnvironment(PayMayaConfig.ENVIRONMENT_PRODUCTION);
        mPayMayaCheckout = new PayMayaCheckout(Constant.PAYMAYA_PUBLIC_KEY, this);

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        view = inflater.inflate(R.layout.fragment_wallet,
                container, false);

        init();

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (et_amount.getText().toString().equalsIgnoreCase("")) {
                            String value = topup_amount[position];
                            et_amount.setText(value);
                        } else {
                            et_amount.setText("");
                            String value = topup_amount[position];
                            et_amount.setText(value);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );

        return view;
    }

    public void init() {

        tv_wallet_amount = (TextView) view.findViewById(R.id.tv_wallet_amount);
        tv_note = (TextView) view.findViewById(R.id.tv_note);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_top_up);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        et_amount = (EditText) view.findViewById(R.id.et_money);
        btn_add_amount = (Button) view.findViewById(R.id.btn_add_money);
        btn_add_amount.setOnClickListener(this);

        if (Utility.isNetworkAvaliable(getActivity())) {
            try {
                getCardDetails getTask = new getCardDetails(getActivity());
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_money:
                if (!et_amount.getText().toString().equalsIgnoreCase("")) {
                    try {
                        Contact contact = new Contact("+63" + Utility.getAppPrefString(getActivity(), Constant.USER_MOBILE),
                                Utility.getAppPrefString(getActivity(), Constant.USER_EMAIL));
                        Buyer buyer = new Buyer(Utility.getAppPrefString(getActivity(), Constant.USER_NAME), "",
                                Utility.getAppPrefString(getActivity(), Constant.USER_NAME));
                        buyer.setContact(contact);

                        BigDecimal summaryTotal = BigDecimal.valueOf(0);
                        List itemsList = new ArrayList<>();
                        String currency = "PHP";

                        BigDecimal item1Amount = BigDecimal.valueOf(Double.parseDouble(et_amount.getText().toString()));
                        summaryTotal.add(item1Amount);
                        TotalAmount totalAmount = new TotalAmount(item1Amount, currency);
                        int quantity = 1;
                        Item item1 = new Item("DME", quantity, totalAmount);
                        itemsList.add(item1);

                        String successURL = "https://www.decidemejob.com/success";
                        String failedURL = "https://www.decidemejob.com/failed";
                        String canceledURL = "https://www.decidemejob.com/canceled";

                        RedirectUrl redirectUrl = new RedirectUrl(successURL, failedURL, canceledURL);
                        RandomString gen = new RandomString(5, ThreadLocalRandom.current());
                        requestReference = gen.nextString() + "_" + Utility.getTimeStamp();
                        Checkout checkout = new Checkout(totalAmount, buyer, itemsList, requestReference, redirectUrl);
                        Log.v("Request : ", checkout.toString());
                        mPayMayaCheckout.execute(getActivity(), checkout);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please enter amount", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPayMayaCheckout.onActivityResult(requestCode, resultCode, data);
//        Log.v("ID:", data.getStringExtra("checkoutId"));
        if (data != null) {
            requestReference = data.getStringExtra("checkoutId");
        }
    }

    @Override
    public void onCheckoutSuccess() {
        if (Utility.isNetworkAvaliable(getActivity())) {
            try {
                payment_status = "approved";
                addMoney getTask = new addMoney(getActivity());
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckoutCanceled() {
        et_amount.setText("");
        Utility.toast("Transaction canceled.", getActivity());
    }

    @Override
    public void onCheckoutFailure(String s) {
        et_amount.setText("");
        Utility.toast("Transaction failed.", getActivity());
    }

    @SuppressLint("StaticFieldLeak")
    public void onPaymentButtonClicked() {
        card = new Card("5123456789012346", "05", "19", "111");
        payMayaPayment = new PayMayaPayment(Constant.PAYMAYA_PUBLIC_KEY, card);

        new AsyncTask<Void, Void, PaymentToken>() {
            private String exceptionMessage;

            @Override
            protected PaymentToken doInBackground(Void... params) {
                try {
                    return payMayaPayment.getPaymentToken();
                } catch (PayMayaPaymentException e) {
                    exceptionMessage = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(PaymentToken paymentToken) {
                if (null == exceptionMessage) {
                    final String paymentTokenId = paymentToken.getPaymentTokenId();
                    Log.v("Token: ", paymentTokenId);
//                    new PaymentsTask().execute(paymentTokenId);
                    return;
                }

                Toast.makeText(getApplicationContext(), "Error: " + exceptionMessage,
                        Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    public class getCardDetails extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message, wallet_amount, note;
        JSONObject jsonObjectMessage;

        public getCardDetails(final Context mContext) {
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
                    note = note.replace("\\n ", "\n\n");
                    tv_note.setText(note);
                    if (!wallet_amount.equalsIgnoreCase("")) {
                        tv_wallet_amount.setText(wallet_amount);
                    } else {
                        tv_wallet_amount.setText("0.00");
                    }
                    recyclerView.setAdapter(new WalletTopupAdapter(getActivity(), topup_amount));

                } else {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_CARD_AND_WALLET;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(getActivity(), Constant.USER_ID)));
                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");
                note = jObject.getString("note");

                if (response.equalsIgnoreCase("true")) {
                    wallet_amount = jObject.getString("wallet_amount");

                    JSONArray walletArray = new JSONArray(jObject.getString("wallet_topup"));
                    int length = walletArray.length();

                    topup_amount = new String[length];

                    for (int a = 0; a < length; a++) {
                        jsonObjectMessage = walletArray.getJSONObject(a);
                        try {
                            topup_amount[a] = jsonObjectMessage.getString("topup_amount");
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

    public class addMoney extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message, amount;

        public addMoney(final Context mContext) {
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
                    et_amount.setText("");
                    if (!amount.equalsIgnoreCase("")) {
                        tv_wallet_amount.setText(amount);
                    } else {
                        tv_wallet_amount.setText("0.00");
                    }
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_ADD_MONEY;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(getActivity(), Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("amount", et_amount.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("order_id", requestReference));
                nameValuePairs.add(new BasicNameValuePair("payment_status", payment_status));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    amount = jObject.getString("wallet_amount");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class sendNonce extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        private String response, message;

        public sendNonce(final Context mContext) {
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
                    payment_status = "approved";
                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            addMoney getTask = new addMoney(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
            String webUrl = Constant.URL_PAYMENT1;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("creditCardType", cardType));
                nameValuePairs.add(new BasicNameValuePair("creditCardNumber", cardNumber));
                nameValuePairs.add(new BasicNameValuePair("expDateMonth", cardMonth));
                nameValuePairs.add(new BasicNameValuePair("expDateYear", cardYear));
                nameValuePairs.add(new BasicNameValuePair("cvv2Number", cardCVV));
                nameValuePairs.add(new BasicNameValuePair("amount", et_amount.getText().toString().trim()));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    order_id = jObject.getString("order_id");
                }

                return null;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class getToken extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        private String response, message;

        public getToken(final Context mContext) {
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
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = "https://pg-sandbox.paymaya.com/payments/v1/payment-tokens";
            try {

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("number", "5123456789012346");
                jsonObject.put("expMonth", "05");
                jsonObject.put("expYear", "2025");
                jsonObject.put("cvc", "111");

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("card", jsonObject.toString()));

                String response1 = Utility.postRequest1(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                return null;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
