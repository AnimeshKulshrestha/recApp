package com.microsoft.cognitiveservices.speech.project.recApp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.microsoft.cognitiveservices.speech.project.recApp.models.CourseModel;

import java.util.ArrayList;

public class TeachingRVAdapter extends RecyclerView.Adapter<TeachingRVAdapter.viewHolder> {
    Context context;
    ArrayList<CourseModel> arrCourse;
    FirebaseAuth firebaseAuth;


    public TeachingRVAdapter(Context context, ArrayList<CourseModel> arrCourse) {
        this.context = context;
        this.arrCourse = arrCourse;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public TeachingRVAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.courses_rv, parent, false);
        return new viewHolder(view);
    }

    @Override
    public int getItemCount() {
        return arrCourse.size();
    }

    @Override
    public void onBindViewHolder(@NonNull TeachingRVAdapter.viewHolder holder, int position) {
        CourseModel courseModel = arrCourse.get(position);
        holder.course.setText(courseModel.getCourse());
        holder.instructor.setText(courseModel.getInstructor().getUsername());
        holder.courseBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,Transcriptions.class);
                intent.putExtra("pushId",courseModel.getPushId());
                intent.putExtra("c_state","Taught");
                intent.putExtra("coursename",courseModel.getCourse());
                intent.putExtra("User_id",firebaseAuth.getUid());
                context.startActivity(intent);
            }
        });
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        TextView course;
        TextView instructor;
        CardView courseBase;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            courseBase = itemView.findViewById(R.id.courseBase);
            course = itemView.findViewById(R.id.course_name);
            instructor = itemView.findViewById(R.id.instructor_course);
        }
    }
}
