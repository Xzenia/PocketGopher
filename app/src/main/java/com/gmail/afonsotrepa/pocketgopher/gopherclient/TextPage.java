package com.gmail.afonsotrepa.pocketgopher.gopherclient;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.afonsotrepa.pocketgopher.History;


/**
 * Simple informational text line ('i')
 */

public class TextPage extends Page
{
    //selector is needed to check for titles
    TextPage(String selector, String line)
    {
        super(null, 0, 'i', selector, line);
    }

    public void render(final TextView textView, Context context, String line)
    {

        final SpannableString text = new SpannableString(line + "\n");

        final Handler handler = new Handler(Looper.getMainLooper());

        //display the line
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                textView.append(text);
            }
        });
    }

    public void open(Context context)
    {
        History.add(context, this.url);

        Toast.makeText(context, "Can't open a page of type 'i'!!", Toast.LENGTH_LONG).show();
    }
}
