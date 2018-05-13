package com.hteck.playtube.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hteck.playtube.fragment.ProgessDialogFragment;

public class BaseActivity extends AppCompatActivity {
    private ProgessDialogFragment progressDialog;
    private static final String DIALOG_TAG = "dialog";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.progressDialog = new ProgessDialogFragment();
    }

    public void showProgessDialog() {
        progressDialog.show(getFragmentManager(), DIALOG_TAG);
    }

    public void dismissProgress() {
        progressDialog.dismiss();
    }
}
