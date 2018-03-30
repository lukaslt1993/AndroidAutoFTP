package com.mif.juje0729.ftp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.example.R;

import org.apache.commons.net.ftp.FTPClient;

public class LauncherActivity extends Activity {

    public boolean downloadClicked = false;
    public boolean searchClicked = false;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission
                .WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission
                .READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        verifyStoragePermissions(this);
    }

    public void filesView(View theButton) {
        Intent intent = new Intent(this, FilesActivity.class);
        startActivity(intent);
    }

    public void serversView(View theButton) {
        Intent intent = new Intent(this, ServersActivity.class);
        startActivity(intent);
    }

    public void download(View theButton) {
        if (!downloadClicked && !searchClicked) {
            downloadClicked = true;
            TextView text = (TextView) findViewById(R.id.textView);
            text.setMovementMethod(new ScrollingMovementMethod());
            text.scrollTo(0, 0);
            text.setText("");
            NotesDatabase db = new NotesDatabase(this);
            FTPClient ftp = new FTPClient();
            FTPFunctions func = new FTPFunctions(text, db, ftp, true, this);
            func.start();

        }

    }

    public void search(View theButton) {
        if (!searchClicked && !downloadClicked) {
            searchClicked = true;
            TextView text = (TextView) findViewById(R.id.textView);

            text.setMovementMethod(new ScrollingMovementMethod());
            text.scrollTo(0, 0);
            text.setText("");
            NotesDatabase db = new NotesDatabase(this);
            FTPClient ftp = new FTPClient();
            FTPFunctions func = new FTPFunctions(text, db, ftp, false, this);
            func.start();
        }

    }

    public void shutDown(View theButton) {
        System.exit(0);
    }

}
