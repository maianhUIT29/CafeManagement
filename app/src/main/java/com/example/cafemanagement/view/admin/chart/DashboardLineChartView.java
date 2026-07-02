package com.example.cafemanagement.view.admin.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Locale;

public class DashboardLineChartView extends View {

    private float[] values = new float[0];
    private final String[] dayLabels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    public DashboardLineChartView(Context context) {
        super(context);
        init();
    }

    public DashboardLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint.setColor(0xFF5D4037);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        fillPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(0xFFFFD54F);
        dotPaint.setStyle(Paint.Style.FILL);
        gridPaint.setColor(0xFFEFEBE9);
        gridPaint.setStrokeWidth(2f);
        labelPaint.setColor(0xFF8D6E63);
        labelPaint.setTextSize(26f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setValues(float[] values) {
        this.values = values != null ? values : new float[0];
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.length == 0) return;

        int padL = 56, padR = 24, padT = 20, padB = 48;
        float chartW = getWidth() - padL - padR;
        float chartH = getHeight() - padT - padB;

        float max = 0.5f;
        for (float v : values) max = Math.max(max, v);
        max = (float) (Math.ceil(max * 2) / 2f);

        for (int i = 0; i <= 4; i++) {
            float y = padT + chartH * i / 4f;
            canvas.drawLine(padL, y, padL + chartW, y, gridPaint);
            float val = max * (4 - i) / 4f;
            labelPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), "%.1f", val),
                    padL - 8, y + 8, labelPaint);
        }

        float stepX = values.length > 1 ? chartW / (values.length - 1) : chartW;

        linePath.reset();
        fillPath.reset();
        for (int i = 0; i < values.length; i++) {
            float x = padL + stepX * i;
            float y = padT + chartH - (values[i] / max * chartH);
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, padT + chartH);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        fillPath.lineTo(padL + stepX * (values.length - 1), padT + chartH);
        fillPath.close();

        fillPaint.setShader(new LinearGradient(0, padT, 0, padT + chartH,
                0x55FFD54F, 0x00FFD54F, Shader.TileMode.CLAMP));
        canvas.drawPath(fillPath, fillPaint);
        fillPaint.setShader(null);
        canvas.drawPath(linePath, linePaint);

        for (int i = 0; i < values.length; i++) {
            float x = padL + stepX * i;
            float y = padT + chartH - (values[i] / max * chartH);
            canvas.drawCircle(x, y, 10f, dotPaint);
            canvas.drawCircle(x, y, 5f, linePaint);
            labelPaint.setTextAlign(Paint.Align.CENTER);
            String day = i < dayLabels.length ? dayLabels[i] : "";
            canvas.drawText(day, x, getHeight() - 12f, labelPaint);
        }
    }
}
