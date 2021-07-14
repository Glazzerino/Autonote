package com.fbu.autonote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Context;
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

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth authManager;
    AppCompatButton btnLogin;
    AppCompatButton btnSignup;
    EditText etPassword;
    EditText etEmail;
    Context context;
    int ACC_CREATION_REQ_CODE = 1011;
    public final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = FirebaseAuth.getInstance();
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        context = this;
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignUp();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                attemptLogin(email, password);
            }
        });
    }

    private void attemptLogin(String email, String password) {

        authManager.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Logged in!");
                    Toast.makeText(context, "Logged in!", Toast.LENGTH_SHORT);
                    launchMainActivity();

                } else {
                    Toast.makeText(context, "Incorrect password or email", Toast.LENGTH_SHORT);
                    Log.e(TAG, "Error logging in: " + task.getException().toString());
                }
            }
        });
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("requestCode", ACC_CREATION_REQ_CODE);
        startActivity(intent);
    }

    private void launchSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent, ACC_CREATION_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == ACC_CREATION_REQ_CODE) {
            String email = data.getStringExtra("email");
            String password = data.getStringExtra("password");
            attemptLogin(email, password);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Skip to main activity if there is a valid session stored on device
        if (authManager.getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}