package com.billardaim;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

public class AimView extends View {

    // ── Modes ─────────────────────────────────────────────────────────────
    public static final int MODE_CALIBRATE = 0;
    public static final int MODE_AIM       = 1;
    public static final int MODE_PAUSED    = 2;
    private int mode = MODE_AIM;

    // ── Touch sub-states in AIM mode ──────────────────────────────────────
    private static final int STEP_IDLE        = 0; // waiting for ball tap
    private static final int STEP_MAGNIFY     = 1; // finger down, showing loupe
    private static final int STEP_BALL_SET    = 2; // ball confirmed, waiting for aim drag
    private static final int STEP_AIMING      = 3; // dragging aim direction
    private int step = STEP_IDLE;

    // ── Colours ───────────────────────────────────────────────────────────
    private static final int[] COLORS = {
        0xFF00FF88, 0xFFFF4444, 0xFF44AAFF, 0xFFFFDD00, 0xFFFF88FF
    };
    private int colorIdx = 0;

    // ── Table ─────────────────────────────────────────────────────────────
    private final TableCalibration table;
    private int calibCorner = 0;

    // ── Ball & aim positions ──────────────────────────────────────────────
    private float ballX, ballY;       // confirmed cue ball centre
    private float fingerX, fingerY;   // live finger position (for loupe)
    private float aimX, aimY;
    private boolean hasBall = false;
    private boolean hasAim  = false;

    // ── Loupe settings ────────────────────────────────────────────────────
    private static final float LOUPE_RADIUS   = 90f;   // loupe circle radius (px)
    private static final float LOUPE_ZOOM     = 3.0f;  // magnification
    private static final float LOUPE_OFFSET_Y = 200f;  // how far above finger to show it
    // How long finger must be held before loupe appears (ms)
    private static final long  LOUPE_DELAY_MS = 0;     // instant

    private boolean loupeVisible = false;

    // ── Paints ────────────────────────────────────────────────────────────
    private final Paint linePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint loupePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint loupeRing    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint loupeCross   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cornerPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dimPaint     = new Paint();

    // Loupe bitmap buffer
    private Bitmap loupeBitmap;
    private Canvas loupeCanvas;

