package com.example.ratefilm.activity;

import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ratefilm.R;
import com.example.ratefilm.data_response.User;
import com.example.ratefilm.databinding.RegistrationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {

    private RegistrationBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private static final String USERS = "Users";

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

        binding.register.setOnClickListener(view -> register());
    }

    private void register() {
        String username = binding.etUsername.getText().toString();
        String login = binding.registerLogin.getText().toString();
        String password = binding.registerPassword.getText().toString();

        if (username.equals("") || login.equals("") || password.equals("")) {
            Toast.makeText(this, getResources().getText(R.string.enter_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        login = login.toLowerCase();

        final String[] tmp = {login.split("@")[0]};

        if (tmp[0].contains(".") || tmp[0].contains("#") || tmp[0].contains("$") || tmp[0].contains("[") || tmp[0].contains("]")) {
            Toast.makeText(this, getResources().getText(R.string.invalid_symbols), Toast.LENGTH_SHORT).show();
            return;
        }

        String finalLogin = login;

        database.child(USERS).get().addOnSuccessListener(dataSnapshot -> {

            if (!dataSnapshot.hasChild(username)) {
                mAuth.createUserWithEmailAndPassword(finalLogin, password).addOnSuccessListener(authResult -> {
                    User user = new User(username, finalLogin);

                    database.child(USERS).child(tmp[0]).setValue(user);

                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.registration_success), Toast.LENGTH_SHORT).show();

                    finish();
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), getResources().getText(R.string.try_again), Toast.LENGTH_LONG).show());
            } else {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.username_already_exists), Toast.LENGTH_LONG).show();
            }
        });
    }
}
