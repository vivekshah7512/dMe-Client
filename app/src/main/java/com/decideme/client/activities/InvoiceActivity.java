package com.decideme.client.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.RandomString;
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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class InvoiceActivity extends Activity implements View.OnClickListener,
        PayMayaCheckoutCallback {

    String cardNumber, cardCVV, cardType, cardMonth, cardYear;
    private Context mContext;
    private TextView tv_base, tv_tax, tv_hourly, tv_wallet, tv_method, tv_total, tv_coupon_message;
    private Button btn_pay;
    private ImageView img_back;
    private TextView tv_coupon_code;
    private RelativeLayout rl_coupon_applied;
    private TextView tv_coupon_applied;
    private ImageView img_clear;
    private String total_amount, order_id, payment_status;
    private Dialog dialog;
    private RatingBar ratingBar1;
    private EditText et_comments;
    private String worker_id, worker_name, worker_rate, worker_total_review, worker_profile;
    // Pay Maya
    private PayMayaCheckout mPayMayaCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PayMayaConfig.setEnvironment(PayMayaConfig.ENVIRONMENT_PRODUCTION);
        mPayMayaCheckout = new PayMayaCheckout(Constant.PAYMAYA_PUBLIC_KEY, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_invoice);

        initUI();
    }

    public void initUI() {
        mContext = InvoiceActivity.this;

        tv_base = (TextView) findViewById(R.id.tv_base_fee);
        tv_coupon_code = (TextView) findViewById(R.id.tv_coupon_code);
        tv_coupon_code.setOnClickListener(this);
        tv_tax = (TextView) findViewById(R.id.tv_tax_fee);
        tv_hourly = (TextView) findViewById(R.id.tv_hourly_fee);
        tv_wallet = (TextView) findViewById(R.id.tv_wallet_fee);
        tv_method = (TextView) findViewById(R.id.tv_payment_method);
        tv_total = (TextView) findViewById(R.id.tv_total);
        tv_coupon_message = (TextView) findViewById(R.id.tv_coupon_message);
        btn_pay = (Button) findViewById(R.id.btn_pay);
        btn_pay.setOnClickListener(this);

        rl_coupon_applied = (RelativeLayout) findViewById(R.id.rl_coupon);
        tv_coupon_applied = (TextView) findViewById(R.id.tv_coupon_applied);
        img_clear = (ImageView) findViewById(R.id.img_coupon_clear);
        img_clear.setOnClickListener(this);

        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(this);

        if (Utility.isNetworkAvaliable(mContext)) {
            try {
                getInvoiceDetails getTask = new getInvoiceDetails(mContext);
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_coupon_clear:
                if (Utility.isNetworkAvaliable(mContext)) {
                    try {
                        getInvoiceDetails getTask = new getInvoiceDetails(mContext);
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.tv_coupon_code:
                Intent intent_coupon = new Intent(InvoiceActivity.this, CouponActivity.class);
                startActivity(intent_coupon);
                break;
            case R.id.btn_pay:
                Utility.writeSharedPreferences(getApplicationContext(), "process_type", "none");
                if (btn_pay.getText().toString().equalsIgnoreCase("Pay via Cash")) {
                    order_id = "";
                    payment_status = "approved";

                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            payAmount getTask = new payAmount(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (btn_pay.getText().toString().equalsIgnoreCase("Pay via Wallet")) {
                    order_id = "";
                    payment_status = "approved";

                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            payAmount getTask = new payAmount(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Contact contact = new Contact("+63" + Utility.getAppPrefString(mContext, Constant.USER_MOBILE),
                                Utility.getAppPrefString(mContext, Constant.USER_EMAIL));
                        Buyer buyer = new Buyer(Utility.getAppPrefString(mContext, Constant.USER_NAME), "",
                                Utility.getAppPrefString(mContext, Constant.USER_NAME));
                        buyer.setContact(contact);

                        BigDecimal summaryTotal = BigDecimal.valueOf(0);
                        List itemsList = new ArrayList<>();
                        String currency = "PHP";

                        BigDecimal item1Amount = BigDecimal.valueOf(Double.parseDouble(total_amount));
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
                        order_id = gen.nextString() + "_" + Utility.getTimeStamp();
                        Checkout checkout = new Checkout(totalAmount, buyer, itemsList, order_id, redirectUrl);
                        Log.v("Request : ", checkout.toString());
                        mPayMayaCheckout.execute(InvoiceActivity.this, checkout);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.img_back:
                if (Utility.getAppPrefString(mContext, "sc_flag").equalsIgnoreCase("1")) {
                    Intent intent = new Intent(InvoiceActivity.this, ScheduleActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(InvoiceActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPayMayaCheckout.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            order_id = data.getStringExtra("checkoutId");
        }
    }

    // Invoice API

    @Override
    protected void onResume() {
        super.onResume();
        if (Utility.getAppPrefString(mContext, "coupon_flag").equalsIgnoreCase("1")) {
            rl_coupon_applied.setVisibility(View.VISIBLE);
            tv_coupon_code.setVisibility(View.GONE);
            tv_coupon_applied.setText(Utility.getAppPrefString(mContext, "message"));
            tv_base.setText("P " + Utility.getAppPrefString(mContext, "base_fee"));
            tv_tax.setText("P " + Utility.getAppPrefString(mContext, "tax"));
            tv_hourly.setText("P " + Utility.getAppPrefString(mContext, "hourly_fee"));
            tv_wallet.setText("P " + Utility.getAppPrefString(mContext, "wallet_deduction"));
            tv_total.setText("P " + Utility.getAppPrefString(mContext, "total_amount"));
        }
    }

    @Override
    public void onCheckoutSuccess() {
        payment_status = "approved";
        if (Utility.isNetworkAvaliable(mContext)) {
            try {
                payAmount getTask = new payAmount(mContext);
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckoutCanceled() {
        Utility.toast("Transaction canceled.", mContext);
    }

    @Override
    public void onCheckoutFailure(String message) {
        Utility.toast("Transaction failed.", mContext);
    }

    public class getInvoiceDetails extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;
        String payment_type;
        String hourly_fee;
        String wallet_deduction;
        String base_fee;
        String tax;
        String is_wallet;

        public getInvoiceDetails(final Context mContext) {
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
                    rl_coupon_applied.setVisibility(View.GONE);
                    tv_coupon_code.setVisibility(View.VISIBLE);
                    tv_base.setText("P " + base_fee);
                    tv_tax.setText("P " + tax);
                    tv_hourly.setText("P " + hourly_fee);
                    tv_wallet.setText("P " + wallet_deduction);
                    tv_method.setText(payment_type);
                    tv_total.setText("P " + total_amount);

                    if (is_wallet.equalsIgnoreCase("true")) {
                        btn_pay.setText("Pay via Wallet");
                    } else {
                        if (payment_type.equalsIgnoreCase("cash")) {
                            btn_pay.setText("Pay via Cash");
                            tv_coupon_code.setVisibility(View.GONE);
                        } else {
                            btn_pay.setText("Pay");
                            tv_coupon_code.setVisibility(View.VISIBLE);
                        }
                    }

                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_INVOICE;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", Utility.getAppPrefString(mContext, "invoice_s_id")));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    payment_type = jObject.getString("paymnet_type");
                    hourly_fee = jObject.getString("hourly_fee");
                    wallet_deduction = jObject.getString("wallet_deduction");
                    total_amount = jObject.getString("total_amount");
                    base_fee = jObject.getString("base_fee");
                    tax = jObject.getString("tax");
                    is_wallet = jObject.getString("is_wallet");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class payAmount extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;

        public payAmount(final Context mContext) {
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
                    dialog = new Dialog(mContext);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(
                            new ColorDrawable(Color.TRANSPARENT));
                    dialog.setContentView(R.layout.dialog_worker_rating);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);

                    ImageView img_profile = (ImageView) dialog.findViewById(R.id.img_ar_profile);
                    TextView tv_name = (TextView) dialog.findViewById(R.id.tv_ar_name);
                    RatingBar ratingBar = (RatingBar) dialog.findViewById(R.id.rating_ar);
                    TextView tv_total_review = (TextView) dialog.findViewById(R.id.tv_rating_ar_value);

                    try {
                        tv_name.setText(worker_name);
                        ratingBar.setRating(Float.parseFloat(worker_rate));
                        tv_total_review.setText("(" + worker_total_review + ")");
                        if (!worker_profile.equalsIgnoreCase("")) {
                            Glide.with(mContext).load(worker_profile)
                                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                                    .thumbnail(0.5f)
                                    .into(img_profile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ratingBar1 = (RatingBar) dialog.findViewById(R.id.rate_worker);
                    et_comments = (EditText) dialog.findViewById(R.id.et_comments);
                    Button btn_submit = (Button) dialog.findViewById(R.id.btn_add_rating);

                    btn_submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ratingBar1.getRating() > 0) {
                                if (Utility.isNetworkAvaliable(mContext)) {
                                    try {
                                        addRating getTask = new addRating(mContext);
                                        getTask.execute();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Toast.makeText(mContext, "Please give rating", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                    Window window = dialog.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();

                    wlp.gravity = Gravity.CENTER;
                    wlp.width = ActionBar.LayoutParams.MATCH_PARENT;
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setAttributes(wlp);

                    dialog.show();
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
            String webUrl = Constant.URL_PAYMENT;

            try {
                // avinashvaghasiya@yahoo.in 12345678
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", Utility.getAppPrefString(mContext, "invoice_s_id")));
                if (btn_pay.getText().toString().equalsIgnoreCase("Pay via Wallet"))
                    nameValuePairs.add(new BasicNameValuePair("payment_type", "wallet"));
                else
                    nameValuePairs.add(new BasicNameValuePair("payment_type", tv_method.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("amount", total_amount));
                nameValuePairs.add(new BasicNameValuePair("order_id", order_id));
                nameValuePairs.add(new BasicNameValuePair("payment_status", payment_status));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    worker_id = jObject.getString("worker_id");
                    worker_name = jObject.getString("worker_name");
                    worker_rate = jObject.getString("worker_rate");
                    worker_profile = jObject.getString("worker_profile");
                    worker_total_review = jObject.getString("worker_total_review");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class addRating extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;

        public addRating(final Context mContext) {
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
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    Intent intent = new Intent(InvoiceActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_ADD_RATING;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", Utility.getAppPrefString(mContext, "invoice_s_id")));
                nameValuePairs.add(new BasicNameValuePair("rating", ratingBar1.getRating() + ""));
                nameValuePairs.add(new BasicNameValuePair("comment", et_comments.getText().toString()));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

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
                            payAmount getTask = new payAmount(mContext);
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
                nameValuePairs.add(new BasicNameValuePair("amount", total_amount));

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
}