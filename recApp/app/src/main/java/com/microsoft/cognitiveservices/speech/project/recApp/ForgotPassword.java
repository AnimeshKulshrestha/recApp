package com.microsoft.cognitiveservices.speech.project.recApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private TextInputLayout femaillayout;
    private TextInputEditText femail;
    private Button forgotpass;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        String email = getIntent().getStringExtra("mail_id");
        femail = findViewById(R.id.forgotpassedittext);
        femaillayout = findViewById(R.id.forgotpasslayout);
        femail.setText(email);
        forgotpass = findViewById(R.id.forgot_pass_btn);
        auth = FirebaseAuth.getInstance();

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendemail = femail.getText().toString().trim();
                if(emailValid(sendemail)){
                    auth.sendPasswordResetEmail(sendemail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ForgotPassword.this,"Email sent",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(ForgotPassword.this,Login.class));
                                finish();
                            }else{
                                Toast.makeText(ForgotPassword.this,"Email not sent",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public boolean emailValid(String email){

        if(email==null || email==""){
            femaillayout.setError("This field can't be empty");
            return false;
        }
        if(email.contains("@")){
            femaillayout.setError(null);
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
        femaillayout.setError("Invalid Email");
        return false;
    }
}