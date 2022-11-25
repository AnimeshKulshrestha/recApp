package com.microsoft.cognitiveservices.speech.project.recApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.cognitiveservices.speech.project.recApp.models.User;

import java.util.ArrayList;

public class Remove_stu_Adapter extends RecyclerView.Adapter<Remove_stu_Adapter.viewHolder> {
    Context context;
    ArrayList<User> student;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference reference,reference_stu;
    String urldb,subjectId,inst_id;


    public Remove_stu_Adapter(Context context, ArrayList<User> student, String subjectId, String inst_id) {
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
    public Remove_stu_Adapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_rv_rem, parent, false);
        return new viewHolder(view);
    }

    @Override
    public int getItemCount() {
        return student.size();
    }

    @Override
    public void onBindViewHolder(@NonNull Remove_stu_Adapter.viewHolder holder, int position) {
        User user = student.get(position);
        holder.email.setText(user.getEmail());
        holder.username.setText(user.getUsername());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> existing = new ArrayList<String>();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    existing.add(dataSnapshot.getValue(User.class).getUserId());
                }
                if(existing.contains(user.getUserId())){
                    holder.rem_stu.setEnabled(true);
                    holder.rem_stu.setText("REMOVE");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.rem_stu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.rem_stu.getText().equals("REMOVE")) {
                    reference.child(user.getUserId()).removeValue();
                    database.getReference().child("Users").child(user.getUserId()).child("Studies")
                            .child(subjectId).removeValue();
                    holder.rem_stu.setText("REMOVEd");
                    holder.rem_stu.setEnabled(false);
                }
            }
        });
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        TextView username;
        TextView email;
        Button rem_stu;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            rem_stu = itemView.findViewById(R.id.rem_stu_rv);
            email = itemView.findViewById(R.id.student_mail_rem);
            username = itemView.findViewById(R.id.StudentName_rem);
        }
    }
}
