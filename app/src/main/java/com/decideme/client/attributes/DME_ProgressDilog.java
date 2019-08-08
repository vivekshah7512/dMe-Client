package com.decideme.client.attributes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ImageView;

import com.decideme.client.R;

public class DME_ProgressDilog extends ProgressDialog {
    GifView gv;
    ImageView Progress_iv;

    public static ProgressDialog ctor(Context context) {
        DME_ProgressDilog dialog = new DME_ProgressDilog(context);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    public DME_ProgressDilog(Context context) {
        super(context);
    }

    public DME_ProgressDilog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_dilog);

        try {
            gv = (GifView) findViewById(R.id.progress_iv);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}

