package com.example.android.BluetoothChat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 2/10/11
 * Time: 9:22 PM
 */
public class WinLostGameOverlayView extends View {

    Context context;

    public WinLostGameOverlayView(Context context) {
        super(context);
        this.context = context;

        invalidate();
    }

    private String winLossText = "You Lose";

    public String getWinLossText() {
        return winLossText;
    }

    public void setWinLossText(String winLossText) {
        this.winLossText = winLossText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();

        paint.setColor(Color.argb(175, 230, 41, 41));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getRight(), getBottom(), paint);

        canvas.translate(getWidth(), getHeight());
        canvas.rotate(-180);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setTextSize(85);
        Rect rect = new Rect();
        paint.getTextBounds(winLossText, 0, winLossText.length(), rect);
        int diff = (getWidth() - rect.width())/2;
        int yDiff = (getHeight()/2 + rect.height()/2);;
        paint.setColor(Color.parseColor("#663300"));
        canvas.drawText(winLossText,diff,yDiff,paint);


    }
}