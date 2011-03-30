package com.happytap.bangbang;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 2/5/11
 * Time: 7:06 PM
 */
public class HolsterWeaponOverlayView extends View {
	
	private static final long serialVersionUID = 1L;

    public HolsterWeaponOverlayView(Context context) {
        super(context);        
    }
    
    

    public HolsterWeaponOverlayView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public HolsterWeaponOverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
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
        //Paint.FontMetrics fm = paint.getFontMetrics();
        //Typeface tf = paint.getTypeface();
        paint.setColor(Color.parseColor("#663300"));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setTextSize(45);
        Rect rect = new Rect();
        paint.getTextBounds(point, 0, point.length(), rect);
        Log.i("holster", rect.toShortString());                
        int diff = (getHeight() - rect.width())/2;
        int yDiff = 40 + rect.height()/2;
        
        
        canvas.translate(0,getHeight());
        canvas.rotate(-90);
        
        /** DO THIS IF YOUR TRANSLATION IS WHACK
        paint.setColor(Color.RED);
        canvas.drawRect(0, 0, getHeight(), getWidth(), paint);        
        paint.setColor(Color.argb(200,200,200,200));
        **/
        canvas.drawText(point, diff, yDiff, paint);
        //canvas.translate(0, rect.height());
        paint.setTextSize(80);
        paint.getTextBounds(pistol,0, pistol.length(), rect);
        diff = (getHeight() - rect.width())/2;
        yDiff = (getWidth()/2 + rect.height()/2);

        canvas.drawText(pistol,diff,yDiff,paint);
        paint.setTextSize(45);
        paint.getTextBounds(towards,0,towards.length(),rect);
        yDiff = getWidth()-40+(rect.height()/2);
        diff = (getHeight() - rect.width())/2;
        canvas.drawText(towards,diff,yDiff,paint);
    }
}
