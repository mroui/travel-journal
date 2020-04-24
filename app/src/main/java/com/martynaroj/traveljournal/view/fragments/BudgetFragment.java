package com.martynaroj.traveljournal.view.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogAddExpenseBinding;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.FragmentBudgetBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Expense;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.adapters.BudgetExpensesAdapter;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.classes.InputTextWatcher;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BudgetFragment extends NotesFragment {

    private FragmentBudgetBinding binding;

    private Travel travel;
    private List<Expense> expenses;
    private BudgetExpensesAdapter adapter;

    private Dialog addDialog;
    private DialogAddExpenseBinding dialogBinding;


    public static BudgetFragment newInstance(Travel travel, Day day, List<Day> days) {
        BudgetFragment fragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_TRAVEL, travel);
        args.putSerializable(Constants.BUNDLE_DAY, day);
        args.putSerializable(Constants.BUNDLE_DAYS, (Serializable) days);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            travel = (Travel) getArguments().getSerializable(Constants.BUNDLE_TRAVEL);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges(view);

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initContentData() {
        expenses = getAllDaysExpensesList();
        initListAdapter();
        setBindingData();
    }


    private void initListAdapter() {
        if (getContext() != null) {
            adapter = new BudgetExpensesAdapter(expenses, getContext());
            binding.budgetExpensesList.setAdapter(adapter);
        }
    }


    private void setBindingData() {
        binding.setBudget(new DecimalFormat("0.00").format(getRemainingBudget()));
        binding.setIsListEmpty(expenses.size() == 0);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.budgetArrowButton.setOnClickListener(this);
        binding.budgetAddFloatingButton.setOnClickListener(this);
        setOnListScrollListener();
        setOnItemLongClickListener();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.budget_arrow_button:
                back();
                break;
            case R.id.budget_add_floating_button:
                showAddExpenseDialog();
                break;
        }
    }


    private void setOnListScrollListener() {
        binding.budgetExpensesList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE)
                    binding.budgetAddFloatingButton.show();
                else
                    binding.budgetAddFloatingButton.hide();
            }

            @Override
            public void onScroll(AbsListView view, int i, int i1, int i2) {
            }
        });
    }


    private void setOnItemLongClickListener() {
        adapter.setOnItemLongClickListener((object, position, view) -> showRemoveDialog((Expense) object));
    }


    //CALCULATIONS----------------------------------------------------------------------------------


    private List<Expense> getAllDaysExpensesList() {
        List<Expense> list = new ArrayList<>();
        if (days != null) {
            for (Day day : days)
                list.addAll(day.getExpenses());
            Collections.sort(list);
            Collections.reverse(list);
        }
        return list;
    }


    private Double getRemainingBudget() {
        Double amount = travel.getBudget();
        for (Expense expense : expenses)
            amount += expense.getAmount();
        return amount;
    }


    //ADDING / DIALOG-------------------------------------------------------------------------------


    private void showAddExpenseDialog() {
        if (getContext() != null && getActivity() != null) {
            addDialog = DialogHandler.createDialog(getContext(), true);
            dialogBinding = DialogAddExpenseBinding.inflate(LayoutInflater.from(getContext()));
            addDialog.setContentView(dialogBinding.getRoot());
            setAddDialogContentData();
            addDialog.show();
        }
    }


    private void setAddDialogContentData() {
        if (getContext() != null) {
            dialogBinding.dialogAddExpenseCategoryInput.setThreshold(1);
            dialogBinding.dialogAddExpenseCategoryInput.setAdapter(new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    getResources().getStringArray(R.array.expense_categories))
            );
            dialogBinding.dialogAddExpenseAmountInput.addTextChangedListener(new InputTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (dialogBinding.dialogAddExpenseAmountInput.hasFocus() && s != null)
                        new FormHandler(getContext()).handleCurrency(s, dialogBinding.dialogAddExpenseAmountInput);
                }
            });
            dialogBinding.dialogAddExpenseButtonPositive.setOnClickListener(v -> {
                if (validateInput(dialogBinding.dialogAddExpenseCategoryInput, dialogBinding.dialogAddExpenseCategoryLayout)
                        && validateInput(dialogBinding.dialogAddExpenseAmountInput, dialogBinding.dialogAddExpenseAmountLayout)) {
                    addExpense();
                    addDialog.dismiss();
                }
            });
            dialogBinding.dialogAddExpenseButtonNegative.setOnClickListener(v -> addDialog.dismiss());
        }
    }


    private void addExpense() {
        adapter.insert(createNewExpense(), 0);
        expenses = adapter.getList();
        dayViewModel.updateDay(today.getId(), new HashMap<String, Object>() {{
            put(Constants.DB_EXPENSES, adapter.getTodayList());
        }});
        today.setExpenses(adapter.getTodayList());
        dayViewModel.setToday(today);
        setBindingData();
    }


    private Expense createNewExpense() {
        String category = dialogBinding.dialogAddExpenseCategoryInput.getText().toString();
        boolean isPositive = dialogBinding.dialogAddExpenseAmountChar.isChecked();
        double amount = Double.parseDouble(Objects.requireNonNull(
                dialogBinding.dialogAddExpenseAmountInput.getText()).toString());
        amount *= isPositive ? 1 : -1;
        return new Expense(category, amount);
    }


    @SuppressLint("SetTextI18n")
    private void showRemoveDialog(Expense expense) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(getContext(), binding.dialogCustomTitle, R.string.dialog_expense_remove_title,
                    binding.dialogCustomDesc, R.string.dialog_expense_remove_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_pink, R.color.pink_bg_light);
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                removeExpense(expense);
                dialog.dismiss();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    private void removeExpense(Expense expense) {
        adapter.remove(expense);
        expenses = adapter.getList();
        Integer index = getDayIndexOfNote(expense);
        if (index != null) {
            days.get(index).getExpenses().remove(expense);
            dayViewModel.updateDay(days.get(index).getId(), new HashMap<String, Object>() {{
                put(Constants.DB_EXPENSES, days.get(index).getExpenses());
            }});
            dayViewModel.setDays(days);
        }
        setBindingData();
    }


    //OTHERS----------------------------------------------------------------------------------------


    private boolean validateInput(MaterialAutoCompleteTextView input, TextInputLayout layout) {
        return new FormHandler(getContext()).validateInput(input, layout);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
