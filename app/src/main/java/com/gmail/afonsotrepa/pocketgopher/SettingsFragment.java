package com.gmail.afonsotrepa.pocketgopher;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

public class SettingsFragment extends PreferenceFragmentCompat {

    Preference backupBookmarkPreference;
    Preference restoreBookmarkPreference;
    Preference fontSizePreference;
    Preference lineSpacingPreference;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        backupBookmarkPreference = findPreference("backup_bookmarks");

        if (backupBookmarkPreference != null){
            backupBookmarkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Extensions.showPermissionsDialog(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    Bookmark.exportBookmarks(getContext());
                    return true;
                }
            });
        }

        restoreBookmarkPreference = findPreference("restore_bookmarks");

        if (restoreBookmarkPreference != null)
        {
            restoreBookmarkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Extensions.showPermissionsDialog(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

                    Context context = getContext();
                    //AlertDialog to be shown when method gets called
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(R.string.restore_bookmarks);

                    LinearLayout layout = Extensions.generateDialogBoxLayout(context);

                    final TextView textView = new TextView(context);
                    textView.setTextAppearance(context, MainActivity.font);
                    textView.setText(R.string.restore_bookmarks_warning_message);

                    layout.addView(textView);
                    alertDialog.setPositiveButton(R.string.save,
                            new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                try
                                {
                                    Bookmark.importBookmarks(getContext());
                                }
                                catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                            }
                    });

                    alertDialog.setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog.setView(layout);
                    alertDialog.show();
                    return true;
                }
            });
        }

        fontSizePreference = findPreference(MainActivity.FONT_SIZE);

        fontSizePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                final SharedPreferences.Editor editor = sharedPreferences.edit();

                dialog.setTitle("Set Font Size");

                //setup the layout
                LinearLayout layout = Extensions.generateDialogBoxLayout(getContext());

                //setup the NumberPicker and add it to the layout
                final NumberPicker fontSizeField = new NumberPicker(getContext());

                fontSizeField.setMinValue(1);
                fontSizeField.setMaxValue(50);
                fontSizeField.setValue(MainActivity.fontSize);

                layout.addView(fontSizeField);

                //apply the layout
                dialog.setView(layout);

                dialog.setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                editor.putInt(MainActivity.FONT_SIZE, fontSizeField.getValue());
                                editor.apply();

                                MainActivity.fontSize = fontSizeField.getValue();
                            }
                        }
                );

                dialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }
                );

                dialog.show();
                return true;
            }
        });

        lineSpacingPreference = findPreference(MainActivity.LINE_SPACING);

        lineSpacingPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                final SharedPreferences.Editor editor = sharedPreferences.edit();

                dialog.setTitle("Set Line Spacing");

                //setup the layout
                LinearLayout layout = Extensions.generateDialogBoxLayout(getContext());

                //setup the NumberPicker and add it to the layout
                final NumberPicker lineSpacingField = new NumberPicker(getContext());

                lineSpacingField.setMinValue(1);
                lineSpacingField.setMaxValue(50);
                lineSpacingField.setValue(MainActivity.lineSpacing);

                layout.addView(lineSpacingField);

                //apply the layout
                dialog.setView(layout);

                dialog.setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                editor.putInt(MainActivity.LINE_SPACING, lineSpacingField.getValue());
                                editor.apply();

                                MainActivity.lineSpacing = lineSpacingField.getValue();
                            }
                        }
                );

                dialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }
                );

                dialog.show();
                return true;
            }
        });
    }
}
