package com.gmail.afonsotrepa.pocketgopher.gopherclient;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.Toast;

import com.gmail.afonsotrepa.pocketgopher.History;
import com.gmail.afonsotrepa.pocketgopher.R;

/**
 *
 */

public class UnknownPage extends Page
{
    private static final Integer IMAGE_TAG = R.drawable.ic_error_white;

    public String line;
    private Character type;

    public UnknownPage(String line)
    {
        super(null, 0, '3', "", line);
    }

    public SpannableString render(final Context context, String line)
    {
        final SpannableString text = new SpannableString("  " + line + "\n");

        //set the image tag behind (left of) the text
        text.setSpan(new ImageSpan(context, IMAGE_TAG), 0, 1, 0);
        return text;
    }

    public void open(Context context)
    {
        History.add(context, this.url);

        Toast.makeText(context, "Can't open a page of type '" + this.type + "' !!",
                Toast.LENGTH_LONG
        ).show();
    }
}
