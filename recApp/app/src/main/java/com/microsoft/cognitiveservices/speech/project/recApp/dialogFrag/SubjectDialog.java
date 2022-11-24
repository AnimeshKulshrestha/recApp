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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.cognitiveservices.speech.project.recApp.R;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class SubjectDialog extends AppCompatDialogFragment {
    private TextInputEditText topic;
    private SubjectDialog.SubjectDialogListener subjectDialogListener;
    private String urldb;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private long subcount;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            subjectDialogListener = (SubjectDialog.SubjectDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+"must implement subjectdialoglistener");
        }
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.layout_add_topic,null);

        auth = FirebaseAuth.getInstance();
        urldb = "https://recapp-9edb4-default-rtdb.asia-southeast1.firebasedatabase.app";
        database = FirebaseDatabase.getInstance(urldb);
        reference = database.getReference().child("Users").child(auth.getUid()).child("Taught");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subcount = snapshot.getChildrenCount();
                topic = view.findViewById(R.id.topictext);
                topic.setText("Course "+subcount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        builder.setView(view).setTitle("Course Name").setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String insert = topic.getText().toString().trim();
                if(!validate(insert))
                    onCreateDialog(savedInstanceState);
                else {
                    Boolean save = true;
                    subjectDialogListener.coursenameadd(insert, save);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Boolean save = false;
                subjectDialogListener.coursenameadd(null,save);
            }
        });


        return builder.create();
    }

    public interface SubjectDialogListener{
        void coursenameadd(String topic,Boolean save);
    }

    public boolean validate(String insert){
        if(insert==null || insert.equals("")){
            Toast.makeText(getContext(),"Field cannot be empty",Toast.LENGTH_LONG).show();
            return false;
        }else{
            return true;
        }
    }
}
