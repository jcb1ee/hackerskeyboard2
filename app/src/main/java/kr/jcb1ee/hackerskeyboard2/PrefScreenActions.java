/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class PrefScreenActions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static void removeIconSpace(androidx.preference.PreferenceGroup group) {
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                androidx.preference.Preference p = group.getPreference(i);
                p.setIconSpaceReserved(false);
                if (p instanceof androidx.preference.PreferenceGroup)
                    removeIconSpace((androidx.preference.PreferenceGroup) p);
            }
        }

        @Override
        public void onDisplayPreferenceDialog(androidx.preference.Preference preference) {
            if (preference instanceof SeekBarPreference) {
                SeekBarPreferenceDialogFragment f =
                        SeekBarPreferenceDialogFragment.newInstance(preference.getKey());
                f.setTargetFragment(this, 0);
                f.show(getParentFragmentManager(), "SeekBarPreferenceDialogFragment");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.prefs_actions);
            removeIconSpace(getPreferenceScreen());
            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            prefs.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                    this);
            super.onDestroy();
        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            (new BackupManager(requireContext())).dataChanged();
        }
    }
}
