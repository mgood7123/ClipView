package smallville7123.example.clipview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ClipView extends HorizontalScrollView {
    public ClipView(Context context) {
        super(context);
        init(context, null);
    }

    public ClipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ClipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    Context mContext;
    AttributeSet mAttrs;
    FrameLayout content;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mAttrs = attrs;

        // make scroll view match parent width
        setFillViewport(true);

        FrameLayout frame = new FrameLayout(context, attrs);
        content = frame;
        frame.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        frame.setTag(Internal);
        addView(frame);
        Clip A = newClip();
        A.setColor(Color.GREEN);
        A.setX(800);
        A.setWidth(1000);
        addClip(A);
    }

    class Clip {
        View content;

        Clip(Context context) {
            content = new FrameLayout(context);
            setWidth(100);
        }

        Clip(Context context, AttributeSet attrs) {
            content = new FrameLayout(context, attrs);
            setWidth(100);
        }

        public Clip(View content) {
            content = content;
        }

        public void setColor(@ColorInt int color) {
            content.setBackgroundColor(color);
        }

        public void setX(float x) {
            ViewGroup.LayoutParams p = content.getLayoutParams();
            if (p != null) {
                if (p instanceof MarginLayoutParams) {
                    ((MarginLayoutParams) p).leftMargin = (int) x;
                    content.setLayoutParams(p);
                } else {
                    throw new RuntimeException("layout is not an instance of MarginLayoutParams");
                }
            } else {
                content.setLayoutParams(
                        new MarginLayoutParams(
                                MATCH_PARENT,
                                MATCH_PARENT
                        ) {
                            {
                                leftMargin = (int) x;
                            }
                        }
                );
            }
        }

        public float getX() {
            return content.getX();
        }

        public void setWidth(int width) {
            ViewGroup.LayoutParams p = content.getLayoutParams();
            if (p != null) {
                if (p instanceof MarginLayoutParams) {
                    ((MarginLayoutParams) p).width = width;
                    content.setLayoutParams(p);
                } else {
                    throw new RuntimeException("layout is not an instance of MarginLayoutParams");
                }
            } else {
                content.setLayoutParams(
                        new MarginLayoutParams(
                                width,
                                MATCH_PARENT
                        )
                );
            }
        }

        public int getWidth() {
            return content.getWidth();
        }

        public ViewPropertyAnimator animate() {
            return content.animate();
        }
    }

    Clip newClip() {
        return new Clip(mContext, mAttrs);
    };

    private static class Internal {}
    Internal Internal = new Internal();
    ArrayList<Clip> clips = new ArrayList<>();

    public void addClip(Clip clip) {
        clips.add(clip);
        content.addView(clip.content);
    }

    private static final String TAG = "ClipView";

    private float relativeToViewX;
    private float relativeToViewY;
    private float relativeToViewScrollX;
    private float relativeToViewScrollY;

    boolean scrolling = false;
    boolean clipTouch = false;
    Clip touchedClip;
    float downDX;
    float downRawX;
    float currentRawX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent() called with: event = [" + MotionEvent.actionToString(event.getAction()) + "]");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (Clip clip : clips) {
                    boolean ret = onClipTouchEvent(clip, event);
                    if (ret) {
                        clipTouch = true;
                        touchedClip = clip;
                        return ret;
                    }
                }
                scrolling = true;
                return super.onTouchEvent(event);
            case MotionEvent.ACTION_MOVE:
                return clipTouch ? onClipTouchEvent(touchedClip, event) : super.onTouchEvent(event);
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (clipTouch) {
                    boolean ret = onClipTouchEvent(touchedClip, event);
                    clipTouch = false;
                    return ret;
                }
                scrolling = false;
                return super.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public float touchZoneWidthLeft = 80.0f;
    public float touchZoneWidthRight = 80.0f;
    public float touchZoneWidth;

    Paint highlightPaint;
    Paint touchZonePaint;
    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        int width = getWidth();
        int height = getHeight();