    public AimView(Context ctx) {
        super(ctx);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        shadowPaint.setColor(0x99000000);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeCap(Paint.Cap.ROUND);

        loupeRing.setStyle(Paint.Style.STROKE);
        loupeRing.setStrokeWidth(4f);
        loupeRing.setColor(0xFFFFFFFF);

        loupeCross.setStyle(Paint.Style.STROKE);
        loupeCross.setStrokeWidth(2f);
        loupeCross.setColor(0xFFFF4444);

        cornerPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(42f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        dimPaint.setColor(0x66000000);

        table = TableCalibration.load(ctx);
        if (table.calibrated) { mode = MODE_AIM; calibCorner = 4; }
        else                  { mode = MODE_CALIBRATE; calibCorner = 0; }
    }

    // ── Public API ────────────────────────────────────────────────────────

    public void setMode(int m) {
        mode = m;
        setAlpha(m == MODE_PAUSED ? 0.25f : 1f);
        invalidate();
    }
    public int getMode() { return mode; }

    public void startCalibration() {
        table.calibrated = false; calibCorner = 0;
        for (int i = 0; i < 4; i++) table.corners[i] = null;
        mode = MODE_CALIBRATE;
        hasBall = false; hasAim = false; step = STEP_IDLE;
        invalidate();
    }

    public void clearAim() {
        hasBall = false; hasAim = false;
        loupeVisible = false; step = STEP_IDLE;
        invalidate();
    }

    public void cycleColor() { colorIdx = (colorIdx + 1) % COLORS.length; invalidate(); }
    public boolean isCalibrated() { return table.calibrated; }

    // ── Touch ─────────────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mode == MODE_PAUSED) return false;
        float x = e.getX(), y = e.getY();

        // ── Calibration mode ──────────────────────────────────────────────
        if (mode == MODE_CALIBRATE) {
            if (e.getAction() == MotionEvent.ACTION_DOWN && calibCorner < 4) {
                table.corners[calibCorner++] = new PointF(x, y);
                if (calibCorner == 4) { table.save(getContext()); mode = MODE_AIM; }
                invalidate();
            }
            return true;
        }

        // ── Aim mode ──────────────────────────────────────────────────────
        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (step == STEP_IDLE || step == STEP_BALL_SET) {
                    // Start loupe for precise ball selection
                    fingerX = x; fingerY = y;
                    loupeVisible = true;
                    step = STEP_MAGNIFY;
                    invalidate();
                } else if (step == STEP_BALL_SET) {
                    // Second touch = start aiming
                    aimX = x; aimY = y; hasAim = true;
                    step = STEP_AIMING;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (step == STEP_MAGNIFY) {
                    // Move loupe while selecting ball
                    fingerX = x; fingerY = y;
                    invalidate();
                } else if (step == STEP_AIMING || step == STEP_BALL_SET) {
                    // Dragging aim direction
                    aimX = x; aimY = y; hasAim = true;
                    step = STEP_AIMING;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (step == STEP_MAGNIFY) {
                    // Confirm ball position at finger location
                    ballX = fingerX; ballY = fingerY;
                    hasBall = true; loupeVisible = false;
                    step = STEP_BALL_SET;
                    hasAim = false;
                    invalidate();
                } else if (step == STEP_AIMING) {
                    // Aim confirmed
                    invalidate();
                }
                return true;
        }
        return false;
    }

    // ── Draw ──────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mode == MODE_CALIBRATE) { drawCalibUI(canvas); return; }

        drawTableBorder(canvas);

        if (hasBall) drawBallMarker(canvas, ballX, ballY);

        if (hasBall && hasAim) {
            float dx = aimX - ballX, dy = aimY - ballY;
            if (dist(0,0,dx,dy) > 15)
                drawAimLine(canvas, ballX, ballY, dx, dy, COLORS[colorIdx], 0);
        }

        // Loupe (magnifier) – drawn last so it's on top
        if (loupeVisible) drawLoupe(canvas, fingerX, fingerY);

        // Hints
        if (!hasBall)
            drawHint(canvas, "انگشت را روی توپ سفید نگه دار");
        else if (!hasAim)
            drawHint(canvas, "بکش برای aim کردن");
    }

    // ── Loupe drawing ─────────────────────────────────────────────────────

    private void drawLoupe(Canvas canvas, float fx, float fy) {
        int W = getWidth(), H = getHeight();
        float lr = LOUPE_RADIUS;
        float cx = fx;
        float cy = fy - LOUPE_OFFSET_Y; // show loupe above finger

        // Keep loupe inside screen
        if (cx - lr < 0)  cx = lr;
        if (cx + lr > W)  cx = W - lr;
        if (cy - lr < 0)  cy = fy + LOUPE_OFFSET_Y; // flip below if too close to top

        // Create / reuse offscreen bitmap for the magnified region
        int bw = (int)(lr * 2), bh = (int)(lr * 2);
        if (loupeBitmap == null || loupeBitmap.getWidth() != bw) {
            loupeBitmap = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
            loupeCanvas = new Canvas(loupeBitmap);
        }
        loupeBitmap.eraseColor(Color.TRANSPARENT);

        // Draw magnified view: translate so finger is in centre, scale up
        loupeCanvas.save();
        loupeCanvas.translate(lr, lr);
        loupeCanvas.scale(LOUPE_ZOOM, LOUPE_ZOOM);
        loupeCanvas.translate(-fx, -fy);

        // Draw table border in loupe
        drawTableBorder(loupeCanvas);
        // Draw existing ball marker if any
        if (hasBall) drawBallMarker(loupeCanvas, ballX, ballY);

        loupeCanvas.restore();

        // Clip to circle and draw onto main canvas
        canvas.save();
        Path clip = new Path();
        clip.addCircle(cx, cy, lr, Path.Direction.CW);
        canvas.clipPath(clip);

        // Dark background first
        Paint bg = new Paint(); bg.setColor(0xFF111122);
        canvas.drawCircle(cx, cy, lr, bg);

        // Draw the magnified bitmap
        canvas.drawBitmap(loupeBitmap, cx - lr, cy - lr, loupePaint);
        canvas.restore();

        // Crosshair at centre of loupe
        canvas.drawLine(cx - 20, cy, cx + 20, cy, loupeCross);
        canvas.drawLine(cx, cy - 20, cx, cy + 20, loupeCross);
        canvas.drawCircle(cx, cy, 5f, loupeCross);

        // Outer ring
        canvas.drawCircle(cx, cy, lr, loupeRing);

        // Label
        Paint lbl = new Paint(Paint.ANTI_ALIAS_FLAG);
        lbl.setColor(0xCCFFFFFF);
        lbl.setTextSize(28f);
        lbl.setTextAlign(Paint.Align.CENTER);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("انگشت را بردار تا تأیید شود", cx, cy + lr + 36f, lbl);
    }

