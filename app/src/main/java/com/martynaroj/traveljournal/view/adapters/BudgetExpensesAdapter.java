package com.martynaroj.traveljournal.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.Expense;
import com.martynaroj.traveljournal.view.interfaces.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

public class BudgetExpensesAdapter extends ArrayAdapter<Expense> {

    private Context context;
    private final List<Expense> list;
    private OnItemLongClickListener listener;


    public BudgetExpensesAdapter(List<Expense> list, Context context) {
        super(context, R.layout.item_budget_expense, list);
        this.context = context;
        this.list = list;
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
            view.findViewById(R.id.expense_item).setOnLongClickListener(v -> {
                listener.onItemLongClick(expense, position, v);
                return true;
            });
        }
        return view;
    }


    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.listener = onItemLongClickListener;
    }


    public List<Expense> getList() {
        return list;
    }


    public List<Expense> getTodayList() {
        List<Expense> todayList = new ArrayList<>();
        for (Expense e : list)
            if (DateUtils.isToday(e.getDate()))
                todayList.add(e);
        return todayList;
    }

}