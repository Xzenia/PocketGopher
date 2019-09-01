package com.gmail.afonsotrepa.pocketgopher.gopherclient;

import android.content.Context;
import android.text.SpannableString;
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

    public SpannableString render(Context context, String line)
    {
        final SpannableString text = new SpannableString(line + "\n");
        return text;
    }

    public void open(Context context)
    {
        History.add(context, this.url);

        Toast.makeText(context, "Can't open a page of type 'i'!!", Toast.LENGTH_LONG).show();
    }
}
