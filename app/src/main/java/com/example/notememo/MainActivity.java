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

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            // Toolbar might not be in layout, that's okay
            return;
        }
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // Remove search menu item since search is always visible
        menu.removeItem(R.id.action_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_date_desc) {
            viewModel.setSortMode(MemoRepository.SORT_BY_DATE_DESC);
            return true;
        } else if (id == R.id.action_sort_date_asc) {
            viewModel.setSortMode(MemoRepository.SORT_BY_DATE_ASC);
            return true;
        } else if (id == R.id.action_sort_category) {
            viewModel.setSortMode(MemoRepository.SORT_BY_CATEGORY);
            return true;
        } else if (id == R.id.action_pin_settings) {
            showPinSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPinSettingsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("PIN Settings");

        boolean pinEnabled = SecurityHelper.isPinEnabled(this);
        String[] options = pinEnabled ?
                new String[]{"Disable PIN", "Change PIN"} :
                new String[]{"Enable PIN"};

        builder.setItems(options, (dialog, which) -> {
            if (pinEnabled) {
                if (which == 0) {
                    SecurityHelper.setPinEnabled(this, false);
                    Toast.makeText(this, "PIN disabled", Toast.LENGTH_SHORT).show();
                } else {
                    showSetPinDialog();
                }
            } else {
                showSetPinDialog();
            }
        });
        builder.show();
    }

    private void showSetPinDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Set PIN");

        TextInputLayout pinLayout = new TextInputLayout(this);
        TextInputEditText pinInput = new TextInputEditText(this);
        pinInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pinInput.setHint("Enter 4-6 digit PIN");
        pinLayout.addView(pinInput);
        pinLayout.setPadding(50, 0, 50, 0);

        builder.setView(pinLayout);
        builder.setPositiveButton("Set", (dialog, which) -> {
            String pin = pinInput.getText().toString();
            if (pin.length() >= 4 && pin.length() <= 6) {
                SecurityHelper.setPin(this, pin);
                Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "PIN must be 4-6 digits", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void setupRecyclerView() {
        adapter = new MemoAdapter(memo -> {
            Intent intent = new Intent(MainActivity.this, MemoDetailsActivity.class);
            intent.putExtra("memo_uid", memo.uid);
            startActivity(intent);
        });
        adapter.setOnMemoLongClickListener(memo -> {
            showDeleteConfirmation(memo);
            return true;
        });
        rvMemos.setLayoutManager(new LinearLayoutManager(this));
        rvMemos.setAdapter(adapter);
    }

    private void showDeleteConfirmation(Memo memo) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Delete Memo")
                .setMessage("Are you sure you want to delete \"" + (memo.title != null && !memo.title.isEmpty() ? memo.title : "this memo") + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.delete(memo);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                viewModel.setSearchQuery(query);

                // Show/hide clear button based on search text
                android.widget.ImageButton btnClearSearch = findViewById(R.id.btnClearSearch);
                if (btnClearSearch != null) {
                    btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


}