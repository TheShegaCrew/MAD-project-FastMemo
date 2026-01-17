package com.example.notememo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvMemos;
    private MemoAdapter adapter;
    private MemoViewModel viewModel;
    private TextInputEditText etSearch;
    private LinearLayout emptyState;
    private View emptySearchState;
    private Chip chipAll, chipClasses, chipLectureNotes, chipAssignments, chipExams, chipToDo, chipReminders, chipPersonal;
    private Toolbar toolbar;
    private View searchContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check PIN lock
        if (SecurityHelper.isPinEnabled(this)) {
            showPinDialog();
            return; // Will set content view after PIN verification
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(MemoViewModel.class);
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupCategoryFilters();
        setupFAB();
        observeViewModel();
    }




    private void showPinDialog() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Enter PIN");
        builder.setMessage("Please enter your PIN to access Fast Memo");

        TextInputLayout pinLayout = new TextInputLayout(this);
        TextInputEditText pinInput = new TextInputEditText(this);
        pinInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pinLayout.addView(pinInput);
        pinLayout.setPadding(50, 0, 50, 0);

        builder.setView(pinLayout);
        AlertDialog pinDialog = builder.create();
        pinDialog.setOnShowListener(d -> {
            if (pinDialog.getWindow() != null) {
                pinDialog.getWindow().setDimAmount(0.75f);
                pinDialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            }
        });
        pinDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            String pin = pinInput.getText().toString();
            if (SecurityHelper.verifyPin(this, pin)) {
                // PIN verified, continue with normal initialization
                viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(MemoViewModel.class);
                initViews();
                setupToolbar();
                setupRecyclerView();
                setupSearch();
                setupCategoryFilters();
                setupFAB();
                observeViewModel();
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        pinDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> finish());
        pinDialog.setCancelable(false);
        pinDialog.show();
    }

    private void initViews() {
        rvMemos = findViewById(R.id.rvMemos);
        etSearch = findViewById(R.id.etSearch);
        emptyState = findViewById(R.id.emptyState);
        emptySearchState = findViewById(R.id.emptySearchState);
        chipAll = findViewById(R.id.chipAll);
        chipClasses = findViewById(R.id.chipClasses);
        chipLectureNotes = findViewById(R.id.chipLectureNotes);
        chipAssignments = findViewById(R.id.chipAssignments);
        chipExams = findViewById(R.id.chipExams);
        chipToDo = findViewById(R.id.chipToDo);
        chipReminders = findViewById(R.id.chipReminders);
        chipPersonal = findViewById(R.id.chipPersonal);
        searchContainer = findViewById(R.id.searchContainer);
        android.widget.ImageButton btnClearSearch = findViewById(R.id.btnClearSearch);

        // Setup clear search button
        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                etSearch.setText("");
                viewModel.setSearchQuery("");
            });
        }

        if (emptySearchState != null) {
            TextView tvMessage = emptySearchState.findViewById(R.id.tvEmptySearchMessage);
            View btnClearSearchFromEmpty = emptySearchState.findViewById(R.id.btnClearSearch);
            if (btnClearSearchFromEmpty != null) {
                btnClearSearchFromEmpty.setOnClickListener(v -> {
                    etSearch.setText("");
                    viewModel.setSearchQuery("");
                });
            }
        }
    }

}