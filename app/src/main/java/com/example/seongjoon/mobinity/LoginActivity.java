package com.example.seongjoon.mobinity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

public class LoginActivity extends AppCompatActivity {

    private SessionCallback sessionCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button newLoginButton = findViewById(R.id.loginButton);
        newLoginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                startActivity(intent); // Move the next page.
            }
        });

        Button logoutButton = findViewById(R.id.LogoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (Session.getCurrentSession().isOpened()) {
                    // 로그인 상태
                    UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                        @Override
                        public void onCompleteLogout() {
                            // 로그아웃 시 activity 이동할 것.
                            Toast.makeText(LoginActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 로그인되어있지 않은 상태
                    Toast.makeText(LoginActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)){
            return ;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void request(){
        UserManagement.getInstance().requestMe(new MeResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d("error", "Session Closed Error is " + errorResult.toString());
            }

            @Override
            public void onNotSignedUp() {

            }

            @Override
            public void onSuccess(UserProfile result) {
                Toast.makeText(LoginActivity.this, "사용자 이름은 " + result.getNickname(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            request();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.d("error", "Session Fail Error is " + exception.getMessage().toString());
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }
}


