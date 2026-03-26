/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package kr.jcb1ee.hackerskeyboard2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Main extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TextView version = findViewById(R.id.main_version);
        version.setText("Version " + getString(R.string.auto_version));

        final Button setup1 = findViewById(R.id.main_setup_btn_configure_imes);
        setup1.setOnClickListener(v ->
            startActivityForResult(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS), 0)
        );

        final EditText hiddenInput = findViewById(R.id.main_hidden_input);

        final Button setup2 = findViewById(R.id.main_setup_btn_set_ime);
        setup2.setOnClickListener(v -> {
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            hiddenInput.requestFocus();
            mgr.showSoftInput(hiddenInput, InputMethodManager.SHOW_FORCED);
            hiddenInput.post(() -> mgr.showInputMethodPicker());
        });

        final Button setup4 = findViewById(R.id.main_setup_btn_input_lang);
        setup4.setOnClickListener(v ->
            startActivityForResult(new Intent(this, InputLanguageSelection.class), 0)
        );

        final Button setup5 = findViewById(R.id.main_setup_btn_settings);
        setup5.setOnClickListener(v ->
            startActivityForResult(new Intent(this, LatinIMESettings.class), 0)
        );
    }
}
