package com.healthtracker.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.healthtracker.doctor.doctor.DoctorMainActivity;
import com.healthtracker.doctor.patient.PatientMainActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private Context context = LoginActivity.this;

    String Entity = "none";

    private Button doctor, patient, register, login;

    private TextView ForgetPass, privacy;

    private EditText email, password;

    private FirebaseAuth auth;
    private DatabaseReference reference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkAlreadyLoggedIn()){
            setContentView(R.layout.activity_login);
            initViews();

            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            auth = FirebaseAuth.getInstance();


            doctor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Entity = "doctors";
                    doctor.setBackground(getResources().getDrawable(R.drawable.btn_bg));
                    doctor.setTextColor(getResources().getColor(R.color.colorPrimary));

                    patient.setTextColor(getResources().getColor(R.color.colorAccent));
                    patient.setBackground(getResources().getDrawable(R.drawable.btn_bg_outline));
                }
            });

            patient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Entity = "patients";
                    doctor.setBackground(getResources().getDrawable(R.drawable.btn_bg_outline));
                    doctor.setTextColor(getResources().getColor(R.color.colorAccent));

                    patient.setTextColor(getResources().getColor(R.color.colorPrimary));
                    patient.setBackground(getResources().getDrawable(R.drawable.btn_bg));
                }
            });

            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkCredentials();
                }
            });

            ForgetPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(email.getText().toString())){
                        Toast.makeText(context, "Please enter your Email", Toast.LENGTH_SHORT).show();
                    } else {
                        sendPasswordResetEmail(email.getText().toString());
                    }
                }
            });
        }
    }

    private void sendPasswordResetEmail(String email) {
        progressDialog = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Sending Password Reset Email...");
        progressDialog.show();

        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(context, "Kindly check your Email Box", Toast.LENGTH_SHORT).show();
                } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                    Toast.makeText(context, "Email not Exists or Disable by the Admin", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void checkCredentials() {
        if (Entity.equals("none")){
            Toast.makeText(context, "Please select an Entity", Toast.LENGTH_SHORT).show();
            return;
        }

        String Email = email.getText().toString();
        String Password = password.getText().toString();

        if (TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password)){
            Toast.makeText(context, "All fields Required", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();
            firebaseLogin(Email, Password);
            
        }
    }

    private void firebaseLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser currentUser = auth.getCurrentUser();
                    final String userId = currentUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference(Entity).child(currentUser.getUid());

                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                startDashboard(Entity);
                                SharedPreferences.Editor editor = getSharedPreferences("doctor_app", MODE_PRIVATE).edit();
                                editor.putString(userId, Entity);
                                editor.apply();
                            } else {
                                Toast.makeText(context, "User doesn't exists in this entity", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startDashboard(String entity) {
        if (entity.equals("patients")){
            Intent intent = new Intent(context, PatientMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        } else if (entity.equals("doctors")){
            Intent intent = new Intent(context, DoctorMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        }
    }

    private boolean checkAlreadyLoggedIn(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser!=null){
            SharedPreferences prefs = getSharedPreferences("doctor_app", MODE_PRIVATE);
            String entity = prefs.getString(currentUser.getUid(), "Not Exist");

            if (!entity.equals("Not Exist")){
                startDashboard(entity);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void initViews() {
        register = findViewById(R.id.register_btn);
        doctor = findViewById(R.id.doctor);
        patient = findViewById(R.id.patient);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login_btn);
        ForgetPass = findViewById(R.id.forget_pass);
        privacy = findViewById(R.id.privacy);
    }
}
