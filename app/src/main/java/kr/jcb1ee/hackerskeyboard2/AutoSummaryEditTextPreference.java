package kr.jcb1ee.hackerskeyboard2;

import android.content.Context;
import androidx.preference.EditTextPreference;
import android.util.AttributeSet;

public class AutoSummaryEditTextPreference extends EditTextPreference {

    public AutoSummaryEditTextPreference(Context context) {
        super(context);
    }

    public AutoSummaryEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoSummaryEditTextPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        setSummary(text);
    }
}
