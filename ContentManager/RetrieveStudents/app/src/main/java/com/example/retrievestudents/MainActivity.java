package com.example.retrievestudents;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickRetrieveStudents(View view) {

//        Toast.makeText(MainActivity.this, "Hello Click", Toast.LENGTH_SHORT).show();

        // Retrieve student records
        String URL = "content://com.example.provider.College/students";

        Uri students = Uri.parse(URL);
        Cursor c = managedQuery(students, null, null, null, "name");

        if (c.moveToFirst()) {
            do{
                Toast.makeText(this,
                        c.getString(c.getColumnIndex("_id")) +
                                ", " +  c.getString(c.getColumnIndex("name")) +
                                ", " + c.getString(c.getColumnIndex("grade")),
                        Toast.LENGTH_SHORT).show();
            } while (c.moveToNext());
        }
    }
}