    // ── Calibration UI ────────────────────────────────────────────────────

    private void drawCalibUI(Canvas canvas) {
        String[] fa = {"بالا چپ", "بالا راست", "پایین راست", "پایین چپ"};
        for (int i = 0; i < calibCorner; i++) {
            PointF p = table.corners[i];
            if (p == null) continue;
            cornerPaint.setColor(0xFF00FF88);
            canvas.drawCircle(p.x, p.y, 18f, cornerPaint);
            cornerPaint.setColor(Color.BLACK);
            canvas.drawCircle(p.x, p.y, 8f, cornerPaint);
        }
        if (calibCorner == 4) drawTableBorder(canvas);
        if (calibCorner < 4) {
            int W = getWidth(), H = getHeight();
            canvas.drawRect(0, H*0.38f, W, H*0.62f, dimPaint);
            textPaint.setTextSize(46f); textPaint.setColor(0xFF00FF88);
            canvas.drawText("گوشه " + (calibCorner+1) + " از 4", W/2f, H/2f-28, textPaint);
            textPaint.setTextSize(34f); textPaint.setColor(Color.WHITE);
            canvas.drawText(fa[calibCorner] + " میز را لمس کن", W/2f, H/2f+28, textPaint);
        }
    }

    // ── Table border ──────────────────────────────────────────────────────

