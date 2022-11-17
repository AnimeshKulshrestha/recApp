package com.microsoft.cognitiveservices.speech.project.recApp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.cognitiveservices.speech.project.recApp.models.User;

import java.util.ArrayList;

public class Student_list_rv_adaptor extends RecyclerView.Adapter<Student_list_rv_adaptor.viewHolder> {
    Context context;
    ArrayList<User> student;


    public Student_list_rv_adaptor(Context context, ArrayList<User> student) {
        this.context = context;
        this.student = student;
    }

    @NonNull
    @Override
    public Student_list_rv_adaptor.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_rv, parent, false);
        return new viewHolder(view);
    }

    @Override
    public int getItemCount() {
        return student.size();
    }

    @Override
    public void onBindViewHolder(@NonNull Student_list_rv_adaptor.viewHolder holder, int position) {
        User user = student.get(position);
        holder.email.setText(user.getEmail());
        holder.username.setText(user.getUsername());
        holder.add_stu.setVisibility(View.GONE);
    }
    public class viewHolder extends RecyclerView.ViewHolder {

        TextView username;
        TextView email;
        Button add_stu;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            add_stu = itemView.findViewById(R.id.stu_rv);
            email = itemView.findViewById(R.id.student_mail);
            username = itemView.findViewById(R.id.StudentName);
        }
    }
}
