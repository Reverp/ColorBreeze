package com.reverp.colorbreeze;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.Set;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }



    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            final Preference enableAutoGrayscale = findPreference("enable_switch");
            enableAutoGrayscale.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if((boolean)o)
                    {
                        setGrayscaleAlarms(getActivity());
                    }
                    else
                    {
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(getActivity(), EnableGrayscaleService.class);
                        PendingIntent pendingIntent = PendingIntent.getService(getActivity(), 0, intent, 0);

                        Intent disableIntent = new Intent(getActivity(), DisableGrayscaleService.class);
                        PendingIntent disablePendingIntent = PendingIntent.getService(getActivity(), 0, disableIntent, 0);

                        alarmManager.cancel(pendingIntent);
                        alarmManager.cancel(disablePendingIntent);
                    }

                    return true;
                }
            });

            final Preference startTimePreference = findPreference("pref_start_time");

            int startTimeHour = Integer.parseInt(read("starttime_hour", "0"));
            int startTimeMinute = Integer.parseInt(read("starttime_minute", "0"));
            startTimePreference.setSummary(getTimeString(startTimeHour, startTimeMinute));
            startTimePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            save("starttime_hour", Integer.toString(selectedHour));
                            save("starttime_minute", Integer.toString(selectedMinute));

                            startTimePreference.setSummary(getTimeString(selectedHour, selectedMinute));
                            setGrayscaleAlarms(getActivity().getApplicationContext());
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                    return true;
                }
            });

            final Preference stopTimePreference = findPreference("pref_stop_time");

            int stopTimeHour = Integer.parseInt(read("stoptime_hour", "0"));
            int stopTimeMinute = Integer.parseInt(read("stoptime_minute", "0"));
            stopTimePreference.setSummary(getTimeString(stopTimeHour, stopTimeMinute));
            stopTimePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            save("stoptime_hour", Integer.toString(selectedHour));
                            save("stoptime_minute", Integer.toString(selectedMinute));

                            stopTimePreference.setSummary(getTimeString(selectedHour, selectedMinute));
                            setGrayscaleAlarms(getActivity().getApplicationContext());
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                    return true;
                }
            });

            final MultiSelectListPreference selectedDaysPreference = (MultiSelectListPreference) findPreference("pref_selected_days");
            String selectedDaysSummary = getSelectedDaysString(selectedDaysPreference.getValues());
            selectedDaysPreference.setSummary(selectedDaysSummary);
            selectedDaysPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String selectedDaysSummary = getSelectedDaysString((Set<String>)newValue);
                    selectedDaysPreference.setSummary(selectedDaysSummary);

                    return true;
                }
            });

        }

        public String getTimeString(int selectedHour, int selectedMinute)
        {
            String hourString = Integer.toString(selectedHour);
            if(selectedHour < 10)
            {
                hourString = "0" + hourString;
            }

            String minuteString = Integer.toString(selectedMinute);
            if(selectedMinute < 10)
            {
                minuteString = "0" + minuteString;
            }

            return hourString + ":" + minuteString;
        }

        private String getSelectedDaysString(Set<String> getValues)
        {
            // Easy and ugly fix to sort summary by weekday
            String[] namesOfDays = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

            StringBuilder days = new StringBuilder();
            for (String nameOfDay : namesOfDays ) {
                if(getValues.contains(nameOfDay)) {
                    if (days.length() > 0)
                    {
                        days.append(", ");
                    }

                    days.append(nameOfDay);
                }
            }

            return days.toString();
        }
        
        public void setGrayscaleAlarms(Context mContext) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(mContext, GrayscaleAlarmReceiver.class);
            intent.setAction(GrayscaleAlarmReceiver.ENABLE_GRAYSCALE_CODE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

            Intent disableIntent = new Intent(mContext, GrayscaleAlarmReceiver.class);
            disableIntent.setAction(GrayscaleAlarmReceiver.DISABLE_GRAYSCALE_CODE);
            PendingIntent disablePendingIntent = PendingIntent.getBroadcast(mContext, 0, disableIntent, 0);

            alarmManager.cancel(pendingIntent);
            alarmManager.cancel(disablePendingIntent);

            Calendar beginCalendar = Calendar.getInstance();
            beginCalendar.setTimeInMillis(System.currentTimeMillis());
            beginCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(read("starttime_hour", "0")));
            beginCalendar.set(Calendar.MINUTE, Integer.parseInt(read("starttime_minute", "0")));
            beginCalendar.set(Calendar.SECOND, 0);

            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(System.currentTimeMillis());
            endCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(read("stoptime_hour", "0")));
            endCalendar.set(Calendar.MINUTE, Integer.parseInt(read("stoptime_minute", "0")));
            endCalendar.set(Calendar.SECOND, 0);

            if (System.currentTimeMillis() > beginCalendar.getTimeInMillis()) {
                beginCalendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, beginCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, endCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, disablePendingIntent);
        }

        public void save(String valueKey, String value) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(valueKey, value);
            edit.apply();
        }

        public String read(String valueKey, String valueDefault) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            return prefs.getString(valueKey, valueDefault);
        }
    }
}
