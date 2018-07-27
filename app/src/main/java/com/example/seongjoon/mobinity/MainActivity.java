package com.example.seongjoon.mobinity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_USED_PERMISSION = 200;
    private static final String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // For requesting the runtime permissions.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionToLocationAccepted = true;

        switch(requestCode){
            case REQUEST_USED_PERMISSION:
                for(int result : grantResults){
                    if(result != PackageManager.PERMISSION_GRANTED){
                        permissionToLocationAccepted = false;
                        break;
                    }
                }
                break;
        }

        if(!permissionToLocationAccepted){
            finish();
        } else {
            // 앱 자동으로 넘기게 하면 될 듯.
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button newMainButton = findViewById(R.id.mainButton);
        newMainButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent); // Move the next page.
            }
        });

        // Check the requested permissions.
        for(String permission : needPermissions){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,needPermissions, REQUEST_USED_PERMISSION);
                break;
            }
        }
    }

}
