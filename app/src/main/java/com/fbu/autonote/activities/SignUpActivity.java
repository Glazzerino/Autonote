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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.jetbrains.annotations.NotNull;

import es.dmoral.toasty.Toasty;

public class SignUpActivity extends AppCompatActivity {

    EditText etEmail;
    EditText etPassword;
    AppCompatButton btnCreateAcc;
    FirebaseAuth authManager;
    EditText etDisplayName;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        btnCreateAcc = findViewById(R.id.btnCreateAcc);
        etEmail = findViewById(R.id.etEmailS);
        etPassword = findViewById(R.id.etPasswordS);
        etDisplayName = findViewById(R.id.etDisplayName);
        authManager = FirebaseAuth.getInstance();
        context = this;
        btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    private void createAccount() {
        String displayName = etDisplayName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        if (!email.isEmpty() && !password.isEmpty() && !displayName.isEmpty()) {
            //Begin the account creation Async task
            authManager.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toasty.success(getApplicationContext(), "Welcome to Autonote!", Toasty.LENGTH_SHORT).show();
                            } else {
                                Toasty.error(getApplicationContext(), "Error while generating new user!", Toasty.LENGTH_SHORT).show();
                                Log.e("SignUpActivity", "Error: " + task.getException().toString());
                            }
                        }
                    }).continueWithTask(new Continuation<AuthResult, Task<Void>>() {
                //Continue the account creation task with an update to the display name
                @Override
                public Task<Void> then(@NonNull @NotNull Task<AuthResult> task) throws Exception {
                    UserProfileChangeRequest addNameRequest  = new UserProfileChangeRequest
                            .Builder()
                            .setDisplayName(displayName)
                            .build();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Task profileUpdateTask = user.updateProfile(addNameRequest);
                    return profileUpdateTask;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                //After all is done return to calling activity providing generated user credentials
                @Override
                public void onSuccess(Void unused) {
                    Intent intent = new Intent();
                    int requestCode = getIntent().getIntExtra("requestCode", 0);
                    intent.putExtra("result", requestCode);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
        }
    }
}