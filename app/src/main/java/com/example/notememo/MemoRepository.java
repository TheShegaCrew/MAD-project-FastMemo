package com.example.notememo;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemoRepository {
    private MemoDao memoDao;
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    // Sorting modes
    public static final int SORT_BY_DATE_DESC = 0;
    public static final int SORT_BY_DATE_ASC = 1;
    public static final int SORT_BY_CATEGORY = 2;

    public MemoRepository(Application application) {
        MemoDatabase db = MemoDatabase.getDatabase(application);
        memoDao = db.memoDao();
    }

    public LiveData<List<Memo>> getAllMemos(int sortMode) {
        switch (sortMode) {
            case SORT_BY_DATE_ASC:
                return memoDao.getAllMemosSortedByCreatedAsc();
            case SORT_BY_CATEGORY:
                return memoDao.getAllMemosSortedByCategory();
            case SORT_BY_DATE_DESC:
            default:
                return memoDao.getAllMemos();
        }
    }

    public LiveData<List<Memo>> getMemosByCategory(String category, int sortMode) {
        if (category == null || category.equals("All")) {
            return getAllMemos(sortMode);
        }
        // For category filter, we still need to apply sorting
        // Since getMemosByCategory doesn't support sort, we'll use getAllMemos and filter in ViewModel
        // Actually, let's add sorted category queries to DAO
        switch (sortMode) {
            case SORT_BY_DATE_ASC:
                return memoDao.getMemosByCategorySortedByCreatedAsc(category);
            case SORT_BY_CATEGORY:
                // When filtering by specific category, all items have same category, so sort by date
                return memoDao.getMemosByCategory(category); // Sort by updated_at DESC
            case SORT_BY_DATE_DESC:
            default:
                return memoDao.getMemosByCategory(category); // Default is updated_at DESC
        }
    }

    public LiveData<List<Memo>> searchMemos(String query, String category, int sortMode) {
        if (query == null || query.isEmpty()) {
            return getMemosByCategory(category, sortMode);
        }
        if (category != null && !category.equals("All")) {
            // Search with category filter and sorting
            switch (sortMode) {
                case SORT_BY_DATE_ASC:
                    return memoDao.searchMemosByCategorySortedByCreatedAsc(query, category);
                case SORT_BY_CATEGORY:
                    return memoDao.searchMemosByCategorySortedByCategory(query, category);
                case SORT_BY_DATE_DESC:
                default:
                    return memoDao.searchMemosByCategory(query, category);
            }
        }
        // Search without category filter but with sorting
        switch (sortMode) {
            case SORT_BY_DATE_ASC:
                return memoDao.searchMemosSortedByCreatedAsc(query);
            case SORT_BY_CATEGORY:
                return memoDao.searchMemosSortedByCategory(query);
            case SORT_BY_DATE_DESC:
            default:
                return memoDao.searchMemos(query);
        }
    }

    public LiveData<Memo> getMemoById(int uid) {
        return memoDao.getMemoById(uid);
    }

    public void insert(Memo memo) {
        executor.execute(() -> {
            memo.setUpdatedAt(System.currentTimeMillis());
            memoDao.insert(memo);
        });
    }

    public void update(Memo memo) {
        executor.execute(() -> {
            memo.setUpdatedAt(System.currentTimeMillis());
            memoDao.update(memo);
        });
    }

    public void delete(Memo memo) {
        executor.execute(() -> memoDao.delete(memo));
    }
}
