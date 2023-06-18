package com.example.ratefilm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ratefilm.databinding.RegistrationBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {

    private RegistrationBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());
    }

    private void init() {
        binding = RegistrationBinding.inflate(getLayoutInflater());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    private void register() {
        String username = binding.etUsername.getText().toString();
        String login = binding.registerLogin.getText().toString();
        String password = binding.registerPassword.getText().toString();

        if (username.equals("") || login.equals("") || password.equals("")) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        database.child("Users").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(username)) {
                    mAuth.createUserWithEmailAndPassword(login, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            User user = new User(username, login);

                            String[] tmp = login.split("@");

                            database.child("Users").child(tmp[0]).setValue(user);

                            Toast.makeText(getApplicationContext(), "Вы успешно зарегистрировались", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Registration.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Произошла ошибка. Попробуйте снова", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Пользователь с таким именем уже существует", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
