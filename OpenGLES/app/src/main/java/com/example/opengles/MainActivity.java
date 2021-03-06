package com.example.opengles;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.opengles.filter.FBOActivity;
import com.example.opengles.filter.FilterActivity;
import com.example.opengles.location.LocationActivity;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 1);

        }
        return false;
    }

    public void gltest(View view) {
        startActivity(new Intent(this, GLTestActivity.class));

    }

    public void filterCamera(View view) {
        startActivity(new Intent(this, FilterActivity.class));
    }

    public void fbodemo(View view) {
        Intent intent = new Intent(this, FBOActivity.class);
        intent.putExtra("type",0);
        startActivity(intent);
    }

    public void location(View view) {
        startActivity(new Intent(this, LocationActivity.class));
    }

    public void soulBtn(View view) {
        Intent intent = new Intent(this, FBOActivity.class);
        intent.putExtra("type",1);
        startActivity(intent);
    }

    public void splitScreen(View view) {
        Intent intent = new Intent(this, FBOActivity.class);
        intent.putExtra("type",2);
        startActivity(intent);
    }

    public void beautiful(View view) {
        Intent intent = new Intent(this, FBOActivity.class);
        intent.putExtra("type",3);
        startActivity(intent);
    }
}