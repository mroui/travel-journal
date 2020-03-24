package com.martynaroj.traveljournal.view.others.classes;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;

import com.google.android.material.button.MaterialButton;

public abstract class RippleDrawable {

    public static void setRippleEffectButton(MaterialButton button, int normalColor, int pressedColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            button.setRippleColor(new ColorStateList(new int[][]{ new int[]{}},new int[] {pressedColor}));
        else
            button.setBackgroundDrawable(getStateListDrawable(normalColor, pressedColor));
    }

    private static StateListDrawable getStateListDrawable(int normalColor, int pressedColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},
                new ColorDrawable(pressedColor));
        states.addState(new int[]{android.R.attr.state_focused},
                new ColorDrawable(pressedColor));
        states.addState(new int[]{android.R.attr.state_activated},
                new ColorDrawable(pressedColor));
        states.addState(new int[]{},
                new ColorDrawable(normalColor));
        return states;
    }

}
