package com.gmail.afonsotrepa.pocketgopher;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gmail.afonsotrepa.pocketgopher.gopherclient.Page;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class Bookmark
{
    public Page page;
    public String name;
    public String url;

    public Integer id; //a unique id that identifies the bookmark

    private static final String BOOKMARKS_FILE = "bookmarks";
    private static final String EXPORTED_BOOKMARKS_FILE = "PocketGopher_Bookmarks";

    private Bookmark(String name, String url, Integer id)
    {
        this.page = Page.makePage(url);
        this.url = url;
        this.name = name;
        this.id = id;
    }

    public Bookmark(Context context, String name, String url)
    {
        this.page = Page.makePage(url);
        this.url = url;
        this.name = name;

        //generate a new unique id
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (context);
        id = sharedPreferences.getInt("id", 0);

        //update the file
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id", id + 1);
        editor.apply();
    }

    /**
     * A simple wrapper for backwards comparability. Avoid!!
     *
     * @deprecated
     */
    public Bookmark(Context context, String name, Character type, String selector, String server,
                    Integer port)
    {
        this(context, name, server + ":" + port + "/" + type.toString() + selector);
    }

    /**
     * Add a bookmark to the file
     */
    void add(Context context)
    {
        try
        {
            FileOutputStream outputStream = context.openFileOutput(BOOKMARKS_FILE,
                    Context.MODE_APPEND);

            outputStream.write((
                    this.name + "\t" +
                            this.url + "\t" +
                            this.id.toString() + "\n"
            ).getBytes());
            outputStream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    void edit(Context context, Bookmark[] bookmarks)
    {
        try
        {
            FileOutputStream outputStream = context.openFileOutput(BOOKMARKS_FILE,
                    Context.MODE_PRIVATE);

            for (Bookmark bookmark : bookmarks)
            {
                outputStream.write((
                        bookmark.name + "\t" +
                                bookmark.url + "\t" +
                                bookmark.id.toString() + "\n"
                ).getBytes());
            }

            outputStream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the bookmarks from the bookmarks file
     *
     * @return a list of all the bookmarks in the bookmarks file
     */
    static ArrayList<Bookmark> read(Context context)
    {
        try
        {
            FileInputStream inputStream = context.openFileInput(BOOKMARKS_FILE);
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputStream));

            //read the bookmark(s) from the file
            ArrayList<Bookmark> bookmarks = new ArrayList<>();
            String b;
            while ((b = bufferedreader.readLine()) != null)
            {
                String[] bsplit = b.split("\t");
                if (bsplit.length > 1)
                {
                    //parse the bookmark
                    Bookmark bookmark = new Bookmark(
                            bsplit[0], //name
                            bsplit[1], //url
                            Integer.parseInt(bsplit[2]) //id
                    );

                    //add it to the list of bookmarks
                    bookmarks.add(bookmark);
                }
            }

            return bookmarks;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private void remove(Context context)
    {
        try
        {
            List<Bookmark> bookmarks = Bookmark.read(context);

            //clear the file
            context.openFileOutput(BOOKMARKS_FILE, Context.MODE_PRIVATE).write("".getBytes());

            for (Bookmark bookmark : bookmarks)
            {
                if (!bookmark.id.equals(this.id))
                {
                    bookmark.add(context);
                }
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void open(Context context)
    {
        this.page.open(context);
    }

    //returns the add dialog screen
    private AlertDialog.Builder loadAddDialog(final Context context, String url)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Add a new Bookmark");

        //setup the layout
        LinearLayout layout = Extensions.generateDialogBoxLayout(context);

        //setup the EditText's and add them to the layout
        final EditText newName = new EditText(context);
        final EditText newUrl = new EditText(context);
        newName.setHint("Name");
        newUrl.setHint("Url");

        newName.setTextAppearance(context, MainActivity.font);
        newUrl.setTextAppearance(context, MainActivity.font);

        layout.addView(newName);
        layout.addView(newUrl);

        //apply the layout
        dialog.setView(layout);

        if (!url.trim().isEmpty())
        {
            newUrl.setText(url);
        }

        final Bookmark bookmark = this;

        dialog.setPositiveButton("Save",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(final DialogInterface dialog, int which)
                    {
                        if (newUrl.getText().toString().trim().length() <= 0)
                        {
                            Toast.makeText(context, "URL field must not be empty!", Toast.LENGTH_SHORT)
                                    .show();
                        }
                        else
                        {
                            bookmark.name = newName.getText().toString();
                            bookmark.url = newUrl.getText().toString();

                            bookmark.add(context);

                            Toast.makeText(context, "Bookmark saved", Toast.LENGTH_SHORT)
                                    .show();
                            dialog.cancel();
                        }

                        //refresh MainActivity (when called by it)
                        if (context.getClass() == MainActivity.class)
                        {
                            ((Activity) context).recreate();
                        }
                    }
                }
        );

        dialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                }
        );

        return dialog;
    }
    //returns the edit dialog screen.
    private AlertDialog.Builder loadEditDialog(final Context context, final Bookmark[] bookmarks)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setTitle("Edit Bookmark");

        //setup the layout
        LinearLayout layout = Extensions.generateDialogBoxLayout(context);

        //setup the EditText's and add them to the layout
        final EditText editName = new EditText(context);
        final EditText editUrl = new EditText(context);

        editName.setHint("Name");
        editUrl.setHint("Url");

        editName.setText(this.name);
        editUrl.setText(this.url);

        editName.setTextAppearance(context, MainActivity.font);
        editUrl.setTextAppearance(context, MainActivity.font);

        layout.addView(editName);
        layout.addView(editUrl);

        //apply the layout
        dialog.setView(layout);

        final Bookmark bookmark = this;

        dialog.setPositiveButton("Save",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(final DialogInterface dialog, int which)
                    {
                        if (editUrl.getText().toString().trim().length() <= 0)
                        {
                            Toast.makeText(context, "URL field must not be empty!", Toast.LENGTH_SHORT)
                                    .show();
                        }
                        else
                        {
                            bookmark.name = editName.getText().toString();
                            bookmark.url = editUrl.getText().toString();

                            bookmark.edit(context, bookmarks);
                            Toast.makeText(context, "Bookmark saved", Toast.LENGTH_SHORT)
                                    .show();

                            dialog.dismiss();
                        }

                        //refresh MainActivity (when called by it)
                        if (context.getClass() == MainActivity.class)
                        {
                            ((Activity) context).recreate();
                        }
                    }
                }
        );

        dialog.setNegativeButton("Remove",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            bookmark.remove(context);

                            dialog.cancel();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();

                            dialog.cancel();
                        }

                        //refresh MainActivity (when called by it)
                        if (context.getClass() == MainActivity.class)
                        {
                            ((Activity) context).recreate();
                        }
                    }
                }
        );

        return dialog;
    }

    public static Bookmark makeNewBookmark(Context context, String url)
    {
        AlertDialog.Builder dialog;
        //Make a new bookmark instance.
        Bookmark bookmark = new Bookmark(context, "", "");
        //Load the dialog box and show it.
        dialog = bookmark.loadAddDialog(context, url);
        dialog.show();

        return bookmark;
    }

    public static Bookmark editBookmark(final Context context, Bookmark bookmark, Bookmark[] bookmarks)
    {
        AlertDialog.Builder dialog;
        dialog = bookmark.loadEditDialog(context, bookmarks);
        dialog.show();

        return bookmark;
    }

    public static void exportBookmarks(final Context context)
    {
        ArrayList<Bookmark> bookmarkArrayList = read(context);

        try
        {
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED)
            {
                File backupFile = new File(Environment.getExternalStorageDirectory(), EXPORTED_BOOKMARKS_FILE);

                FileOutputStream outputStream = new FileOutputStream(backupFile);

                for (Bookmark bookmark : bookmarkArrayList)
                {
                    outputStream.write((
                            bookmark.name + "\t" +
                                    bookmark.url + "\t" +
                                    bookmark.id.toString() + "\n"
                    ).getBytes());
                }

                outputStream.close();

                Extensions.showToast(context, "Backup saved as " + Environment.getExternalStorageDirectory() + "/" + EXPORTED_BOOKMARKS_FILE);
            }
            else
            {
                Extensions.showToast(context, context.getString(R.string.export_bookmarks_no_permission_message)
                + Environment.getExternalStorageDirectory());
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void importBookmarks(final Context context) throws FileNotFoundException {
        FileInputStream backupFile = new FileInputStream(new File(Environment.getExternalStorageDirectory(), EXPORTED_BOOKMARKS_FILE));

        ArrayList<Bookmark> bookmarks = readFileFromExternalStorage(backupFile);

        Bookmark[] bookmarkArray = new Bookmark[bookmarks.size()];
        bookmarkArray = bookmarks.toArray(bookmarkArray);

        //Used to access the edit function.
        if (bookmarks.size() > 0){
            Bookmark bookmark = bookmarks.get(0);
            bookmark.edit(context, bookmarkArray);

            Extensions.showToast(context, "Bookmarks restored from "+Environment.getExternalStorageDirectory() + "/" + EXPORTED_BOOKMARKS_FILE);
        }
        else
        {
            Extensions.showToast(context,context.getString(R.string.invalid_bookmarks_message));
        }

    }

    private static ArrayList<Bookmark> readFileFromExternalStorage(FileInputStream inputStream)
    {
        try
        {

            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputStream));

            //read the bookmark(s) from the file
            ArrayList<Bookmark> bookmarks = new ArrayList<>();
            String b;
            while ((b = bufferedreader.readLine()) != null)
            {
                String[] bsplit = b.split("\t");
                if (bsplit.length > 1)
                {
                    //parse the bookmark
                    Bookmark bookmark = new Bookmark(
                            bsplit[0], //name
                            bsplit[1], //url
                            Integer.parseInt(bsplit[2]) //id
                    );

                    //add it to the list of bookmarks
                    bookmarks.add(bookmark);
                }
            }

            return bookmarks;
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
