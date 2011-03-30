package com.happytap.bangbang;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 2/9/11
 * Time: 7:59 PM
 */

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;


@Deprecated
public class RotateTextView extends TextView
{

        public RotateTextView(Context context, AttributeSet attributes) {
                super(context, attributes);

        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//              setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
                setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
        }


        @Override
        protected
        void onDraw(Canvas canvas) {
                canvas.save();

                Log.d("Foo", "Canvas width: " + canvas.getWidth() + "; height: " + canvas.getHeight());
                Log.d("Foo", "View width: " + getWidth() + "; height: " + getHeight());

                canvas.translate(-getHeight()/2f, -getWidth()/2f);
                canvas.rotate(-90);

                super.onDraw(canvas);

                canvas.restore();
        }
}
