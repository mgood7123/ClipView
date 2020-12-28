package smallville7123.example.clipview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

public class ClipView extends FrameLayout {
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

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        HorizontalScrollView h = new HorizontalScrollView(context, attrs);
        h.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        FrameLayout frame = new FrameLayout(context, attrs);
        frame.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        FrameLayout A = new FrameLayout(context, attrs);
        A.setBackgroundColor(Color.GREEN);
        A.setLayoutParams(
                new ViewGroup.MarginLayoutParams(
                        1000,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        frame.addView(A);

        FrameLayout B = new FrameLayout(context, attrs);
        B.setBackgroundColor(Color.RED);
        B.setLayoutParams(
                new ViewGroup.MarginLayoutParams(
                        1000,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ) {
                    {
                        setMargins(1200, 0, 0, 0);
                    }
                }
        );
        frame.addView(B);

        h.addView(frame);
        addView(h);
    }
}