package com.example.notememo;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import androidx.lifecycle.Observer;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

public class MemoEditorActivity extends AppCompatActivity {
    private TextInputEditText etTitle;
    private TextInputEditText etContent;
    private AutoCompleteTextView actvCategory;
    private Chip chipClasses, chipLectureNotes, chipAssignments, chipExams, chipToDo, chipReminders, chipPersonal;
    private MemoViewModel viewModel;
    private boolean isEditMode = false;
    private int currentMemoUid = -1;
    private String selectedCategory = "Classes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(MemoViewModel.class);
        
        // Check if editing existing memo
        currentMemoUid = getIntent().getIntExtra("memo_uid", -1);
        if (currentMemoUid != -1) {
                isEditMode = true;
                setContentView(R.layout.activity_memo_editor_edit);
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
        } else {
            setContentView(R.layout.activity_memo_editor);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        setupToolbar();
        initViews();
        if (isEditMode) {
            loadMemoData();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(isEditMode ? "Edit Memo" : "New Memo");
                getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_revert);
            }
        }
        
        if (isEditMode) {
            TextView tvLastEdited = findViewById(R.id.tvLastEdited);
            if (tvLastEdited != null) {
                tvLastEdited.setVisibility(View.VISIBLE);
                // Will be updated when memo loads
            }
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        actvCategory = findViewById(R.id.actvCategory);
        
        // Edit mode category chips
        chipClasses = findViewById(R.id.chipClasses);
        chipLectureNotes = findViewById(R.id.chipLectureNotes);
        chipAssignments = findViewById(R.id.chipAssignments);
        chipExams = findViewById(R.id.chipExams);
        chipToDo = findViewById(R.id.chipToDo);
        chipReminders = findViewById(R.id.chipReminders);
        chipPersonal = findViewById(R.id.chipPersonal);

        if (actvCategory != null && !isEditMode) {
            String[] categories = {"Classes", "Lecture Notes", "Assignments", "Exams & Tests", "To-Do", "Reminders", "Personal"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_dropdown_item_1line, categories);
            actvCategory.setAdapter(adapter);
            actvCategory.setText("Classes", false);
        }
        
        if (isEditMode) {
            setupCategoryChips();
        }
    }
    
    private void setupCategoryChips() {
        android.widget.CompoundButton.OnCheckedChangeListener chipListener = (buttonView, isChecked) -> {
            Chip[] chips = {chipClasses, chipLectureNotes, chipAssignments, chipExams, chipToDo, chipReminders, chipPersonal};
            if (isChecked) {
                for (Chip c : chips) {
                    if (c != null && c != buttonView) {
                        c.setChecked(false);
                        c.setChipBackgroundColorResource(R.color.surface_variant_dark);
                    }
                }
                ((Chip) buttonView).setChipBackgroundColorResource(R.color.red_primary);
                if (buttonView == chipClasses) selectedCategory = "Classes";
                else if (buttonView == chipLectureNotes) selectedCategory = "Lecture Notes";
                else if (buttonView == chipAssignments) selectedCategory = "Assignments";
                else if (buttonView == chipExams) selectedCategory = "Exams & Tests";
                else if (buttonView == chipToDo) selectedCategory = "To-Do";
                else if (buttonView == chipReminders) selectedCategory = "Reminders";
                else if (buttonView == chipPersonal) selectedCategory = "Personal";
            } else {
                ((Chip) buttonView).setChipBackgroundColorResource(R.color.surface_variant_dark);
            }
        };

        Chip[] chips = {chipClasses, chipLectureNotes, chipAssignments, chipExams, chipToDo, chipReminders, chipPersonal};
        for (Chip c : chips) {
            if (c != null) {
                c.setOnCheckedChangeListener(chipListener);
            }
        }
    }

    private void loadMemoData() {
        viewModel.getMemoById(currentMemoUid).observe(this, memo -> {
            if (memo != null) {
            if (etTitle != null) {
                    etTitle.setText(memo.title);
            }
            if (etContent != null) {
                    etContent.setText(memo.content);
            }
            if (isEditMode) {
                String category = memo.category != null ? memo.category : "Classes";
                selectedCategory = category;
                if (chipClasses != null && category.equals("Classes")) {
                    chipClasses.setChecked(true);
                } else if (chipLectureNotes != null && category.equals("Lecture Notes")) {
                    chipLectureNotes.setChecked(true);
                } else if (chipAssignments != null && category.equals("Assignments")) {
                    chipAssignments.setChecked(true);
                } else if (chipExams != null && category.equals("Exams & Tests")) {
                    chipExams.setChecked(true);
                } else if (chipToDo != null && category.equals("To-Do")) {
                    chipToDo.setChecked(true);
                } else if (chipReminders != null && category.equals("Reminders")) {
                    chipReminders.setChecked(true);
                } else if (chipPersonal != null && category.equals("Personal")) {
                    chipPersonal.setChecked(true);
                }
                    
                    // Update last edited timestamp
                    TextView tvLastEdited = findViewById(R.id.tvLastEdited);
                    if (tvLastEdited != null && memo.updatedAt > 0) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault());
                        String lastEdited = "LAST EDITED " + sdf.format(new java.util.Date(memo.updatedAt)).toUpperCase();
                        tvLastEdited.setText(lastEdited);
                    }
                } else if (actvCategory != null && memo.category != null) {
                    actvCategory.setText(memo.category, false);
            }
        }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            saveMemo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveMemo() {
        String title = etTitle != null ? etTitle.getText().toString() : "";
        String content = etContent != null ? etContent.getText().toString() : "";
        String category = isEditMode ? selectedCategory : 
                (actvCategory != null ? actvCategory.getText().toString() : "Personal");

        if (isEditMode && currentMemoUid != -1) {
            viewModel.getMemoById(currentMemoUid).observe(this, memo -> {
                if (memo != null) {
                    memo.title = title;
                    memo.content = content;
                    memo.category = category;
                    viewModel.update(memo);
                    finish();
                }
            });
        } else {
            Memo newMemo = new Memo(title, content, category);
            viewModel.insert(newMemo);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return true;
    }
}
