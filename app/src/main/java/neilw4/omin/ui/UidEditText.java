package neilw4.omin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class UidEditText extends EditText {
    public UidEditText(Context context) {
        super(context);
    }

    public UidEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UidEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        View parent = (ViewGroup) getParent();
        if (parent != null) {
            setMinimumWidth(parent.getMeasuredWidth());
        }
    }

}