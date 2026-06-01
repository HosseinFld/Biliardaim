package com.billardaim;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Stores the 4 corners of the billiard table as a trapezoid.
 * Corners: topLeft, topRight, bottomRight, bottomLeft
 */
public class TableCalibration {

    private static final String PREFS = "table_calib";
    private static final String KEY_SET = "calibrated";

    // 4 corners in order: TL, TR, BR, BL
    public final PointF[] corners = new PointF[4];
    public boolean calibrated = false;

    /** Axis-aligned bounding rect of the 4 corners */
    public RectF bounds() {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (PointF p : corners) {
            if (p == null) continue;
            if (p.x < minX) minX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
        }
        return new RectF(minX, minY, maxX, maxY);
    }

    public static TableCalibration load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        TableCalibration c = new TableCalibration();
        c.calibrated = sp.getBoolean(KEY_SET, false);
        for (int i = 0; i < 4; i++) {
            float x = sp.getFloat("cx" + i, -1);
            float y = sp.getFloat("cy" + i, -1);
            c.corners[i] = (x >= 0 && y >= 0) ? new PointF(x, y) : null;
        }
        return c;
    }

    public void save(Context ctx) {
        SharedPreferences.Editor ed = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        ed.putBoolean(KEY_SET, true);
        for (int i = 0; i < 4; i++) {
            if (corners[i] != null) {
                ed.putFloat("cx" + i, corners[i].x);
                ed.putFloat("cy" + i, corners[i].y);
            }
        }
        ed.apply();
        calibrated = true;
    }

    public static void reset(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    /** Is point p inside the table polygon (4 corners)? */
    public boolean contains(float px, float py) {
        if (!calibrated) return true;
        // Point-in-polygon for convex quad
        for (int i = 0; i < 4; i++) {
            PointF a = corners[i];
            PointF b = corners[(i + 1) % 4];
            if (a == null || b == null) return true;
            float cross = (b.x - a.x) * (py - a.y) - (b.y - a.y) * (px - a.x);
            if (cross < 0) return false;
        }
        return true;
    }

    /**
     * Given ray from (ox,oy) in direction (dx,dy), find intersection with table border.
     * Returns [hitX, hitY, reflectedDx, reflectedDy] or null.
     */
    public float[] rayHitBorder(float ox, float oy, float dx, float dy, float maxLen) {
        if (!calibrated) return null;
        float mag = (float) Math.sqrt(dx * dx + dy * dy);
        if (mag == 0) return null;
        float nx = dx / mag, ny = dy / mag;

        float tMin = maxLen;
        float[] best = null;

        for (int i = 0; i < 4; i++) {
            PointF a = corners[i];
            PointF b = corners[(i + 1) % 4];
            if (a == null || b == null) continue;

            // Ray vs segment intersection
            float[] hit = raySegment(ox, oy, nx, ny, a.x, a.y, b.x, b.y);
            if (hit != null && hit[0] > 2f && hit[0] < tMin) {
                tMin = hit[0];
                // reflect direction off this wall
                float wx = b.x - a.x, wy = b.y - a.y;
                float wLen = (float) Math.sqrt(wx * wx + wy * wy);
                wx /= wLen; wy /= wLen;
                float dot = nx * wx + ny * wy;
                float rdx = (2 * dot * wx - nx) * mag;
                float rdy = (2 * dot * wy - ny) * mag;
                best = new float[]{ox + nx * tMin, oy + ny * tMin, rdx, rdy};
            }
        }
        return best;
    }

    /** Returns [t, u] for ray-segment intersection, or null. t=distance along ray. */
    private static float[] raySegment(float ox, float oy, float dx, float dy,
                                       float ax, float ay, float bx, float by) {
        float ex = bx - ax, ey = by - ay;
        float denom = dx * ey - dy * ex;
        if (Math.abs(denom) < 1e-6f) return null;
        float fx = ax - ox, fy = ay - oy;
        float t = (fx * ey - fy * ex) / denom;
        float u = (fx * dy - fy * dx) / denom;
        if (t > 0 && u >= 0 && u <= 1) return new float[]{t, u};
        return null;
    }
}
