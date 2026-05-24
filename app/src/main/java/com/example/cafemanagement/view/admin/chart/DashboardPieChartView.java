package com.example.cafemanagement.view.admin.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
public class DashboardPieChartView extends View {

    private final List<Slice> slices = new ArrayList<>();
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    public DashboardPieChartView(Context context) {
        super(context);
        init();
    }

    public DashboardPieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setColor(0xFF5D4037);
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(float[] values, int[] colors, String[] labels) {
        slices.clear();
        float total = 0;
        for (float v : values) total += v;
        if (total <= 0) {
            slices.add(new Slice(1f, 0xFFE0E0E0, "Chưa có dữ liệu"));
        } else {
            for (int i = 0; i < values.length; i++) {
                if (values[i] > 0) {
                    slices.add(new Slice(values[i], colors[i],
                            labels != null && i < labels.length ? labels[i] : ""));
                }
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (slices.isEmpty()) return;

        float total = 0;
        for (Slice s : slices) total += s.value;

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f - 12f;
        float radius = Math.min(cx, cy) - 24f;
        oval.set(cx - radius, cy - radius, cx + radius, cy + radius);

        float start = -90f;
        for (Slice slice : slices) {
            float sweep = slice.value / total * 360f;
            arcPaint.setColor(slice.color);
            canvas.drawArc(oval, start, sweep, true, arcPaint);
            start += sweep;
        }

        arcPaint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx, cy, radius * 0.55f, arcPaint);

        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        float legendY = getHeight() - 28f;
        float legendX = 16f;
        float itemW = (getWidth() - 32f) / Math.max(slices.size(), 1);
        int i = 0;
        for (Slice slice : slices) {
            float x = legendX + itemW * i + 8;
            arcPaint.setColor(slice.color);
            canvas.drawCircle(x + 8, legendY, 8, arcPaint);
            int pct = Math.round(slice.value / total * 100f);
            canvas.drawText(slice.label + " " + pct + "%", x + 22, legendY + 8, textPaint);
            i++;
        }
    }

    private static class Slice {
        final float value;
        final int color;
        final String label;

        Slice(float value, int color, String label) {
            this.value = value;
            this.color = color;
            this.label = label;
        }
    }
}
