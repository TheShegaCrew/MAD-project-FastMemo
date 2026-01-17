package com.example.notememo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MemoDetailsActivity extends AppCompatActivity {
    private MemoViewModel viewModel;
    private TextView tvMemoTitle;
    private TextView tvMemoContent;
    private TextView tvMetadata;
    private TextView tvUpdated;
    private Chip chipCategory;
    private int memoUid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_memo_details);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(MemoViewModel.class);
        
        memoUid = getIntent().getIntExtra("memo_uid", -1);
        if (memoUid == -1) {
            // Try legacy string id
            String memoId = getIntent().getStringExtra("memo_id");
            if (memoId != null) {
                try {
                    memoUid = Integer.parseInt(memoId);
                } catch (NumberFormatException e) {
                    finish();
                    return;
                }
            } else {
                finish();
                return;
            }
        }

        setupToolbar();
        initViews();
            loadMemoData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("");
                getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_revert);
            }
        }
    }

    private void initViews() {
        tvMemoTitle = findViewById(R.id.tvMemoTitle);
        tvMemoContent = findViewById(R.id.tvMemoContent);
        tvMetadata = findViewById(R.id.tvMetadata);
        tvUpdated = findViewById(R.id.tvUpdated);
        chipCategory = findViewById(R.id.chipCategory);
    }

    private void loadMemoData() {
        viewModel.getMemoById(memoUid).observe(this, memo -> {
            if (memo == null) {
                finish();
                return;
            }

            if (tvMemoTitle != null) {
                tvMemoTitle.setText(memo.title != null ? memo.title : "");
            }

            if (tvMemoContent != null) {
                tvMemoContent.setText(memo.content != null ? memo.content : "");
            }

            if (tvMetadata != null && memo.createdAt > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                tvMetadata.setText(sdf.format(new Date(memo.createdAt)));
            }

            if (tvUpdated != null && memo.updatedAt > 0) {
                long diff = System.currentTimeMillis() - memo.updatedAt;
                if (diff < 60000) {
                    tvUpdated.setText("Updated just now");
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                    tvUpdated.setText("Updated " + sdf.format(new Date(memo.updatedAt)));
                }
            }

            if (chipCategory != null && memo.category != null) {
                chipCategory.setText(memo.category);
                // Set category color
                int categoryColor = getCategoryColor(memo.category);
                chipCategory.setChipBackgroundColorResource(categoryColor);
            }
        });
    }

    private int getCategoryColor(String category) {
        if (category == null) return R.color.category_personal;
        switch (category) {
            case "Classes":
                return R.color.category_work;
            case "Lecture Notes":
                return R.color.category_ideas;
            case "Assignments":
                return R.color.category_work;
            case "Exams & Tests":
                return R.color.category_work;
            case "To-Do":
                return R.color.category_personal;
            case "Reminders":
                return R.color.category_ideas;
            case "Personal":
            default:
                return R.color.category_personal;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(this, MemoEditorActivity.class);
            intent.putExtra("memo_uid", memoUid);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_delete) {
            showDeleteConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        // Get the memo once and delete it
        viewModel.getMemoById(memoUid).observe(this, memo -> {
            if (memo != null) {
                // Remove observer to avoid multiple calls
                viewModel.getMemoById(memoUid).removeObservers(this);
                
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Delete Memo")
                        .setMessage("Are you sure you want to delete \"" + (memo.title != null && !memo.title.isEmpty() ? memo.title : "this memo") + "\"? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.delete(memo);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_details, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ViewModel will automatically update via LiveData
    }
}
