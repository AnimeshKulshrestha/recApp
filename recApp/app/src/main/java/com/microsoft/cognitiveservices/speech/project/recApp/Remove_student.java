package com.microsoft.cognitiveservices.speech.project.recApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.cognitiveservices.speech.project.recApp.models.User;

import java.util.ArrayList;
import java.util.Locale;

public class Remove_student extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Remove_stu_Adapter remove_stu_adapter;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String urldb;
    private ArrayList<User> students;
    private String sub_Id;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_student);
        setSupportActionBar(findViewById(R.id.toolbar3));

        students = new ArrayList<User>();
        recyclerView = findViewById(R.id.stu_rv_rem);
        urldb = "https://recapp-9edb4-default-rtdb.asia-southeast1.firebasedatabase.app";
        sub_Id = getIntent().getStringExtra("push_Id");
        swipeRefreshLayout = findViewById(R.id.refresh_rem_stu);
        searchView = findViewById(R.id.search_rem_stu);

        String coursename = getIntent().getStringExtra("coursename");
        getSupportActionBar().setTitle(coursename);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(urldb);
        reference = database.getReference();

        findViewById(R.id.cnf_stu_rem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Remove_student.this, Subjects.class);
                startActivity(intent);
            }
        });
        setlist();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setlist();
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
                    search_rem(s);
                    return true;
                }
            });
        }
    }

    public void search_rem(String s){
        ArrayList<User> filtered = new ArrayList<User>();
        for(User student:students){
            if(student.getUsername().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))
                    || student.getEmail().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))
                    || student.getFirstname().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))
                    || student.getLastname().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT)))
                filtered.add(student);
        }
        remove_stu_adapter = new Remove_stu_Adapter(this, students, sub_Id, auth.getUid());
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(remove_stu_adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }


    public void setlist(){
        reference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                students.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (!user.getUserId().equals(auth.getUid()))
                        students.add(user);
                }
                remove_stu_adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("db_error", error.toString());
            }
        });

        remove_stu_adapter = new Remove_stu_Adapter(this, students, sub_Id, auth.getUid());
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(remove_stu_adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
}