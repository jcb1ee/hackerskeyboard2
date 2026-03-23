package kr.jcb1ee.hackerskeyboard2;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.PreferenceDialogFragmentCompat;

public class SeekBarPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    public static SeekBarPreferenceDialogFragment newInstance(String key) {
        SeekBarPreferenceDialogFragment f = new SeekBarPreferenceDialogFragment();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        f.setArguments(args);
        return f;
    }

    private SeekBarPreference pref() {
        return (SeekBarPreference) getPreference();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SeekBarPreference p = pref();
        TextView minText = view.findViewById(R.id.seekMin);
        TextView maxText = view.findViewById(R.id.seekMax);
        TextView valText = view.findViewById(R.id.seekVal);
        SeekBar seek = view.findViewById(R.id.seekBarPref);

        valText.setText(p.formatFloatDisplay(p.mVal));
        minText.setText(p.formatFloatDisplay(p.mMin));
        maxText.setText(p.formatFloatDisplay(p.mMax));
        seek.setProgress(p.getProgressVal());

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {}
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float newVal = p.percentToSteppedVal(progress, p.mMin, p.mMax, p.mStep, p.mLogScale);
                    if (newVal != p.mVal) {
                        p.onChange(newVal);
                    }
                    p.setVal(newVal);
                    seekBar.setProgress(p.getProgressVal());
                }
                valText.setText(p.formatFloatDisplay(p.mVal));
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        pref().onDialogClose(positiveResult);
    }
}
