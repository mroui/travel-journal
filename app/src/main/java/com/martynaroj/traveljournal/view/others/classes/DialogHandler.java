package com.martynaroj.traveljournal.view.others.classes;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public abstract class DialogHandler {

    public static Dialog createDialog(Context context, boolean cancelable) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(cancelable);
        if (dialog.getWindow() != null)
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    public static void initContent(Context context, TextView viewTitle, int titleId,
                                   TextView viewDesc, int descId, MaterialButton viewButtonPositive,
                                   int positiveTextId, MaterialButton viewButtonNegative,
                                   int negativeTextId, int colorId, int rippleColorId) {
        viewTitle.setText(context.getString(titleId));
        viewDesc.setText(context.getString(descId));
        viewButtonPositive.setText(context.getResources().getString(positiveTextId));
        RippleDrawable.setRippleEffectButton(
                viewButtonPositive,
                Color.TRANSPARENT,
                context.getResources().getColor(rippleColorId)
        );
        viewButtonPositive.setTextColor(context.getResources().getColor(colorId));
        viewButtonNegative.setText(context.getResources().getString(negativeTextId));
        RippleDrawable.setRippleEffectButton(
                viewButtonNegative,
                Color.TRANSPARENT,
                context.getResources().getColor(rippleColorId)
        );
        viewButtonNegative.setTextColor(context.getResources().getColor(colorId));
    }

}