    private void drawTableBorder(Canvas canvas) {
        if (!table.calibrated) return;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0x8800FF88); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4f);
        p.setPathEffect(new DashPathEffect(new float[]{20,10}, 0));
        Path path = new Path();
        path.moveTo(table.corners[0].x, table.corners[0].y);
        for (int i = 1; i < 4; i++) path.lineTo(table.corners[i].x, table.corners[i].y);
        path.close();
        canvas.drawPath(path, p);
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG); dot.setColor(0xFF00FF88);
        for (PointF c : table.corners) if (c != null) canvas.drawCircle(c.x, c.y, 8f, dot);
    }

    // ── Ball marker ───────────────────────────────────────────────────────

    private void drawBallMarker(Canvas canvas, float x, float y) {
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setStyle(Paint.Style.STROKE); ring.setStrokeWidth(3f);
        ring.setColor(0xCCFFFFFF); canvas.drawCircle(x, y, 22f, ring);
        ring.setColor(0xFF00FF88); canvas.drawCircle(x, y, 14f, ring);
        // centre dot
        ring.setStyle(Paint.Style.FILL); ring.setColor(0xFFFFFFFF);
        canvas.drawCircle(x, y, 4f, ring);
    }

    // ── Aim line with bounces ─────────────────────────────────────────────

    private void drawAimLine(Canvas canvas, float ox, float oy,
                              float dx, float dy, int color, int bounce) {
        if (bounce > 4) return;
        float mag = dist(0, 0, dx, dy);
        if (mag < 1) return;
        float nx = dx/mag, ny = dy/mag;
        float segLen = mag * (float)Math.pow(0.78, bounce);
        if (segLen < 20) return;

        int alpha = Math.max(40, 220 - bounce * 55);
        int c = (color & 0x00FFFFFF) | (alpha << 24);
        float sw = Math.max(2f, 6f - bounce * 1.2f);

        float[] hit = table.calibrated
                ? table.rayHitBorder(ox, oy, dx, dy, segLen)
                : screenBounce(ox, oy, nx, ny, segLen);

        float endX = ox + nx*segLen, endY = oy + ny*segLen;
        float rdx = 0, rdy = 0;
        boolean didHit = false;
        if (hit != null) {
            endX = hit[0]; endY = hit[1];
            rdx = hit[2]; rdy = hit[3]; didHit = true;
        }

        drawDash(canvas, ox, oy, endX, endY, c, sw, 18, 9);
        drawDot(canvas, endX, endY, c, Math.max(4f, 10f - bounce*2f));

        if (didHit && bounce < 4)
            drawAimLine(canvas, endX, endY, rdx, rdy, color, bounce + 1);
    }

    private float[] screenBounce(float ox, float oy, float nx, float ny, float len) {
        int W = getWidth(), H = getHeight();
        float tMin = len; int axis = -1; float t;
        if (nx<0){t=-ox/nx;     if(t>2f&&t<tMin){tMin=t;axis=0;}}
        if (nx>0){t=(W-ox)/nx;  if(t>2f&&t<tMin){tMin=t;axis=0;}}
        if (ny<0){t=-oy/ny;     if(t>2f&&t<tMin){tMin=t;axis=1;}}
        if (ny>0){t=(H-oy)/ny;  if(t>2f&&t<tMin){tMin=t;axis=1;}}
        if (axis==-1) return null;
        float mag=(float)Math.sqrt(nx*nx+ny*ny);
        return new float[]{ox+nx*tMin, oy+ny*tMin,
                (axis==0?-nx:nx)*mag, (axis==1?-ny:ny)*mag};
    }

    private void drawDash(Canvas c, float x1, float y1, float x2, float y2,
                           int color, float w, float dash, float gap) {
        Path p = new Path(); p.moveTo(x1,y1); p.lineTo(x2,y2);
        shadowPaint.setStrokeWidth(w+4);
        shadowPaint.setPathEffect(new DashPathEffect(new float[]{dash,gap},0));
        c.drawPath(p, shadowPaint);
        linePaint.setColor(color); linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(w); linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setPathEffect(new DashPathEffect(new float[]{dash,gap},0));
        c.drawPath(p, linePaint); linePaint.setPathEffect(null);
    }

    private void drawDot(Canvas c, float x, float y, int color, float r) {
        c.drawCircle(x, y, r+3, shadowPaint);
        dotPaint.setColor(color); dotPaint.setStyle(Paint.Style.FILL);
        c.drawCircle(x, y, r, dotPaint);
    }

    private void drawHint(Canvas canvas, String msg) {
        int W = getWidth(), H = getHeight();
        Paint bg = new Paint(); bg.setColor(0xBB000000);
        textPaint.setTextSize(34f);
        float tw = textPaint.measureText(msg);
        canvas.drawRoundRect(W/2f-tw/2f-20, H-130, W/2f+tw/2f+20, H-80, 16,16, bg);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(msg, W/2f, H-94, textPaint);
    }

    private static float dist(float x1,float y1,float x2,float y2){
        float dx=x2-x1,dy=y2-y1; return (float)Math.sqrt(dx*dx+dy*dy);
    }
}
