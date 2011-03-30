package com.happytap.bangbang;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
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
        this(context,null,0);
    }

    public WinLostGameOverlayView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public WinLostGameOverlayView(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	private String winLossText = "You Win";

    private Integer textColor = Color.BLUE;

    public Integer getTextColor() {
        return textColor;
    }

    public void setTextColor(Integer textColor) {
        this.textColor = textColor;
    }

    public String getWinLossText() {
        return winLossText;
    }

    public void setWinLossText(String winLossText) {
        this.winLossText = winLossText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setTextSize(85);
        Rect rect = new Rect();
        paint.getTextBounds(winLossText, 0, winLossText.length(), rect);
        canvas.translate(0, getHeight());
        canvas.rotate(-90);
        int xDiff = (getHeight() - rect.width())/2;
        int yDiff = (getWidth()/2 + rect.height()/2);
        paint.setColor(textColor);
        canvas.drawText(winLossText,xDiff,yDiff,paint);
    }
}
