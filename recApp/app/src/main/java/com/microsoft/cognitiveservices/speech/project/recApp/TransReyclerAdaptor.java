package com.microsoft.cognitiveservices.speech.project.recApp;


import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.microsoft.cognitiveservices.speech.project.recApp.models.Transmodel;

import java.util.ArrayList;

public class TransReyclerAdaptor extends RecyclerView.Adapter<TransReyclerAdaptor.viewHolder>{
    private Context context;
    private ArrayList<Transmodel> arrTrans;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String urldb,state;
    private AlertDialog.Builder builder;


    public TransReyclerAdaptor(Context context, ArrayList<Transmodel> arrTrans,String pushId,String state) {
        this.setContext(context);
        this.arrTrans = arrTrans;
        this.state = state;
        builder = new AlertDialog.Builder(context);
        urldb = "https://recapp-9edb4-default-rtdb.asia-southeast1.firebasedatabase.app";
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance(urldb);
        reference = database.getReference().child("Users").child(firebaseAuth.getUid())
                .child("Taught").child(pushId).child("transcriptions");
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.transcription_rv , parent , false);
        return new viewHolder(view);
    }

    @Override
    public int getItemCount() {
        return getArrTrans().size();
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Transmodel trans = getArrTrans().get(position);
        if(state=="Studies")
            holder.delete.setVisibility(View.GONE);
        holder.filename.setText(trans.getFilename());
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBuilder().setTitle("Download "+trans.getFilename()+".txt ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                Uri uri = Uri.parse(trans.getUrl());
                                DownloadManager.Request request = new DownloadManager.Request(uri);
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                                request.setDestinationInExternalFilesDir(getContext(), Environment.DIRECTORY_DOWNLOADS, trans.getFilename()+".txt");
                                manager.enqueue(request);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                AlertDialog alertDialog = getBuilder().create();
                alertDialog.show();
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBuilder().setTitle("Are you Sure?")
                        .setMessage("You won't recover any data")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getReference().child(trans.getUnique_id()).child("url").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    String url = task.getResult().getValue(String.class);
                                                    storageReference = getFirebaseStorage().getReferenceFromUrl(url);
                                                    getStorageReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            getReference().child(trans.getUnique_id()).removeValue();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                getReference().child(trans.getUnique_id()).removeValue();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(context,trans.getUnique_id(),Toast.LENGTH_LONG).show();
                            }
                        });
                AlertDialog alertDialog =  getBuilder().create();
                alertDialog.show();
            }
        });
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<Transmodel> getArrTrans() {
        return arrTrans;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseStorage getFirebaseStorage() {
        return firebaseStorage;
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public DatabaseReference getReference() {
        return reference;
    }

    public String getUrldb() {
        return urldb;
    }

    public AlertDialog.Builder getBuilder() {
        return builder;
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        TextView filename;
        ImageView delete,download;
        public viewHolder(@NonNull View itemView){
            super(itemView);
            download = itemView.findViewById(R.id.download_trans);
            delete = itemView.findViewById(R.id.delete_trans);
            filename = itemView.findViewById(R.id.filename);
        }
    }
}
