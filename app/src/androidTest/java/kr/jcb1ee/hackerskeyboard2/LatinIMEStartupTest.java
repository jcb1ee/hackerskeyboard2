package kr.jcb1ee.hackerskeyboard2;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LatinIMEStartupTest {

    /**
     * Replicates LatinIME's PendingIntent creation (lines 495, 499-500).
     */
    @Test
    public void notificationPendingIntentCreationSucceeds() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(NotificationReceiver.ACTION_SHOW);
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);
        assertNotNull(pi);
    }

    /**
     * Replicates LatinIME's receiver registration (lines 415, 434, 492).
     * Uses bare registerReceiver() to mirror the current broken code — RED on API 34+.
     */
    @Test
    public void notificationReceiverRegistrationSucceeds() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context ctx, Intent intent) {}
        };
        IntentFilter filter = new IntentFilter(NotificationReceiver.ACTION_SHOW);
        filter.addAction(NotificationReceiver.ACTION_SETTINGS);
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        context.unregisterReceiver(receiver);
    }
}
