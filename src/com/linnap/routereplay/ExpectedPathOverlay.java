
package com.linnap.routereplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.linnap.routereplay.replay.Fix;
import com.linnap.routereplay.replay.Replay;

class ExpectedPathOverlay extends Overlay {
	public static final int EXPECTED_FIX_CROSS_PIXELS = 10;
	
	Replay replay;
	
	public ExpectedPathOverlay(Replay replay) {
		this.replay = replay;
	}
	
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		
		if (!shadow) {			
			Projection projection = mapView.getProjection();
			
			Paint paint = new Paint();
			paint.setColor(Color.RED);
	        paint.setStrokeWidth(1.0f);

	        Point lastPoint = null;
	        for (Fix f : replay.fullgps) {
	        	Point currentPoint = new Point();
	        	projection.toPixels(f.geoPoint, currentPoint);
	        	
	        	if (lastPoint != null) {
	        		canvas.drawLine(lastPoint.x, lastPoint.y, currentPoint.x, currentPoint.y, paint);
	        	}
	        	lastPoint = currentPoint;
	        }
	        
	        paint.setStrokeWidth(2.0f);
	        for (Fix f : replay.expected) {
	        	Point point = new Point();
	        	projection.toPixels(f.geoPoint, point);
	        	
	        	drawCross(canvas, point, paint);
	        }
		}
	}
	
	public void drawCross(Canvas canvas, Point point, Paint paint) {
        canvas.drawLine(point.x - EXPECTED_FIX_CROSS_PIXELS, point.y - EXPECTED_FIX_CROSS_PIXELS, point.x + EXPECTED_FIX_CROSS_PIXELS, point.y + EXPECTED_FIX_CROSS_PIXELS, paint);
        canvas.drawLine(point.x + EXPECTED_FIX_CROSS_PIXELS, point.y - EXPECTED_FIX_CROSS_PIXELS, point.x - EXPECTED_FIX_CROSS_PIXELS, point.y + EXPECTED_FIX_CROSS_PIXELS, paint);
	}
}