//        if (draggable != null) {
//            if (draggable.isResizing) {
//                drawHighlight(canvas, width, height, highlightPaint);
//            }
//        }
//        drawTouchZones(canvas, width, height, touchZonePaint);
    }

    boolean isResizing;
    boolean isDragging;
    float clipOriginalStart;
    float clipOriginalWidth;
    float clipOriginalEnd;
    boolean resizingLeft;
    boolean resizingRight;
    float widthLeft = 30.0f;
    float widthRight = 30.0f;

    public boolean onClipTouchEvent(Clip clip, MotionEvent event) {
        currentRawX = event.getRawX();
        relativeToViewX = event.getX() + getScrollX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                return false;
            case MotionEvent.ACTION_MOVE:
                if (!isResizing && isDragging) {
                    if (currentRawX + downDX >= 0) {
                        clip.setX(currentRawX + downDX);
                    } else {
                        clip.setX(0);
                    }
                    return true;
                }
                return false;
            case MotionEvent.ACTION_DOWN:
                isDragging = false;
                isResizing = false;
                clipOriginalStart = clip.getX();
                clipOriginalWidth = clip.getWidth();
                clipOriginalEnd = clipOriginalStart + clipOriginalWidth;
                downRawX = currentRawX;
                resizingLeft = false;
                resizingRight = false;
                Log.d(TAG, "relativeToViewX = [ " + relativeToViewX + "]");
                Log.d(TAG, "clipOriginalStart = [ " + clipOriginalStart + "]");
                Log.d(TAG, "clipOriginalEnd = [ " + clipOriginalEnd + "]");
                if (relativeToViewX < widthLeft) {
//                    resizingLeft = true;
//                    isResizing = true;
                } else if ((clip.content.getRight() - relativeToViewX) < widthRight) {
//                    resizingRight = true;
//                    isResizing = true;
                } else if (relativeToViewX >= clipOriginalStart && relativeToViewX <= clipOriginalEnd) {
                    isDragging = true;
                }
                if (isResizing || isDragging) {
                    Log.d(TAG, "isResizing = [ " + isResizing + "]");
                    Log.d(TAG, "isDragging = [ " + isDragging + "]");
                    clip.content.invalidate();
                    downDX = clipOriginalStart - downRawX;
                    return true;
                }
            default:
                return false;
        }
    }








    /**
     * <p>Adds a child view. If no layout parameters are already set on the child, the
     * default parameters for this ViewGroup are set on the child.</p>
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(android.graphics.Canvas)}, {@link #onDraw(android.graphics.Canvas)},
     * {@link #dispatchDraw(android.graphics.Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     *
     * @see #generateDefaultLayoutParams()
     */
    @Override
    public void addView(View child) {
        addView(child, -1);
    }

    /**
     * Adds a child view. If no layout parameters are already set on the child, the
     * default parameters for this ViewGroup are set on the child.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(android.graphics.Canvas)}, {@link #onDraw(android.graphics.Canvas)},
     * {@link #dispatchDraw(android.graphics.Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     * @param index the position at which to add the child
     *
     * @see #generateDefaultLayoutParams()
     */
    @Override
    public void addView(View child, int index) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = generateDefaultLayoutParams();
            if (params == null) {
                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
            }
        }
        addView(child, index, params);
    }

    /**
     * Adds a child view with this ViewGroup's default layout parameters and the
     * specified width and height.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(android.graphics.Canvas)}, {@link #onDraw(android.graphics.Canvas)},
     * {@link #dispatchDraw(android.graphics.Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     */
    @Override
    public void addView(View child, int width, int height) {
        final ViewGroup.LayoutParams params = generateDefaultLayoutParams();
        params.width = width;
        params.height = height;
        addView(child, -1, params);
    }

    /**
     * Adds a child view with the specified layout parameters.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(android.graphics.Canvas)}, {@link #onDraw(android.graphics.Canvas)},
     * {@link #dispatchDraw(android.graphics.Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     * @param params the layout parameters to set on the child
     */
    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addView(child, -1, params);
    }

    /**
     * Adds a child view with the specified layout parameters.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(android.graphics.Canvas)}, {@link #onDraw(android.graphics.Canvas)},
     * {@link #dispatchDraw(android.graphics.Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     * @param index the position at which to add the child or -1 to add last
     * @param params the layout parameters to set on the child
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        Object tag = child.getTag();
        if (tag instanceof Internal) super.addView(child, index, params);
        else addClip(new Clip(child));
    }
}