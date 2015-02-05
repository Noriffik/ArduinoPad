package com.dev.aproschenko.arduinocontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

public class OrientationView extends View
{
    private static final String TAG = "OrientationView";
    private static final boolean D = true;

    private Paint paint = new Paint();
    private int orientation = LinearLayout.HORIZONTAL;
    private int gravity = Gravity.LEFT;
    private int value = 0;

    public static int MAX_VALUE = 15;
    public static int MAX_POINTS = 5;

    private static int START_COLOR = Color.GREEN;
    private static int END_COLOR = Color.RED;

    private int marginSize = 0;
    public void setMarginSize(int marginSize) { this.marginSize = marginSize; }

    public OrientationView(Context context)
    {
        super(context);
        setupPaint();
    }

    public OrientationView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setupPaint();
    }

    public OrientationView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setupPaint();
    }

    public void setGravity(int gravity) { this.gravity = gravity; }
    public void setOrientation(int orientation) { this.orientation = orientation; }
    public void setValue(int value)
    {
        if (this.value != value)
        {
            this.value = value;
            invalidate();
        }
    }

    private void setupPaint()
    {
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
    }

    private void drawRect(Canvas canvas, int x1, int y1, int x2, int y2, String name, int value)
    {
        if (D)
            Log.d(TAG, String.format("%s (%d) - x1=%d y1=%d x2=%d y2=%d", name, value, x1, y1, x2, y2));

        Shader shader;
        if (orientation == LinearLayout.HORIZONTAL)
        {
            if (gravity == Gravity.RIGHT)
                shader = new LinearGradient(0, 0, getRight(), 0, END_COLOR, START_COLOR, Shader.TileMode.CLAMP);
            else
                shader = new LinearGradient(0, 0, getRight(), 0, START_COLOR, END_COLOR, Shader.TileMode.CLAMP);
        }
        else
        {
            if (gravity == Gravity.RIGHT)
                shader = new LinearGradient(0, 0, 0, getHeight(), END_COLOR, START_COLOR, Shader.TileMode.CLAMP);
            else
                shader = new LinearGradient(0, 0, 0, getHeight(), START_COLOR, END_COLOR, Shader.TileMode.CLAMP);
        }

        Paint paint = new Paint();
        paint.setShader(shader);
        canvas.drawRect(new RectF(x1, y1, x2, y2), paint);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        paint.setColor(Color.RED);
        int savedValue = value > 0 ? value : -value;

        if (orientation == LinearLayout.HORIZONTAL)
        {
            int size1 = getRight() / MAX_POINTS;
            int cur = savedValue / (MAX_VALUE / MAX_POINTS);
            if (gravity == Gravity.RIGHT)
            {
                if (value >= 0)
                {
                    drawRect(canvas, (MAX_POINTS - cur) * size1, 0, getRight(), getHeight(), "right", savedValue);
                }
            }
            else //LEFT
            {
                if (value <= 0)
                {
                    drawRect(canvas, 0, 0, cur * size1, getHeight(), "left", savedValue);
                }
            }
        }
        else //vertical
        {
            int size1 = getBottom() / MAX_POINTS;
            int cur = savedValue / (MAX_VALUE / MAX_POINTS);
            if (gravity == Gravity.RIGHT)
            {
                if (value >= 0)
                {
                    drawRect(canvas, 0, (MAX_POINTS - cur) * size1, getRight(), getHeight(), "back", savedValue);
                }
            }
            else //LEFT
            {
                if (value <= 0)
                {
                    drawRect(canvas, 0, 0, getRight(), cur * size1, "forward", savedValue);
                }
            }
        }
    }
}
