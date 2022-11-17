//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
package com.microsoft.cognitiveservices.speech.project.recApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.project.recApp.dialogFrag.DialogFragment;
import com.microsoft.cognitiveservices.speech.project.recApp.dialogFrag.LoadingDialog;
import com.microsoft.cognitiveservices.speech.project.recApp.models.Transmodel;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity implements DialogFragment.DialogFragmentListener {
    //
    // Configuration for speech recognition
    //

    // Replace below with your own subscription key
    private static final String SpeechSubscriptionKey = "5fa774581bf441b3abf5334bc2e370c7";
    // Replace below with your own service region (e.g., "westus").
    private static final String SpeechRegion = "eastus";

    //
    // Configuration for intent recognition
    //

//    // Replace below with your own Language Understanding subscription key
//    // The intent recognition service calls the required key 'endpoint key'.
//    private static final String LanguageUnderstandingSubscriptionKey = "YourLanguageUnderstandingSubscriptionKey";
//    // Replace below with the deployment region of your Language Understanding application
//    private static final String LanguageUnderstandingServiceRegion = "YourLanguageUnderstandingServiceRegion";
//    // Replace below with the application ID of your Language Understanding application
//    private static final String LanguageUnderstandingAppId = "YourLanguageUnderstandingAppId";
//    // Replace below with your own Keyword model file, kws.table model file is configured for "Computer" keyword
//    private static final String KwsModelFile = "kws.table";

    private TextView recognizedTextView;

    private Button recognizeContinuousButton;
    private AlertDialog.Builder builder;
    private DialogFragment dialogFragment;
    private FirebaseDatabase database;
    private String urldb,filename,pushId;
    private Boolean saving;
    private FirebaseStorage storage;
    private StorageReference reference;
    private FirebaseAuth auth;
    private LoadingDialog loadingDIalog;

    private MicrophoneStream microphoneStream;
    private MicrophoneStream createMicrophoneStream() {
        this.releaseMicrophoneStream();

        microphoneStream = new MicrophoneStream();
        return microphoneStream;
    }
    private void releaseMicrophoneStream() {
        if (microphoneStream != null) {
            microphoneStream.close();
            microphoneStream = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recognizedTextView = findViewById(R.id.recognizedText);
        recognizedTextView.setMovementMethod(new ScrollingMovementMethod());
        recognizeContinuousButton = findViewById(R.id.buttonRecognizeContinuous);
        urldb = "https://recapp-9edb4-default-rtdb.asia-southeast1.firebasedatabase.app";
        builder = new AlertDialog.Builder(this);
        pushId = getIntent().getStringExtra("pushId");
        dialogFragment = new DialogFragment();

        loadingDIalog = new LoadingDialog(MainActivity.this);
        database = FirebaseDatabase.getInstance(urldb);
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        reference = storage.getReference().child(auth.getUid()).child(pushId);

        // Initialize SpeechSDK and request required permissions.
        try {
            // a unique number within the application to allow
            // correlating permission request responses with the request.
            int permissionRequestId = 5;

            // Request permissions needed for speech recognition
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, INTERNET, READ_EXTERNAL_STORAGE}, permissionRequestId);
        } catch (Exception ex) {
            Log.e("SpeechSDK", "could not init sdk, " + ex.toString());
            recognizedTextView.setText("Could not initialize: " + ex.toString());
        }

        // create config
        final SpeechConfig speechConfig;
        try {
            speechConfig = SpeechConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);
        } catch (Exception ex) {
            Log.e("failed sub",ex.getMessage());
            displayException(ex);
            return;
        }
        recognizeContinuousButton.setOnClickListener(new View.OnClickListener() {
            private static final String logTag = "reco 3";
            private boolean continuousListeningStarted = false;
            private SpeechRecognizer reco = null;
            private AudioConfig audioInput = null;
            private String buttonText = "";
            private ArrayList<String> content = new ArrayList<>();

            @Override
            public void onClick(final View view) {
                final Button clickedButton = (Button) view;
                disableButtons();
                if (continuousListeningStarted) {
                    if (reco != null) {
                        final Future<Void> task = reco.stopContinuousRecognitionAsync();
                        setOnTaskCompletedListener(task, result -> {
                            Log.i(logTag, "Continuous recognition stopped.");
                            Log.v("sign","Loadding text into file");
                            MainActivity.this.runOnUiThread(() -> {
                                clickedButton.setText(buttonText);
                            });
                            enableButtons();
                            continuousListeningStarted = false;
                        });
                        openDialog();
                    } else {
                        continuousListeningStarted = false;
                    }

                    return;
                }

                clearTextBox();

                try {
                    content.clear();
                    audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
                    reco = new SpeechRecognizer(speechConfig, audioInput);

                    reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Intermediate result received: " + s);
                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                        content.remove(content.size() - 1);
                    });

                    reco.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Final result received: " + s);
                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                    });

                    final Future<Void> task = reco.startContinuousRecognitionAsync();
                    setOnTaskCompletedListener(task, result -> {
                        continuousListeningStarted = true;
                        MainActivity.this.runOnUiThread(() -> {
                            buttonText = clickedButton.getText().toString();
                            clickedButton.setText("Stop");
                            clickedButton.setEnabled(true);
                        });
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    displayException(ex);
                }
            }
        });

    }

    private void displayException(Exception ex) {
        recognizedTextView.setText(ex.getMessage() + System.lineSeparator() + TextUtils.join(System.lineSeparator(), ex.getStackTrace()));
    }

    private void clearTextBox() {
        AppendTextLine("", true);
    }

    private void setRecognizedText(final String s) {
        AppendTextLine(s, true);
    }

    private void AppendTextLine(final String s, final Boolean erase) {
        MainActivity.this.runOnUiThread(() -> {
            if (erase) {
                recognizedTextView.setText(s);
            } else {
                String txt = recognizedTextView.getText().toString();
                recognizedTextView.setText(txt + System.lineSeparator() + s);
            }
        });
    }

    private void disableButtons() {
        MainActivity.this.runOnUiThread(() -> {
            recognizeContinuousButton.setEnabled(false);
        });
    }

    private void enableButtons() {
        MainActivity.this.runOnUiThread(() -> {
            recognizeContinuousButton.setEnabled(true);
        });
    }

    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
        s_executorService.submit(() -> {
            T result = task.get();
            listener.onCompleted(result);
            return null;
        });
    }

    @Override
    public void applytext(String topic, Boolean save) {
        filename = topic;
        saving = save;
        if(saving)
            loadText(filename);
        else{
            alert_cancel();
        }
    }


    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }


    private static ExecutorService s_executorService;
    static {
        s_executorService = Executors.newCachedThreadPool();
    }

    private void loadText(String title){
        String reco = recognizedTextView.getText().toString();
        loadingDIalog.startLoading();
        reference.child(title+".txt").putBytes(reco.getBytes()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.child(title+".txt").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DatabaseReference dbref = database.getReference().child("Users").child(auth.getUid()).child("Taught")
                                .child(pushId).child("transcriptions");
                        String t_uId = dbref.push().getKey();
                        Transmodel transmodel = new Transmodel(title,uri.toString(),t_uId);
                        database.getReference().child("Users").child(auth.getUid()).child("Taught")
                                .child(pushId).child("transcriptions")
                                .child(t_uId).setValue(transmodel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        loadingDIalog.dismiss();
                                        Toast.makeText(MainActivity.this,"File uploaded",Toast.LENGTH_LONG).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingDIalog.dismiss();
                                        Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDIalog.dismiss();
                Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void openDialog(){
        dialogFragment.show(getSupportFragmentManager(),"Set Title");
        dialogFragment.setCancelable(false);
    }

    public void alert_cancel(){
        builder.setTitle("Are you Sure?")
                .setMessage("The transcription will be lost")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this,"You cancelled the save",Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openDialog();
                    }
                });
        builder.setCancelable(false);
        AlertDialog alertDialog =  builder.create();
        alertDialog.show();
    }
    @Override
    public void onBackPressed() {
        if (dialogFragment.isResumed()) {
            alert_cancel();
        } else
            super.onBackPressed();
    }

}
