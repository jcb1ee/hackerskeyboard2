/*
 * Transparent trampoline activity that handles RECORD_AUDIO runtime permission
 * and the RecognizerIntent dialog, then broadcasts the result back to LatinIME.
 */
package kr.jcb1ee.hackerskeyboard2;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import java.util.ArrayList;

public class VoiceInputActivity extends Activity {

    static final String ACTION_VOICE_RESULT = "kr.jcb1ee.hackerskeyboard2.VOICE_RESULT";
    static final String EXTRA_VOICE_TEXT    = "voice_text";
    static final String EXTRA_LANGUAGE      = "language";

    private static final int REQ_PERMISSION = 1;
    private static final int REQ_VOICE      = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQ_PERMISSION);
        } else {
            startVoiceRecognition();
        }
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        String lang = getIntent().getStringExtra(EXTRA_LANGUAGE);
        if (lang != null) {
            // Normalize: "ko_KR" → "ko-KR" (BCP-47 format expected by the recognizer)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang.replace('_', '-'));
        }
        try {
            startActivityForResult(intent, REQ_VOICE);
        } catch (Exception e) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        if (requestCode == REQ_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecognition();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_VOICE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                Intent broadcast = new Intent(ACTION_VOICE_RESULT);
                broadcast.setPackage(getPackageName());
                broadcast.putExtra(EXTRA_VOICE_TEXT, results.get(0));
                sendBroadcast(broadcast);
            }
        }
        finish();
    }
}
