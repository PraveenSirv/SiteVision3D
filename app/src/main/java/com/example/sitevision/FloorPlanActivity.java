package com.example.sitevision;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FloorPlanActivity extends AppCompatActivity {

    ListView planListView;
    List<String> planFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_floor_plan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set title for the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("2D Plan Explorer");
        }

        planListView = findViewById(R.id.planListView);

        // Load all .pdf files from assets folder
        planFiles = getFilesFromAssets();

        if (planFiles.isEmpty()) {
            Toast.makeText(this, "No floor plans found in assets folder.", Toast.LENGTH_SHORT).show();
        }

        // Show the list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                planFiles
        );

        planListView.setAdapter(adapter);

        // Handle item click
        planListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedFile = planFiles.get(position);
            Intent intent = new Intent(this, PlanViewerActivity.class);
            intent.putExtra("file_name", selectedFile);
            startActivity(intent);
        });
    }

    // Function to list all PDF files from assets/
    private List<String> getFilesFromAssets() {
        List<String> planList = new ArrayList<>();
        AssetManager assetManager = getAssets();
        try {
            String[] files = assetManager.list("");
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".png") || file.endsWith(".pdf")) {
                        planList.add(file);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return planList;
    }
}