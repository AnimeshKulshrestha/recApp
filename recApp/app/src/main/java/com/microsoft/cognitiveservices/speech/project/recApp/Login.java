package com.microsoft.cognitiveservices.speech.project.recApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class Login extends AppCompatActivity {

    private TextView newsignup;
    private TextInputEditText logemail,logpass;
    private Button login;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        newsignup = findViewById(R.id.newsignup);
        logemail = findViewById(R.id.logemail);
        logpass = findViewById(R.id.logpass);
        login = findViewById(R.id.login);

        newsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Login.this,SignUp.class);
                startActivity(i);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = logemail.getText().toString().trim(),
                        password = logpass.getText().toString().trim();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>(){
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Intent i = new Intent(Login.this,MainActivity.class);
                                    startActivity(i);
                                }else{
                                    Toast.makeText(Login.this,
                                            "Authentication failed.",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }


}