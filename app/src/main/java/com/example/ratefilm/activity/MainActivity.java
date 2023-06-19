package com.example.ratefilm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.example.ratefilm.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
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

        mAuth = FirebaseAuth.getInstance();

        binding.enter.setOnClickListener(view -> tryEnter());

        binding.noAccount.setOnClickListener(view -> register());
    }

    private void tryEnter() {
        String login = binding.login.getText().toString();
        String password = binding.password.getText().toString();

        if (login.equals("") || password.equals("")) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(login, password).addOnSuccessListener(authResult -> showFilmsList()).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Произошла ошибка, попробуйте снова", Toast.LENGTH_LONG).show());
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