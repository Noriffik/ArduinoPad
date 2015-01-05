package com.dev.aproschenko.arduinopad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
                    canvas.drawRect((MAX_POINTS - cur) * size1, 0, getRight(), getHeight(), paint);
                }
            }
            else //LEFT
            {
                if (value <= 0)
                {
                    canvas.drawRect(0, 0, cur * size1, getHeight(), paint);
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
                    canvas.drawRect(0, (MAX_POINTS - cur) * size1, getRight(), getHeight(), paint);
                }
            }
            else //LEFT
            {
                if (value <= 0)
                {
                    canvas.drawRect(0, 0, getRight(), cur * size1, paint);
                }
            }
        }
    }
}
