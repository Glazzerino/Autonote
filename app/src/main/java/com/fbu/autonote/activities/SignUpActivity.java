package com.fbu.autonote.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.fbu.autonote.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class SignUpActivity extends AppCompatActivity {

    EditText etEmail;
    EditText etPassword;
    AppCompatButton btnCreateAcc;
    FirebaseAuth authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        btnCreateAcc = findViewById(R.id.btnCreateAcc);
        etEmail = findViewById(R.id.etEmailS);
        etPassword = findViewById(R.id.etPasswordS);
        authManager = FirebaseAuth.getInstance();
        btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                createAccount(email, password);
            }
        });
    }

    private void createAccount(String email, String password) {
        authManager.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Welcome to Autonote!", Toast.LENGTH_LONG);
                            Intent intent = new Intent();
                            int requestCode = getIntent().getIntExtra("requestCode", 0);
                            intent.putExtra("result", requestCode);
                            intent.putExtra("email", email);
                            intent.putExtra("password", password);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error while generating new user!" ,Toast.LENGTH_SHORT);
                            Log.e("SignUpActivity", "Error: " + task.getException().toString());
                        }
                    }
                });
    }
}