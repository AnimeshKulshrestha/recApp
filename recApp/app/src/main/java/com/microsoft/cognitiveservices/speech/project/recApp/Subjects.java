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
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.cognitiveservices.speech.project.recApp.dialogFrag.SubjectDialog;
import com.microsoft.cognitiveservices.speech.project.recApp.models.CourseModel;
import com.microsoft.cognitiveservices.speech.project.recApp.models.StudiesModel;
import com.microsoft.cognitiveservices.speech.project.recApp.models.User;

import java.util.ArrayList;

public class Subjects extends AppCompatActivity implements SubjectDialog.SubjectDialogListener{

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Button addCourse;
    private FirebaseAuth auth;
    private AlertDialog.Builder builder;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private TeachingRVAdapter teachingRVAdapter;
    private StudyingRVAdapter studyingRVAdapter;
    private LinearLayoutManager linearLayoutManager;
    private String urldb;
    private ArrayList<StudiesModel> studiesModels;
    private ArrayList<CourseModel> courses;
    private ArrayList<CourseModel> courses_study;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);
        setSupportActionBar(findViewById(R.id.toolbar));

        auth = FirebaseAuth.getInstance();
        urldb = "https://recapp-9edb4-default-rtdb.asia-southeast1.firebasedatabase.app";
        database = FirebaseDatabase.getInstance(urldb);
        reference = database.getReference().child("Users").child(auth.getUid());
        builder = new AlertDialog.Builder(this);
        swipeRefreshLayout = findViewById(R.id.refresh_sub);
        linearLayoutManager = new LinearLayoutManager(this);
        addCourse = findViewById(R.id.add);
        recyclerView = findViewById(R.id.courses_rv);
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        courses = new ArrayList<CourseModel>();
        courses.clear();
        navigationView = findViewById(R.id.nav_view);
        if(savedInstanceState==null){
            getSupportActionBar().setTitle("Teaching");
            navigationView.setCheckedItem(R.id.taught);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.studied:
                        getSupportActionBar().setTitle("Studying");
                        addCourse.setVisibility(View.GONE);
                        courses.clear();
                        setStudying();
                        break;
                    case R.id.taught:
                        getSupportActionBar().setTitle("Teaching");
                        addCourse.setVisibility(View.VISIBLE);
                        courses.clear();
                        setTaught();
                        break;
                    case R.id.logout_nav:
                        builder.setTitle("Are you Sure?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent i = new Intent(Subjects.this,Login.class);
                                        startActivity(i);
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
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (navigationView.getCheckedItem().getItemId()){

                    case R.id.studied:
                        getSupportActionBar().setTitle("Studying");
                        addCourse.setVisibility(View.GONE);
                        courses.clear();
                        setStudying();
                        break;
                    case R.id.taught:
                        getSupportActionBar().setTitle("Teaching");
                        addCourse.setVisibility(View.VISIBLE);
                        courses.clear();
                        setTaught();
                        break;
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
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

    public void openDialog(){
        SubjectDialog subjectDialog = new SubjectDialog();
        subjectDialog.show(getSupportFragmentManager(),"Add Course");
    }

    @Override
    public void coursenameadd(String topic, Boolean save) {
        DatabaseReference reference;
        reference = database.getReference().child("Users").child(auth.getUid());
        String pushId = reference.child("Taught").push().getKey();

        if(save){
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    CourseModel course = new CourseModel();
                    course.setPushId(pushId);
                    course.setCourse(topic);
                    course.setInstructor(user);
                    database.getReference().child("Users").child(auth.getUid()).child("Taught").child(pushId).setValue(course);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("db_error","error in instructor");
                }
            });
            Intent intent = new Intent(Subjects.this, Add_student.class);
            intent.putExtra("push_Id",pushId);
            intent.putExtra("coursename",topic);
            startActivity(intent);
        }
    }

    public void setStudying(){
        courses_study = new ArrayList<CourseModel>();
        studyingRVAdapter = new StudyingRVAdapter(this,courses_study);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(studyingRVAdapter);
        studiesModels = new ArrayList<StudiesModel>();
        reference.child("Studies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studiesModels.clear();
                courses_study.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String inst_id = dataSnapshot.child("instructor").getValue(String.class),
                            subj_id = dataSnapshot.child("Subject_Id").getValue(String.class);
                    StudiesModel studies = new StudiesModel();
                    studies.setInst_id(inst_id);
                    studies.setSubject_Id(subj_id);
                    studiesModels.add(studies);
                }
                setStudyAdapter(courses_study);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void setStudyAdapter(ArrayList<CourseModel> courses_study){
        DatabaseReference ref_stu;
        for(StudiesModel study:studiesModels){
            ref_stu = database.getReference().child("Users").child(study.getInst_id()).child("Taught").child(study.getSubject_Id());
            ref_stu.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    CourseModel courseModel = snapshot.getValue(CourseModel.class);
                    if(courseModel!=null) {
                        courses_study.add(0, courseModel);
                        studyingRVAdapter.notifyDataSetChanged();
                    }
                    Log.e("test",courses_study.size()+"");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    public void setTaught(){
        reference.child("Taught").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courses.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    CourseModel courseModel = dataSnapshot.getValue(CourseModel.class);
                    if(courseModel!=null)
                        courses.add(0,courseModel);
                }
                teachingRVAdapter.notifyDataSetChanged();
                if(courses.size()==0){
                    findViewById(R.id.empty_sub).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.empty_sub).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("databaseerror",error.toString());
            }
        });

        teachingRVAdapter = new TeachingRVAdapter(this,courses);
        recyclerView.setAdapter(teachingRVAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.taught);
        getSupportActionBar().setTitle("Teaching");
        setTaught();
    }

}