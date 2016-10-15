package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
/**
 * 豎值SeekBar
 * 參考一下網上的例子，但是有個bug和一個需要注意的點
 * 1.bug：拖動的時候沒問題，當調用setProgress（）時，滑塊移動不跟進度條移動
 * 2.注意：因為豎直seekbar是交換了寬高，所以設置padding時，設置 android:paddingLeft="20dp"
 *        android:paddingRight="20dp"，實際是設置bottom和top的padding。所以要想設置底部
 *        和上部的寬高就設置左右padding，解決滑塊顯示不全問題。
 * @author malong
 *
 */
public class VerticalSeekBar extends SeekBar {

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalSeekBar(Context context) {
		super(context);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		canvas.rotate(-90);
		canvas.translate(-getHeight(), 0);
		super.onDraw(canvas);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
	                                      int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec,widthMeasureSpec );
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(!isEnabled()) {
			return false;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN :
		case MotionEvent.ACTION_MOVE :
		case MotionEvent.ACTION_UP :
			setProgress(getMax()-(int) (getMax() * event.getY() / getHeight()));
			onSizeChanged(getWidth(), getHeight(), 0, 0);
			break;

		case MotionEvent.ACTION_CANCEL:
			break;
		}

		return true;
	}

	// 解決調用setProgress（）方法時滑塊不跟隨的bug
	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);

	}
}