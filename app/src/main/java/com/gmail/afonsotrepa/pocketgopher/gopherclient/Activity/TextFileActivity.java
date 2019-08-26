package com.gmail.afonsotrepa.pocketgopher.gopherclient.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.afonsotrepa.pocketgopher.Bookmark;
import com.gmail.afonsotrepa.pocketgopher.Extensions;
import com.gmail.afonsotrepa.pocketgopher.HistoryActivity;
import com.gmail.afonsotrepa.pocketgopher.MainActivity;
import com.gmail.afonsotrepa.pocketgopher.R;
import com.gmail.afonsotrepa.pocketgopher.gopherclient.Connection;
import com.gmail.afonsotrepa.pocketgopher.gopherclient.Page;

import java.io.IOException;


/**
 *
 */

public class TextFileActivity extends AppCompatActivity
{
    Page page;
    String selector;
    String server;
    Integer port;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); //same layout as MenuActivity

        //widget to write to
        final TextView textView = findViewById(R.id.textView);
        //set the font
        textView.setTextAppearance(this, MainActivity.font);

        textView.setLineSpacing(MainActivity.lineSpacing, 1);
        textView.setTextSize(MainActivity.fontSize);

        //Make Gopher links selectable.
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        //start a new thread to do network stuff
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final Handler handler = new Handler(Looper.getMainLooper());

                //intent stuff
                Intent intent = getIntent();

                page = (Page) intent.getSerializableExtra("page");
                selector = page.selector;
                server = page.server;
                port = page.port;
                url = page.url;

                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setTitle(url);
                    }
                });

                ///Network stuff
                final String lines;
                try
                {
                    //start new connection
                    Connection conn = new Connection(server, port);

                    //get the desired text file
                    lines = conn.getText(selector);

                    //make the progress bar invisible
                    final ProgressBar progressBar = findViewById(R.id.progressBar);
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }
                catch (final IOException e)
                {
                    e.printStackTrace();
                    //inform the user of the error
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(),
                                    Toast.LENGTH_LONG
                            );
                            toast.show();
                        }
                    });
                    //kill current activity (go back to the previous one on the stack)
                    finish();
                    return;
                }

                //render the lines on the screen
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        textView.setText(lines);
                    }
                });

            }
        }).start();

    }

    //setup the menu/title bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.client_downloadable, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.addBookmarkButton:
                try
                {
                    Bookmark.makeNewBookmark(this, url);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }

                return true;

            case R.id.downloadButton:
                page.download(this);

                return true;

            case R.id.refresh:
                this.recreate();

                return true;

            case R.id.link:
                //create the dialog to be shown when the button gets clicked
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Enter Gopher URL");

                LinearLayout layout = Extensions.generateDialogBoxLayout(this);

                //setup the EditText where the user will input url to the page
                final EditText input = new EditText(this);

                input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                input.setHint("URL");

                input.setText(url);

                input.setTextAppearance(this, MainActivity.font);
                //add EditText to layout
                layout.addView(input);

                alertDialog.setView(layout);

                alertDialog.setPositiveButton("Go",
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(final DialogInterface dialog, int which)
                            {
                                //setup the page
                                if (input.getText().toString().trim().length() > 0)
                                {
                                    //setup the page
                                    Page page = Page.makePage(input.getText().toString());
                                    page.open(TextFileActivity.this);
                                }
                                else
                                {
                                    Extensions.showToast(getApplicationContext(), "URL field must not be empty");
                                }
                            }
                        }
                );

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        }
                );

                alertDialog.show();

                return true;

            case R.id.history:
                Intent intent = new Intent(TextFileActivity.this, HistoryActivity.class);
                startActivity(intent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
