package com.example.android.BluetoothChat;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.View;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 2/5/11
 * Time: 7:06 PM
 */
public class HolsterWeaponOverlayView extends View {

    Context context;

    public HolsterWeaponOverlayView(Context context) {
        super(context);
        this.context = context;

        invalidate();
    }

    private static final String point = "POINT YOUR";
    private static final String pistol = "PISTOL";
    private static final String towards = "AT THE GROUND!";

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.argb(200,200,200,200));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getRight(), getBottom(), paint);
        Paint.FontMetrics fm = paint.getFontMetrics();
        Typeface tf = paint.getTypeface();
        paint.setColor(Color.parseColor("#663300"));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setTextSize(45);
        Rect rect = new Rect();
        paint.getTextBounds(point, 0, point.length(), rect);
        Log.i("holster", rect.toShortString());
        canvas.translate(getWidth(), getHeight());
        canvas.rotate(-180);
        int diff = (getWidth() - rect.width())/2;
        int yDiff = 40 + rect.height()/2;
        canvas.drawText(point, diff, yDiff, paint);
        //canvas.translate(0, rect.height());
        paint.setTextSize(80);
        paint.getTextBounds(pistol,0, pistol.length(), rect);
        diff = (getWidth() - rect.width())/2;
        yDiff = (getHeight()/2 + rect.height()/2);

        canvas.drawText(pistol,diff,yDiff,paint);
        paint.setTextSize(45);
        paint.getTextBounds(towards,0,towards.length(),rect);
        yDiff = getHeight()-40+(rect.height()/2);
        diff = (getWidth() - rect.width())/2;
        canvas.drawText(towards,diff,yDiff,paint);
    }
}
