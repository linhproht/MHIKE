package com.example.mhike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mhike.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding_data;
    DatabaseHelper2 dbHelper2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding_data = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding_data.getRoot());

        dbHelper2 = new DatabaseHelper2(this);

        binding_data.signupButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String email = binding_data.signupEmail.getText().toString();
                String password = binding_data.signupPassword.getText().toString();
                String confirmPassword = binding_data.signupConfirm.getText().toString();

                if (email.equals("") || password.equals("") || confirmPassword.equals(""))
                    Toast.makeText(SignupActivity.this, "Fill all the fields please!", Toast.LENGTH_SHORT).show();
                else {
                    if (password.equals(confirmPassword)){
                        Boolean checkUserEmail = dbHelper2.checkEmail(email);

                        if (checkUserEmail == false){
                            Boolean insert = dbHelper2.insertData(email, password);

                            if (insert == true){
                                Toast.makeText(SignupActivity.this, "Signup Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }else {
                                Toast.makeText(SignupActivity.this, "Signup Failed", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(SignupActivity.this, "User already existed, Please back to Login page", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(SignupActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        binding_data.loginRedirectText.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}