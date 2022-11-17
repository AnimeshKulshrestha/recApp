package com.microsoft.cognitiveservices.speech.project.recApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.microsoft.cognitiveservices.speech.project.recApp.models.Transmodel;
import com.microsoft.cognitiveservices.speech.project.recApp.models.User;

import java.util.ArrayList;
import java.util.Locale;

public class Transcriptions extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private TransReyclerAdaptor transReyclerAdaptor;
    private Student_list_rv_adaptor student_list_rv_adaptor;
    private ArrayList<Transmodel> files;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private DatabaseReference reference;
    private String state,pushId,coursename,urldb,user_id;
    private AlertDialog.Builder builder;
    private ArrayList<User> students;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView notrans;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcriptions);
        setSupportActionBar(findViewById(R.id.trans_toolbar));
        recyclerView = findViewById(R.id.recyclerView);
        notrans = findViewById(R.id.no_trans);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        swipeRefreshLayout = findViewById(R.id.refresh_trans);
        builder = new AlertDialog.Builder(this);
        urldb = "https://recapp-9edb4-default-rtdb.asia-southeast1.firebasedatabase.app";
        database = FirebaseDatabase.getInstance(urldb);
        state = getIntent().getStringExtra("c_state");
        pushId = getIntent().getStringExtra("pushId");
        coursename = getIntent().getStringExtra("coursename");
        user_id = getIntent().getStringExtra("User_id");
        reference = database.getReference().child("Users").child(user_id)
                .child("Taught").child(pushId).child("transcriptions");
        files = new ArrayList<Transmodel>();
        students = new ArrayList<User>();
        searchView = findViewById(R.id.search_trans);
        getSupportActionBar().setTitle(coursename);
        drawerLayout = findViewById(R.id.trans_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        if(state.equals("Taught"))
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.trans_nav);
        navigationView.setCheckedItem(R.id.transcriptions);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.transcriptions:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        setList();
                        break;
                    case R.id.remove_student:
                        Intent intent = new Intent(Transcriptions.this, Remove_student.class);
                        intent.putExtra("push_Id",pushId);
                        intent.putExtra("coursename",coursename);
                        startActivity(intent);
                        break;
                    case R.id.list_stu:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        showStudents();
                        break;
                    case R.id.add_student:
                        Intent i = new Intent(Transcriptions.this, Add_student.class);
                        i.putExtra("push_Id",pushId);
                        i.putExtra("coursename",coursename);
                        startActivity(i);
                        break;
                    case R.id.new_transcription:
                        Intent i1 = new Intent(Transcriptions.this,MainActivity.class);
                        i1.putExtra("pushId",pushId);
                        startActivity(i1);
                        break;
                    case R.id.delete_course:
                            builder.setTitle("Are you Sure?")
                            .setMessage("You won't recover any data")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                                                String url = dataSnapshot.child("url").getValue(String.class);
                                                storageReference = firebaseStorage.getReferenceFromUrl(url);
                                                storageReference.delete();
                                            }
                                            database.getReference().child("Users").child(firebaseAuth.getUid())
                                                    .child("Taught").child(pushId).removeValue();
                                            Intent intent1 = new Intent(Transcriptions.this,Subjects.class);
                                            startActivity(intent1);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                        AlertDialog alertDialog =  builder.create();
                        alertDialog.show();
                        break;
                }
                return true;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (navigationView.getCheckedItem().getItemId()){
                    case R.id.transcriptions:
                        setList();
                        break;
                    case R.id.list_stu:
                        showStudents();
                        break;
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        if(searchView!=null){
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    search_trans(s);
                    return true;
                }
            });
        }
    }

    public void search_trans(String s){
        ArrayList<Transmodel> filtered = new ArrayList<Transmodel>();
        for(Transmodel file:files){
            if(file.getFilename().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT)))
                filtered.add(file);
        }
        transReyclerAdaptor = new TransReyclerAdaptor(this,filtered,pushId,state);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(transReyclerAdaptor);
    }

    public void showStudents(){
        student_list_rv_adaptor = new Student_list_rv_adaptor(Transcriptions.this,students);
        database.getReference().child("Users")
                .child(user_id).child("Taught")
                .child(pushId).child("Students").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        students.clear();
                        for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                            students.add(dataSnapshot.getValue(User.class));
                        }
                        if(students.size()==0){
                            notrans.setVisibility(View.VISIBLE);
                            notrans.setText("No Students added");
                        }else
                            notrans.setVisibility(View.GONE);
                        student_list_rv_adaptor.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(student_list_rv_adaptor);
    }

    public void setList(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                files.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Transmodel transmodel = dataSnapshot.getValue(Transmodel.class);
                    files.add(0,transmodel);
                }
                if(files.size()==0){
                    TextView notrans = findViewById(R.id.no_trans);
                    notrans.setVisibility(View.VISIBLE);
                    if(state.equals("Taught"))
                        notrans.setText("No Transcriptions present add more");
                    else
                        notrans.setText("No Transcriptions present");
                }else
                    notrans.setVisibility(View.GONE);
                transReyclerAdaptor.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("databaseerror",error.toString());
            }
        });
        transReyclerAdaptor = new TransReyclerAdaptor(this,files,pushId,state);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(transReyclerAdaptor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.transcriptions);
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        files.clear();
        setList();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}