package com.microsoft.cognitiveservices.speech.project.recApp.dialogFrag;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.microsoft.cognitiveservices.speech.project.recApp.R;

public class LoadingDialog {
    Activity activity;
    AlertDialog alertDialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    public void startLoading(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.waiting_dialog,null));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }
    public void dismiss(){
        alertDialog.dismiss();
    }

}
