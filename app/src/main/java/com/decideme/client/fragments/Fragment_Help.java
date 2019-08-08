package com.decideme.client.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.decideme.client.R;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.Utility;

/**
 * Created by vivek_shah on 9/11/17.
 */
public class Fragment_Help extends Fragment {

    View view;
    private WebView webView;
    private DME_ProgressDilog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        view = inflater.inflate(R.layout.fragment_help,
                container, false);

            init();

            webView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }
            });

        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void init() {

        webView = (WebView) view.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        mProgressDialog = new DME_ProgressDilog(getActivity());
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        webView.loadUrl(Utility.getAppPrefString(getActivity(), "help_url"));

    }
}
