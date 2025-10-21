package com.example.sitevision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    LinearLayout btnFloorPlan,btnSiteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Link UI buttons
        btnFloorPlan = findViewById(R.id.btnFloorPlan);
        btnSiteView = findViewById(R.id.btnSiteView);

        // Handle Floor Plan click
        btnFloorPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Start AR Floor Plan Activity
                // Replace FloorPlanActivity with your XR implementation
                Intent intent = new Intent(MainActivity.this, FloorPlanActivity.class);
                startActivity(intent);
            }
        });

        // Handle 360° Site View click
        btnSiteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Start 360° XR activity
                Intent intent = new Intent(MainActivity.this, Site360Activity.class);
                startActivity(intent);
            }
        });
    }
}