package com.martynaroj.traveljournal.view.others.classes;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class FormHandler {

    private Context context;

    public FormHandler(Context context) {
        this.context = context;
    }


    public void addWatcher(final TextInputEditText input, final TextInputLayout layout) {
        input.addTextChangedListener(new InputTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (input.hasFocus())
                    validateInput(input, layout);
            }
        });
    }


    public boolean validateInput(TextInputEditText input, TextInputLayout layout) {
        String value = input.getText() == null ? "" : input.getText().toString();
        if (value.isEmpty() || value.trim().isEmpty()) {
            layout.setError(context.getResources().getString(R.string.messages_field_no_empty));
            input.requestFocus();
            return false;
        } else if (input.getInputType() ==
                (InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS + InputType.TYPE_CLASS_TEXT)
                && !isValidEmail(value)) {
            layout.setError(context.getResources().getString(R.string.messages_invalid_email));
            input.requestFocus();
            return false;
        } else if (input.getInputType() ==
                (InputType.TYPE_NUMBER_FLAG_DECIMAL + InputType.TYPE_CLASS_NUMBER)
                && Double.valueOf(input.getText().toString()).equals(0D)) {
            layout.setError(context.getResources().getString(R.string.messages_value_zero));
            input.requestFocus();
            return false;
        }else
            layout.setError(null);
        return true;
    }


    public boolean validateInput(AutoCompleteTextView input, TextInputLayout layout) {
        String value = input.getText() == null ? "" : input.getText().toString();
        if (value.isEmpty() || value.trim().isEmpty()) {
            layout.setError(context.getResources().getString(R.string.messages_field_no_empty));
            input.requestFocus();
            return false;
        } else
            layout.setError(null);
        return true;
    }


    public boolean validateInputsEquality(TextInputEditText input1, TextInputEditText input2,
                                          TextInputLayout layout2) {
        if (!isEqual(input1, input2)) {
            if (input1.getInputType() ==
                    InputType.TYPE_TEXT_VARIATION_PASSWORD + InputType.TYPE_CLASS_TEXT) {
                layout2.setError(context.getResources().getString(R.string.messages_password_not_match));
                input2.requestFocus();
            } else if (input1.getInputType() ==
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS + InputType.TYPE_CLASS_TEXT) {
                layout2.setError(context.getResources().getString(R.string.messages_email_not_match));
                input2.requestFocus();
            }
            return false;
        } else
            layout2.setError(null);
        return true;
    }


    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private boolean isEqual(TextInputEditText input1, TextInputEditText input2) {
        if (input1.getText() != null && input2.getText() != null)
            return input1.getText().toString().equals(input2.getText().toString());
        else return false;
    }


    public boolean validateLength(TextInputEditText input, TextInputLayout layout, int minLength) {
        String value = input.getText() == null ? "" : input.getText().toString();
        if (value.length() < minLength || value.trim().isEmpty() || value.trim().length() < minLength) {
            layout.setError("Enter at least " + minLength + " characters");
            input.requestFocus();
            return false;
        } else
            layout.setError(null);
        return true;
    }


    public void handleCurrency(Editable s, TextInputEditText editText) {
        String text = s.toString();
        if (text.contains(".")) {
            if ((text.substring(text.indexOf(".")).length() > 3
                    && text.length() <= Constants.MAX_CURRENCY_LENGTH)
                    || text.substring(text.length() - 1).equals(".")
                    && text.length() >= Constants.MAX_CURRENCY_LENGTH) {
                text = text.substring(0, text.length() - 1);
                editText.setText(text);
                editText.setSelection(text.length());
            }
        }
    }


    public void clearInput(TextInputEditText input, TextInputLayout layout) {
        clearText(input);
        offWatcher(layout);
        clearFocus(input);
    }


    private void clearText(TextInputEditText input) {
        input.setText("");
    }


    private void offWatcher(TextInputLayout layout) {
        layout.setErrorEnabled(false);
    }


    private void clearFocus(TextInputEditText input) {
        input.clearFocus();
    }

}
