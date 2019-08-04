package com.gmail.afonsotrepa.pocketgopher;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Extensions {

    public static LinearLayout generateDialogBoxLayout (Context context){
        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        layout.setLayoutParams(layoutParams);

        layout.setLayoutParams(layoutParams);
        layout.setOrientation(LinearLayout.VERTICAL);

        //set layout padding.
        layout.setPadding(20,10,20,10);

        return layout;
    }

    public static void showToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT)
                .show();
    }
}
