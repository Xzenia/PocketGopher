package com.gmail.afonsotrepa.pocketgopher.gopherclient.Activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gmail.afonsotrepa.pocketgopher.Bookmark;
import com.gmail.afonsotrepa.pocketgopher.Extensions;
import com.gmail.afonsotrepa.pocketgopher.HistoryActivity;
import com.gmail.afonsotrepa.pocketgopher.MainActivity;
import com.gmail.afonsotrepa.pocketgopher.R;
import com.gmail.afonsotrepa.pocketgopher.gopherclient.Connection;
import com.gmail.afonsotrepa.pocketgopher.gopherclient.Page;

import java.io.IOException;

public class MenuActivity extends AppCompatActivity
{
    Page page;
    String selector;
    String server;
    Integer port;
    String url;

    ProgressBar progressBar;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        textView = findViewById(R.id.textView);

        textView.setTextAppearance(this, MainActivity.font);

        textView.setLineSpacing(MainActivity.lineSpacing, 1);
        textView.setTextSize(MainActivity.fontSize);

        //Make Gopher links selectable.
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        progressBar = findViewById(R.id.progressBar);

        //get info
        Intent intent = getIntent();

        page = (Page) intent.getSerializableExtra("page");
        selector = page.selector;
        server = page.server;
        port = page.port;
        url = page.url;

        new GetGopherMenu().execute("", "", "");
    }

    private class GetGopherMenu extends AsyncTask<String, Void, SpannableString> {
        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void...values) {
            super.onProgressUpdate(values);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected SpannableString doInBackground(String... strings) {
            //start new connection
            Connection conn;
            SpannableString lines = new SpannableString("");

            try {
                conn = new Connection(server, port);
                lines = conn.getMenu(selector, MenuActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
                MenuActivity.this.finish();
            }

            return lines;
        }

        @Override
        protected void onPostExecute(SpannableString result){
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);
            textView.append(result);
        }
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
