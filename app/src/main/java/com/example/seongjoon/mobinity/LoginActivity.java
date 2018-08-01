package com.example.seongjoon.mobinity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button snsButton = findViewById(R.id.sns);
        snsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginActivity.this, SNSLoginActivity.class);
                startActivity(intent); // Move the next page.
            }
        });

        Button loginButton = findViewById(R.id.loginButton);
        EditText emailText = findViewById(R.id.email);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // 사용자 설정
                User.getInstance().setUserName(emailText.getText().toString());
                User.getInstance().setProfileImagePath("@mipmap/ic_launcher_round");


                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                startActivity(intent); // Move the next page.
            }
        });
    }
}


