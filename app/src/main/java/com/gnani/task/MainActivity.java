package com.gnani.task;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView showPhoto;
    File file;
    ArrayList<String> toMatch = new ArrayList<>();
    ArrayList<String> blackList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showPhoto = findViewById(R.id.show_photo);
        FloatingActionButton fab = findViewById(R.id.fab);

        toMatch.add("selfie");
        toMatch.add("takeselfie");
        toMatch.add("take selfie");
        toMatch.add("click selfie");
        toMatch.add("ek selfie");
        toMatch.add("1 selfie");
        toMatch.add("open camera and take selfie");

        blackList.add("don't");
        blackList.add("no");
        blackList.add("do not");
        blackList.add("not");
        blackList.add("cancel");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Started listening", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startListening();

            }
        });
    }


    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speech prompt");
        try {
            startActivityForResult(intent, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "No speech support",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                try {
                    int textTokenCount = 0;
                    if (resultCode == RESULT_OK && null != data) {
                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        for (String str : result) {
                            if (toMatch.contains(str) && !blackList.contains(str)) {
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                                } else {
                                    takePicture();
                                    break;
                                }
                            }else {
                                textTokenCount++;
                            }
                        }
                        if(result.size() == textTokenCount){
                            Toast.makeText(getApplicationContext(), "We couldn't recognise the speech", Toast.LENGTH_SHORT).show();
                        }

                    }
                } catch (Exception e) {
                    Log.d("Debug", e.getMessage());
                }
                break;
            }

            case 101:
                if (resultCode == RESULT_OK) {
                    showPhoto.setImageURI(FileProvider.getUriForFile(getApplicationContext(),
                            getPackageName(), file));
                    Toast.makeText(getApplicationContext(), "took photo", Toast.LENGTH_SHORT).show();
                }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getApplicationContext(), "We can't proceed without permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = getOutputMediaFile();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(file));
        } else {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(getApplicationContext(),
                            getPackageName(), file));
        }
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(takePictureIntent, 101);
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }
}
