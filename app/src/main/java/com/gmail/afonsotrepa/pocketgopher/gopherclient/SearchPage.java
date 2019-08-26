package com.gmail.afonsotrepa.pocketgopher.gopherclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.gmail.afonsotrepa.pocketgopher.Extensions;
import com.gmail.afonsotrepa.pocketgopher.History;
import com.gmail.afonsotrepa.pocketgopher.MainActivity;
import com.gmail.afonsotrepa.pocketgopher.R;


/**
 * Index-Search server ('7')
 */

public class SearchPage extends Page
{
    private static final Integer IMAGE_TAG = R.drawable.ic_search_white;

    public SearchPage(String selector, String server, Integer port, String line)
    {
        super(server, port, '7', selector, line);
    }

    public SpannableString render(final Context context, String line)
    {
        final SpannableString text = new SpannableString("  " + line + " \n");

        final Page page = this;

        //create the span (and the function to be run when it's clicked)
        final ClickableSpan cs1 = new ClickableSpan()
        {
            @Override
            public void onClick(View widget)
            {
                page.open(context);
            }
        };
        final ClickableSpan cs2 = new ClickableSpan()
        {
            @Override
            public void onClick(View widget)
            {
                page.open(context);
            }
        };

        //make it clickable
        text.setSpan(cs1, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(cs2, 2, text.length() - 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //set the image tag behind (left of) the text
        text.setSpan(new ImageSpan(context, IMAGE_TAG), 0, 1, 0);

        return text;
    }


    public void open(final Context context)
    {
        History.add(context, this.url);

        //AlertDialog to be shown when method gets called
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Input search query");

        LinearLayout layout = Extensions.generateDialogBoxLayout(context);

        //the EditText where the user will input the name of the file
        final EditText input = new EditText(context);
        input.setTextAppearance(context, MainActivity.font);

        layout.addView(input);

        dialog.setView(layout);

        dialog.setPositiveButton("Send",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //get the query
                        String query = input.getText().toString();


                        //make the line
                        MenuPage page = new MenuPage(
                                selector + "\t" + query,
                                server,
                                port
                        );

                        page.open(context);

                    }
                }
        );

        dialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                }
        );

        dialog.show();
    }
}
