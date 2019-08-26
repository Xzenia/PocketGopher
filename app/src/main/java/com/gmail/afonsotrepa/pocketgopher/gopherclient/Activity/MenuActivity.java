package com.gmail.afonsotrepa.pocketgopher.gopherclient.Activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.SpannableStringBuilder;
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
import java.util.List;

public class MenuActivity extends AppCompatActivity
{
    String selector;
    String server;
    Integer port;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final TextView textView = findViewById(R.id.textView);

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
                //handler to the main thread
                final Handler handler = new Handler(Looper.getMainLooper());

                //get info
                Intent intent = getIntent();

                final Page page = (Page) intent.getSerializableExtra("page");

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
                List<Page> lines;
                try
                {
                    //start new connection
                    Connection conn = new Connection(server, port);

                    //get the desired directory/menu
                    lines = conn.getMenu(selector);

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

                final SpannableStringBuilder pageContent = new SpannableStringBuilder("");

                //render the lines on the screen
                for (Page line : lines)
                {
                    pageContent.append(line.render(MenuActivity.this, line.line));
                }

                //make the progress bar invisible
                final ProgressBar progressBar = findViewById(R.id.progressBar);
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        progressBar.setVisibility(View.GONE);

                        textView.setText(pageContent);
                    }
                });
            }
        }).start();
    }


    //setup the menu/title bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.client_menu, menu);
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
                                    Page page = Page.makePage(input.getText().toString());
                                    page.open(MenuActivity.this);
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

            case R.id.refresh:
                MenuActivity.this.recreate();

                return true;

            case R.id.history:
                Intent intent = new Intent(MenuActivity.this, HistoryActivity.class);
                startActivity(intent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
