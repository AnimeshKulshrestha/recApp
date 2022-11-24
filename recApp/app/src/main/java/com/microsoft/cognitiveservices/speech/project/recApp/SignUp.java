package com.microsoft.cognitiveservices.speech.project.recApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.microsoft.cognitiveservices.speech.project.recApp.dialogFrag.LoadingDialog;
import com.microsoft.cognitiveservices.speech.project.recApp.models.User;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private TextView existinglogin;
    private TextInputEditText newusername,newpass,newemail,cnfpass,fname,lname;
    private TextInputLayout unlayout,passlayout,emaillayout,cnflayout,flayout,llayout;
    private Button signUp;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private LoadingDialog loadingDialog;

    private String urldb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        urldb = "https://recapp-9edb4-default-rtdb.asia-southeast1.firebasedatabase.app";

        existinglogin = findViewById(R.id.existinglogin);
        newusername = findViewById(R.id.newusername);
        loadingDialog = new LoadingDialog(SignUp.this);
        newpass = findViewById(R.id.newpass);
        newemail = findViewById(R.id.newemail);
        fname = findViewById(R.id.fname);
        lname = findViewById(R.id.lname);
        cnfpass = findViewById(R.id.cnfpass);
        unlayout = findViewById(R.id.newuserlayout);
        passlayout = findViewById(R.id.newpasslayout);
        emaillayout = findViewById(R.id.newemaillayout);
        cnflayout = findViewById(R.id.cnfpasslayout);
        flayout = findViewById(R.id.fnamelayout);
        llayout = findViewById(R.id.lnamelayout);
        signUp = findViewById(R.id.signup);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(urldb);

        existinglogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUp.this,Login.class);
                startActivity(i);
            }
        });

        newusername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                usernameValid();
            }
        });
        fname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                fnameValid();
            }
        });
        lname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                lnameValid();
            }
        });
        newemail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                emailValid();
            }
        });
        newpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                passwordValid();
            }
        });
        cnfpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                confirmation();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = newemail.getText().toString().trim();
                String password = newpass.getText().toString().trim();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String first = fname.getText().toString().trim(),
                                    last = lname.getText().toString().trim(),
                                    email = newemail.getText().toString().trim(),
                                    username = newusername.getText().toString().trim();
                            String id = task.getResult().getUser().getUid();
                            loadingDialog.startLoading();
                            Toast.makeText(SignUp.this,"Email for email confirmation",Toast.LENGTH_LONG).show();
                            FirebaseUser new_user= mAuth.getCurrentUser();
                            new_user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        User user = new User(first, last, username, email, id);
                                        database.getReference().child("Users").child(id).setValue(user);
                                        mAuth.signOut();
                                        Intent i = new Intent(SignUp.this, Login.class);
                                        startActivity(i);
                                        finish();
                                    }
                                    loadingDialog.dismiss();
                                }
                            });
                        }
                        else{
                            Toast.makeText(SignUp.this,"Error... Invalid Entry", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


    }

    public boolean usernameValid(){
        String username = newusername.getText().toString().trim();
        if(username.equals("")){
            unlayout.setError("Field can't be empty");
            return false;
        }
        flayout.setError(null);
        return true;
    }

    public boolean isNameValid(@NonNull String name){
        for(int i=0;i<name.length();i++){
            if(Character.isDigit(name.charAt(i)))
                return false;
        }
        return true;
    }
    public boolean fnameValid(){
        String firstname = fname.getText().toString().trim();
        if(firstname.equals("")){
            flayout.setError("Field can't be empty");
            return false;
        }
        if(isNameValid(firstname)) {
            flayout.setError(null);
            return true;
        }
        flayout.setError("Invalid First Name");
        return false;
    }

    public boolean lnameValid(){
        String lastname = lname.getText().toString().trim();
        if(isNameValid(lastname)) {
            llayout.setError(null);
            return true;
        }
        llayout.setError("Invalid Last Name");
        return false;
    }
    public boolean emailValid(){
        String email = newemail.getText().toString().trim();
        if(email==null){
            emaillayout.setError("This field can't be empty");
            return false;
        }
        if(email.contains("@")){
            emaillayout.setError(null);
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
        emaillayout.setError("Invalid Email");
        return false;
    }
    public boolean passwordValid(){
        String pass = newpass.getText().toString().trim();
        if(pass==null){
            passlayout.setError("This field can't be empty");
            return false;
        }
        if(pass.length()<8){
            passlayout.setError("Password must be atleast 8 characters");
            return false;
        }
        passlayout.setError(null);
        return true;
    }
    public boolean confirmation(){
        String pass = newpass.getText().toString().trim(),cnf = cnfpass.getText().toString().trim();
        if(pass.equals(cnf)) {
            cnflayout.setError(null);
            return true;
        }
        cnflayout.setError("Passwords don't match");
        return false;
    }
}