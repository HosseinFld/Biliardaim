package com.billardaim;

import android.app.*;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager wm;
    private AimView aimView;
    private View controlPanel;
    private WindowManager.LayoutParams aimParams, ctrlParams;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();
        addAimView();
        addControlPanel();
    }

    private void createNotification() {
        String ch = "aim_overlay";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(ch, "Billiard Aim", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
        startForeground(1, new Notification.Builder(this, ch)
                .setContentTitle("Billiard Aim Active")
                .setSmallIcon(android.R.drawable.ic_menu_compass).build());
    }

    private void addAimView() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        aimView = new AimView(this);
        aimParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        aimParams.gravity = Gravity.TOP | Gravity.START;
        wm.addView(aimView, aimParams);
    }

    private void addControlPanel() {
        controlPanel = LayoutInflater.from(this).inflate(R.layout.overlay_controls, null);
        ctrlParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        ctrlParams.gravity = Gravity.TOP | Gravity.START;
        ctrlParams.x = 16; ctrlParams.y = 120;
        wm.addView(controlPanel, ctrlParams);

        ImageButton btnPause   = controlPanel.findViewById(R.id.btnPause);
        ImageButton btnClear   = controlPanel.findViewById(R.id.btnClear);
        ImageButton btnCalib   = controlPanel.findViewById(R.id.btnCalib);
        ImageButton btnColor   = controlPanel.findViewById(R.id.btnColor);
        ImageButton btnClose   = controlPanel.findViewById(R.id.btnClose);
        View drag              = controlPanel.findViewById(R.id.dragHandle);

        // Pause / resume
        btnPause.setOnClickListener(v -> {
            if (aimView.getMode() == AimView.MODE_PAUSED) {
                aimView.setMode(AimView.MODE_AIM);
                aimParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                btnPause.setImageResource(android.R.drawable.ic_media_pause);
                btnPause.setColorFilter(0xFFAAAAAA);
            } else {
                aimView.setMode(AimView.MODE_PAUSED);
                aimParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                btnPause.setImageResource(android.R.drawable.ic_media_play);
                btnPause.setColorFilter(0xFF00FF88);
            }
            wm.updateViewLayout(aimView, aimParams);
        });

        // Clear current aim
        btnClear.setOnClickListener(v -> aimView.clearAim());

        // Re-calibrate table
        btnCalib.setOnClickListener(v -> {
            aimView.startCalibration();
            aimParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            wm.updateViewLayout(aimView, aimParams);
        });

        // Cycle line colour
        btnColor.setOnClickListener(v -> aimView.cycleColor());

        // Close overlay
        btnClose.setOnClickListener(v -> stopSelf());

        // Drag handle
        final float[] sr = new float[2];
        final int[]   sp = new int[2];
        drag.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                sr[0] = e.getRawX(); sr[1] = e.getRawY();
                sp[0] = ctrlParams.x; sp[1] = ctrlParams.y;
            } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                ctrlParams.x = sp[0] + (int)(e.getRawX() - sr[0]);
                ctrlParams.y = sp[1] + (int)(e.getRawY() - sr[1]);
                wm.updateViewLayout(controlPanel, ctrlParams);
            }
            return true;
        });
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (aimView != null)      wm.removeView(aimView);
        if (controlPanel != null) wm.removeView(controlPanel);
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
