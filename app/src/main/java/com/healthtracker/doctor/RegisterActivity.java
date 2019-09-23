package com.healthtracker.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.healthtracker.doctor.doctor.DoctorMainActivity;
import com.healthtracker.doctor.model.Registration;
import com.healthtracker.doctor.patient.PatientMainActivity;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private Context context = RegisterActivity.this;

    private String Entity = "none";

    private EditText firstName, lastName, Email, Country, Phone, Password, confirmPassword;

    private Button doctor, patient, Register;

    private ImageView back, passView, confirmView;

    private FirebaseAuth auth;
    private DatabaseReference reference;

    private ProgressDialog progressDialog;

    private Registration registerUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();

        auth = FirebaseAuth.getInstance();

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

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

        passView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    Password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    Password.setSelection(Password.length());
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    Password.setSelection(Password.length());
                    return true;
                }
                return false;
            }
        });

        confirmView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    confirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confirmPassword.setSelection(confirmPassword.length());
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confirmPassword.setSelection(confirmPassword.length());
                    return true;
                }
                return false;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performRegistration() {
        if (Entity.equals("none")){
            Toast.makeText(context, "Please select an Entity", Toast.LENGTH_SHORT).show();
            return;
        } else {
            reference = FirebaseDatabase.getInstance().getReference(Entity);
        }

        String FirstName = firstName.getText().toString();
        String LastName = lastName.getText().toString();
        String email = Email.getText().toString();
        String country = Country.getText().toString();
        String phone = Phone.getText().toString();
        String password = Password.getText().toString();
        String ConfrimPassowrd = confirmPassword.getText().toString();

        if (TextUtils.isEmpty(FirstName) || TextUtils.isEmpty(LastName) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(country) ||
                TextUtils.isEmpty(phone) ||  TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(ConfrimPassowrd)){
            Toast.makeText(context, "All fields Required", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(ConfrimPassowrd)){
            Toast.makeText(context, "Password not Matched", Toast.LENGTH_SHORT).show();
        } else {
            registerUser = new Registration(FirstName + " " + LastName, email, country, phone, password);
            progressDialog = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
            progressDialog.setMessage("Registering...");
            progressDialog.show();
            registerUserinFirebase();
        }
    }

    private void registerUserinFirebase() {
        auth.createUserWithEmailAndPassword(Email.getText().toString(), Password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    final FirebaseUser currentUser = auth.getCurrentUser();
                    final String userId = currentUser.getUid();

                    SharedPreferences.Editor editor = getSharedPreferences("doctor_app", MODE_PRIVATE).edit();
                    editor.putString(userId, Entity);
                    editor.apply();

                    uploadToFirebase(userId);
                } else {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void uploadToFirebase(String userId) {
        reference.child(userId).setValue(registerUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Successfully Registered!", Toast.LENGTH_SHORT).show();
                startMainActivity();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void startMainActivity() {
        if (Entity.equals("patients")){
            Intent intent = new Intent(context, PatientMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        } else if (Entity.equals("doctors")){
            Intent intent = new Intent(context, DoctorMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        }
    }

    private void initViews() {
        back = findViewById(R.id.back);
        doctor = findViewById(R.id.doctor);
        patient = findViewById(R.id.patient);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        Email = findViewById(R.id.email);
        Country = findViewById(R.id.country);
        Phone = findViewById(R.id.phone);
        Password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        passView = findViewById(R.id.view_password);
        confirmView = findViewById(R.id.view_confirm_password);
        Register = findViewById(R.id.sign_up_btn);
    }
}
