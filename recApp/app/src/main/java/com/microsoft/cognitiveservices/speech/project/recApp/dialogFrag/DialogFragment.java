package com.microsoft.cognitiveservices.speech.project.recApp.dialogFrag;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.microsoft.cognitiveservices.speech.project.recApp.R;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DialogFragment extends AppCompatDialogFragment {

    private TextInputEditText topic;
    private DialogFragmentListener dialogFragmentListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            dialogFragmentListener = (DialogFragmentListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+"must implement dialogfragmentlistner");
        }
    }

    public boolean validate(String insert){
        if(insert==null || insert.equals("")){
            Toast.makeText(getContext(),"Field cannot be empty",Toast.LENGTH_LONG).show();
            return false;
        }else{
            return true;
        }
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.layout_add_topic,null);

        topic = view.findViewById(R.id.topictext);
        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp(System.currentTimeMillis()));

        topic.setText("Lecture on "+timestamp);
        builder.setView(view).setTitle("Set Title").setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String insert = topic.getText().toString().trim();
                if(validate(insert)) {
                    Boolean save = true;
                    dialogFragmentListener.applytext(insert, save);
                }else
                    onCreateDialog(savedInstanceState);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Boolean save = false;
                dialogFragmentListener.applytext(null,save);
            }
        });


        return builder.create();
    }

    public interface DialogFragmentListener{
        void applytext(String topic,Boolean save);
    }

}
