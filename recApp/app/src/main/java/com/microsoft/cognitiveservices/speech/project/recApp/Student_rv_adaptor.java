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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.cognitiveservices.speech.project.recApp.models.User;

import java.util.ArrayList;

public class Student_rv_adaptor extends RecyclerView.Adapter<Student_rv_adaptor.viewHolder> {
    Context context;
    ArrayList<User> student;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference reference,reference_stu;
    String urldb,subjectId,inst_id;


    public Student_rv_adaptor(Context context, ArrayList<User> student,String subjectId,String inst_id) {
        this.context = context;
        this.student = student;
        this.subjectId = subjectId;
        this.inst_id = inst_id;
        firebaseAuth = FirebaseAuth.getInstance();
        urldb = "yourFirabaseDatabaseURL";
        database = FirebaseDatabase.getInstance(urldb);
        reference = database.getReference().child("Users").child(firebaseAuth.getUid())
                .child("Taught").child(subjectId).child("Students");
    }

    @NonNull
    @Override
    public Student_rv_adaptor.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_rv, parent, false);
        return new viewHolder(view);
    }

    @Override
    public int getItemCount() {
        return student.size();
    }

    @Override
    public void onBindViewHolder(@NonNull Student_rv_adaptor.viewHolder holder, int position) {
        User user = student.get(position);
        holder.email.setText(user.getEmail());
        holder.username.setText(user.getUsername());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    User existing = dataSnapshot.getValue(User.class);
                    if(existing.getUserId().equals(user.getUserId())) {
                        holder.add_stu.setText("ADDED");
                        holder.add_stu.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context,"Failed to add",Toast.LENGTH_LONG).show();
            }
        });
        holder.add_stu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.add_stu.getText().equals("ADD")) {
                    reference.child(user.getUserId()).setValue(user).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Problem",e.toString());
                        }
                    });
                    reference_stu = database.getReference().child("Users").child(user.getUserId()).child("Studies")
                            .child(subjectId);
                    reference_stu.child("instructor").setValue(inst_id);
                    reference_stu.child("Subject_Id").setValue(subjectId);
                }
            }
        });
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
