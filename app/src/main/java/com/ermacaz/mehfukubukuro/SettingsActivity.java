package com.ermacaz.mehfukubukuro;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.widget.Toast;

import net.margaritov.preference.colorpicker.ColorPickerPreference;


import java.util.Calendar;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = true;

    private PendingIntent pendingIntent;
    private AlarmManager manager;
    private SharedPreferences mPreferences;
    private boolean mEnabled;
    private TimePreference mTimePreference;



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //allows network calls on main thread - image would not load in time for notification if on separate thread
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new     StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        setupSimplePreferencesScreen();
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    }

    public void startAlarm() {
        if (!mEnabled)
        {
            return;
        }
        Intent alarmIntent = new Intent(SettingsActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(SettingsActivity.this, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        Calendar cal = mTimePreference.getCalendar();

        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        manager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    /*
     * immediate call to get Meh status.  Called form check now button.  Should not interfere with set alarm
     */
    public void startNowOnce() {
        //create own that does not interfere with alarm
        Intent alarmIntent = new Intent(SettingsActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntentOnce = PendingIntent.getBroadcast(SettingsActivity.this, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
         manager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntentOnce);
    }

    public void stopAlarm() {
        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);
        mEnabled =  mPreferences.getBoolean("enabled", false);
        final SwitchPreference enableSwitch = (SwitchPreference)getPreferenceManager().findPreference("enableSwitch");
        enableSwitch.setChecked(mEnabled);

        enableSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("enabled", (boolean)newValue);
                editor.commit();
                mEnabled = (boolean)newValue;
                if (mEnabled)
                {
                    startAlarm();
                    Toast.makeText(getApplicationContext(), "Notification on", Toast.LENGTH_SHORT).show();
                }
                if (!mEnabled)
                {
                    stopAlarm();
                    Toast.makeText(getApplicationContext(), "Notification off", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        mTimePreference = (TimePreference)getPreferenceManager().findPreference("updateTimeKey");
        mTimePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("lookup_time", mTimePreference.getSummary().toString());
                editor.commit();
                startAlarm();
                return true;
            }
        });

        Preference syncPref = getPreferenceManager().findPreference("syncNow");
        syncPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startNowOnce();
                return true;
            }
        });

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
//        bindPreferenceSummaryToValue(findPreference("notifications_ringtone"));


        CheckBoxPreference soundPreference = (CheckBoxPreference)getPreferenceManager().findPreference("notifications_sound");
        soundPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("soundEnabled", (boolean)newValue);
                editor.commit();
                return true;
            }
        });

        CheckBoxPreference vibratePreference = (CheckBoxPreference)getPreferenceManager().findPreference("notifications_vibrate");
        vibratePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("vibrateEnabled", (boolean)newValue);
                editor.commit();
                if ((boolean)newValue) {
                    Vibrator vb = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    vb.vibrate(500);
                }
                return true;
            }
        });

        RingtonePreference ringtonePreference = (RingtonePreference)getPreferenceManager().findPreference("notifications_ringtone");
        ringtonePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("ringtone", (String)newValue);
                editor.commit();

                //update ui
                String stringValue = newValue.toString();
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
                return true;
            }
        });

        ((ColorPickerPreference)findPreference("color1")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                String colorStr = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
                String colorStr = ColorPickerPreference.convertToRGB(Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(colorStr);
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("textColor", colorStr);
                editor.commit();
                return true;
            }

        });



    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
