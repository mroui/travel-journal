package com.martynaroj.traveljournal.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.Expense;

import java.util.List;

public class BudgetExpensesAdapter extends ArrayAdapter<Expense> {

    private Context context;

    public BudgetExpensesAdapter(List<Expense> listData, Context context) {
        super(context, R.layout.item_budget_expense, listData);
        this.context = context;
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        final Expense expense = getItem(position);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.item_budget_expense, null);
        }
        if (expense != null) {
            ((TextView) view.findViewById(R.id.expense_category)).setText(expense.getCategory());
            ((TextView) view.findViewById(R.id.expense_date)).setText(expense.getDateTimeString());
            ((TextView) view.findViewById(R.id.expense_amount)).setText(expense.getAmountString());
            if (expense.getAmount() > 0)
                ((TextView) view.findViewById(R.id.expense_amount)).setTextColor(
                        context.getResources().getColor(R.color.green)
                );
            else
                ((TextView) view.findViewById(R.id.expense_amount)).setTextColor(
                        context.getResources().getColor(R.color.red)
                );
        }
        return view;
    }

}