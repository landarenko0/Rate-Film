package com.example.ratefilm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ratefilm.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private EditText login_et;
    private EditText password_et;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        init();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            showFilmsList();
        }

        setContentView(binding.getRoot());
    }

    private void init() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        login_et = binding.login;
        password_et = binding.password;
        mAuth = FirebaseAuth.getInstance();

        binding.enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryEnter();
            }
        });

        binding.noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    private void tryEnter() {
        String login = login_et.getText().toString();
        String password = password_et.getText().toString();

        if (login.equals("") || password.equals("")) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(login, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                showFilmsList();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Произошла ошибка, попробуйте снова", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void register() {
        Intent intent = new Intent(MainActivity.this, Registration.class);
        startActivity(intent);
    }

    private void showFilmsList() {
        Intent intent = new Intent(MainActivity.this, FilmsListActivity.class);
        startActivity(intent);
    }
}