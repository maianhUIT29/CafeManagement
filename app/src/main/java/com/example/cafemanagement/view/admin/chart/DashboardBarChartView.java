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

public class DashboardBarChartView extends View {

    private final List<Bar> bars = new ArrayList<>();
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();
    public DashboardBarChartView(Context context) {
        super(context);
        init();
    }

    public DashboardBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        labelPaint.setColor(0xFF8D6E63);
        labelPaint.setTextSize(28f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(0xFF3E2723);
        valuePaint.setTextSize(26f);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        gridPaint.setColor(0xFFEFEBE9);
        gridPaint.setStrokeWidth(2f);
    }

    public void setData(float[] values, String[] labels, int[] colors) {
        bars.clear();
        int n = values != null ? values.length : 0;
        for (int i = 0; i < n; i++) {
            int color = colors != null && i < colors.length ? colors[i] : 0xFF5D4037;
            String label = labels != null && i < labels.length ? labels[i] : "";
            bars.add(new Bar(values[i], label, color));
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bars.isEmpty()) return;

        float max = 1f;
        for (Bar b : bars) max = Math.max(max, b.value);
        max = (float) (Math.ceil(max));

        int padL = 48, padR = 24, padT = 24, padB = 56;
        float chartW = getWidth() - padL - padR;
        float chartH = getHeight() - padT - padB;

        for (int i = 0; i <= 4; i++) {
            float y = padT + chartH * i / 4f;
            canvas.drawLine(padL, y, padL + chartW, y, gridPaint);
        }

        float barW = chartW / bars.size() * 0.55f;
        float gap = chartW / bars.size();

        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            float left = padL + gap * i + (gap - barW) / 2f;
            float barH = bar.value / max * chartH;
            float top = padT + chartH - barH;
            barRect.set(left, top, left + barW, padT + chartH);
            barPaint.setColor(bar.color);
            canvas.drawRoundRect(barRect, 12, 12, barPaint);

            if (bar.value > 0) {
                canvas.drawText(String.valueOf((int) bar.value),
                        left + barW / 2f, top - 8, valuePaint);
            }

            String label = bar.label.length() > 7 ? bar.label.substring(0, 6) + "…" : bar.label;
            canvas.drawText(label, left + barW / 2f, getHeight() - 16f, labelPaint);
        }
    }

    private static class Bar {
        final float value;
        final String label;
        final int color;

        Bar(float value, String label, int color) {
            this.value = value;
            this.label = label;
            this.color = color;
        }
    }
}
