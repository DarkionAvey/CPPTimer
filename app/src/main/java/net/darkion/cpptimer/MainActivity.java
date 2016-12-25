package net.darkion.cpptimer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by DarkionAvey
 */

public class MainActivity extends Activity {
    @TargetApi(VERSION_CODES.M)
    public void requestPermissions() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getApplicationContext().getPackageName()));

        startActivityForResult(intent, 1);
    }

    @TargetApi(VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(getApplicationContext(), "Access denied", Toast.LENGTH_LONG).show();
                finish();

            }
            else startService(new Intent(getApplicationContext(), BBW.class));

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getBaseContext())) {
                requestPermissions();
                return;
            }
        }
        startService(new Intent(getApplicationContext(), BBW.class));


    }

}
