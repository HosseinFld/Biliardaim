package com.billardaim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_REQ = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop  = findViewById(R.id.btnStop);
        Button btnReset = findViewById(R.id.btnReset);

        btnStart.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())), OVERLAY_REQ);
            } else startOverlay();
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, OverlayService.class));
            Toast.makeText(this, "Overlay stopped", Toast.LENGTH_SHORT).show();
        });

        btnReset.setOnClickListener(v -> {
            TableCalibration.reset(this);
            Toast.makeText(this, "Table calibration reset", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == OVERLAY_REQ) {
            if (Settings.canDrawOverlays(this)) startOverlay();
            else Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startOverlay() {
        startService(new Intent(this, OverlayService.class));
        Toast.makeText(this, "Overlay active!", Toast.LENGTH_LONG).show();
        finish();
    }
}
