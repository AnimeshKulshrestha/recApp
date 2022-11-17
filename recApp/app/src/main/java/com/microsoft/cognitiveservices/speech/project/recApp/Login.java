package com.microsoft.cognitiveservices.speech.project.recApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Long2;
import android.text.Editable;
import android.text.TextWatcher;
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
    Boolean valid_m,valid_p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null) {
            Intent intent = new Intent(Login.this, Subjects.class);
            startActivity(intent);
            finish();
        }


        newsignup = findViewById(R.id.newsignup);
        logemail = findViewById(R.id.logemail);
        logpass = findViewById(R.id.logpass);
        login = findViewById(R.id.login);

        valid_m = false;
        valid_p = false;

        newsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Login.this,SignUp.class);
                startActivity(i);
            }
        });

        logemail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                validmail();
            }
        });

        logpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                validpass();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = logemail.getText().toString().trim(),
                        password = logpass.getText().toString().trim();
                if(valid_m && valid_p) {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent i = new Intent(Login.this, Subjects.class);
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(Login.this,
                                                "Authentication failed.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(Login.this,"Empty Fields , can't login", Toast.LENGTH_LONG).show();;
                }
            }
        });
    }
    public void validmail(){
        if(!(logemail.getText().equals("")||logemail.getText().equals(null)))
            valid_m = true;
    }
    public void validpass(){
        if(!(logpass.getText().equals("")||logpass.getText().equals(null)))
            valid_p = true;
    }
}