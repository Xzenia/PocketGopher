package com.gmail.afonsotrepa.pocketgopher;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;

public class SettingsFragment extends PreferenceFragmentCompat {

    Preference backupBookmarkPreference;
    Preference restoreBookmarkPreference;
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
    }
}
