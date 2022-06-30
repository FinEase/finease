package com.example.finease;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

public class ExportActivity extends AppCompatActivity {

    private DatabaseReference exportRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        mAuth = FirebaseAuth.getInstance();
        exportRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);

        exportRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Map<String, String> value = (Map<String, String>) snapshot.getValue();
                    JSONObject json = new JSONObject(value);
                    String jsonString = json.toString();
                    try {
                        Writer output = null;
                        ContextWrapper cw = new ContextWrapper(getApplicationContext());
                        File directory = cw.getDir("data", Context.MODE_PRIVATE);
                        File file = new File(directory, "exportedData" + ".json");

                        output = new BufferedWriter(new FileWriter(file));
                        output.write(jsonString);
                        output.close();
                        Toast.makeText(getApplicationContext(), "DB saved as JSON to " + String.valueOf(directory) + "exportedData.json", Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        }
    }