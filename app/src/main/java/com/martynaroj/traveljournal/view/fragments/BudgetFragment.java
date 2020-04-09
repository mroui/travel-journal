package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialAutoCompleteTextView;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogAddExpenseBinding;
import com.martynaroj.traveljournal.databinding.FragmentBudgetBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Expense;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.adapters.BudgetExpensesAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.classes.InputTextWatcher;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.DayViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BudgetFragment extends BaseFragment implements View.OnClickListener {

    private FragmentBudgetBinding binding;
    private UserViewModel userViewModel;
    private DayViewModel dayViewModel;

    private Travel travel;
    private Day today;
    private List<Day> days;
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

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            travel = (Travel) getArguments().getSerializable(Constants.BUNDLE_TRAVEL);
            today = (Day) getArguments().getSerializable(Constants.BUNDLE_DAY);
            days = (List<Day>) getArguments().getSerializable(Constants.BUNDLE_DAYS);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            dayViewModel = new ViewModelProvider(getActivity()).get(DayViewModel.class);
        }
    }


    private void initContentData() {
        if (getContext() != null) {
            expenses = getAllDaysExpensesList();
            initListAdapter();
            setBindingData();
        }
    }


    private void initListAdapter() {
        adapter = new BudgetExpensesAdapter(expenses, getContext());
        binding.budgetExpensesList.setAdapter(adapter);
    }


    private void setBindingData() {
        binding.setBudget(new DecimalFormat("#.00").format(getRemainingBudget()));
        binding.setIsListEmpty(expenses.size() == 0);
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
                back();
            }
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.budgetArrowButton.setOnClickListener(this);
        binding.budgetAddFloatingButton.setOnClickListener(this);
        setOnListScrollListener();
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


    //CALCULATIONS----------------------------------------------------------------------------------


    private List<Expense> getAllDaysExpensesList() {
        List<Expense> list = new ArrayList<>();
        for (Day day : days)
            list.addAll(day.getExpenses());
        Collections.sort(list);
        Collections.reverse(list);
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
            addDialog = new Dialog(getContext());
            addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            addDialog.setCancelable(true);
            addDialog.setContentView(R.layout.dialog_add_expense);
            if (addDialog.getWindow() != null)
                addDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
        dayViewModel.updateDay(today.getId(), new HashMap<String, Object>() {{
            put(Constants.DB_EXPENSES, adapter.getTodayList());
        }});
        expenses = adapter.getTodayList();
        today.setExpenses(expenses);
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


    //OTHERS----------------------------------------------------------------------------------------


    private boolean validateInput(TextInputEditText input, TextInputLayout layout) {
        return new FormHandler(getContext()).validateInput(input, layout);
    }


    private boolean validateInput(MaterialAutoCompleteTextView input, TextInputLayout layout) {
        return new FormHandler(getContext()).validateInput(input, layout);
    }


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
