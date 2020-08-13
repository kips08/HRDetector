package com.kips.hrdetector;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button yourButton = (Button) findViewById(R.id.start);

        yourButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivityPermissionsDispatcher.openPermisionWithPermissionCheck(MainActivity.this);
            }
        });
    }

    @NeedsPermission({Manifest.permission.CAMERA})
    public void openPermision() {
        startActivity(new Intent(MainActivity.this, HRDetection.class));
    }

    @OnShowRationale({Manifest.permission.CAMERA})
    void showRationaleForCameraOrGallery(final PermissionRequest request) {
        MainActivityPermissionsDispatcher.openPermisionWithPermissionCheck(MainActivity.this);
    }

    @OnPermissionDenied({Manifest.permission.CAMERA})
    void showDeniedForCameraOrGallery() {

    }

    @OnNeverAskAgain({Manifest.permission.CAMERA})
    void showNeverAskForCameraOrGallery() {
        Toast.makeText(this, "Aktifkan Manual di setting", Toast.LENGTH_LONG).show();
    }

}
