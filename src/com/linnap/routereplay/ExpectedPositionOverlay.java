
package com.linnap.routereplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.linnap.routereplay.replay.Fix;
import com.linnap.routereplay.replay.Replay;

class ExpectedPositionOverlay extends Overlay {
	
	public static final int CROSS_PIXELS = 20;
	
	Fix currentFix = null;
	
	public void updateFix(Fix fix) {
		currentFix = fix;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow || currentFix == null)
			return;
		
		Projection projection = mapView.getProjection();
		Point point = new Point();
		projection.toPixels(currentFix.geoPoint, point);
		
		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
        paint.setStrokeWidth(8.0f);
        
        canvas.drawLine(point.x - CROSS_PIXELS, point.y - CROSS_PIXELS, point.x + CROSS_PIXELS, point.y + CROSS_PIXELS, paint);
        canvas.drawLine(point.x + CROSS_PIXELS, point.y - CROSS_PIXELS, point.x - CROSS_PIXELS, point.y + CROSS_PIXELS, paint);

        return;
	}
}