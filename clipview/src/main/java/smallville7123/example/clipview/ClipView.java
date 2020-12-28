package smallville7123.example.clipview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

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
    FrameLayout content;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
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

        FrameLayout A = new FrameLayout(context, attrs);
        A.setBackgroundColor(Color.GREEN);
        A.setLayoutParams(
                new ViewGroup.MarginLayoutParams(
                        1000,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        FrameLayout B = new FrameLayout(context, attrs);
        B.setBackgroundColor(Color.RED);
        B.setLayoutParams(
                new ViewGroup.MarginLayoutParams(
                        2200,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        B.setX(1200);
        addView(A);
        addView(B);
    }

    private static class Internal {}
    Internal Internal = new Internal();

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
        else addClip(child);
    }

    ArrayList<View> clips = new ArrayList<>();

    public void addClip(View view) {
        clips.add(view);
        content.addView(view);
    }

    private static final String TAG = "ClipView";

    private float relativeToViewX;
    private float relativeToViewY;
    private float relativeToViewScrollX;
    private float relativeToViewScrollY;

    boolean scrolling = false;
    boolean clipTouch = false;
    View touchedClip;
    float originalX;
    float downDX;
    float downRawX;
    float currentRawX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent() called with: event = [" + MotionEvent.actionToString(event.getAction()) + "]");
        currentRawX = event.getRawX();
        relativeToViewX = event.getX();
        relativeToViewScrollX = relativeToViewX + getScrollX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (View clip : clips) {
                    MarginLayoutParams params = (MarginLayoutParams) clip.getLayoutParams();
                    float clipStart = clip.getX();
                    float clipEnd = clipStart + clip.getWidth();
                    Log.d(TAG, "clipStart = [ " + clipStart + "]");
                    Log.d(TAG, "clipEnd = [ " + clipEnd + "]");
                    Log.d(TAG, "relativeToViewScrollX = [ " + relativeToViewScrollX + "]");
                    if (relativeToViewScrollX >= clipStart && relativeToViewScrollX <= clipEnd) {
                        clipTouch = true;
                        touchedClip = clip;
                        originalX = clipStart;
                        downRawX = currentRawX;
                        downDX = originalX - downRawX;
                        break;
                    }
                }
                if (!clipTouch) scrolling = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (clipTouch) clipTouch = false;
                else scrolling = false;
                break;
        }
        return clipTouch ? onClipTouchEvent(touchedClip, event) : super.onTouchEvent(event);
    }

    public boolean onClipTouchEvent(View clip, MotionEvent event) {
        Log.d(TAG, "onClipTouchEvent() called with: clip = [" + clip + "], event = [" + MotionEvent.actionToString(event.getAction()) + "]");
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            clip.animate().x(currentRawX + downDX).setDuration(0).start();
        }
        return true;
    }
}