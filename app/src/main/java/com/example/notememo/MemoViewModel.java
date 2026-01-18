package com.example.notememo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import java.util.List;

public class MemoViewModel extends AndroidViewModel {
    private MemoRepository repository;
    private MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private MutableLiveData<String> selectedCategory = new MutableLiveData<>("All");
    private MutableLiveData<Integer> sortMode = new MutableLiveData<>(MemoRepository.SORT_BY_DATE_DESC);
    private MediatorLiveData<List<Memo>> memoList;
    private LiveData<List<Memo>> currentSource;

    public MemoViewModel(Application application) {
        super(application);
        repository = new MemoRepository(application);
        
        memoList = new MediatorLiveData<>();
        
        // Update memoList whenever any filter changes
        memoList.addSource(searchQuery, query -> updateMemoList());
        memoList.addSource(selectedCategory, category -> updateMemoList());
        memoList.addSource(sortMode, sort -> updateMemoList());
        
        // Initial load
        updateMemoList();
    }

    private void updateMemoList() {
        String query = searchQuery.getValue() != null ? searchQuery.getValue() : "";
        String category = selectedCategory.getValue() != null ? selectedCategory.getValue() : "All";
        int sort = sortMode.getValue() != null ? sortMode.getValue() : MemoRepository.SORT_BY_DATE_DESC;
        
        // Remove previous source if exists
        if (currentSource != null) {
            memoList.removeSource(currentSource);
        }
        
        // Add new source
        currentSource = repository.searchMemos(query, category, sort);
        memoList.addSource(currentSource, memos -> {
            if (memos != null) {
                memoList.setValue(memos);
            }
        });
    }

    public LiveData<List<Memo>> getMemoList() {
        return memoList;
    }

    public MutableLiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public MutableLiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    public MutableLiveData<Integer> getSortMode() {
        return sortMode;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query != null ? query : "");
    }

    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category != null ? category : "All");
    }

    public void setSortMode(int mode) {
        sortMode.setValue(mode);
    }

    public void insert(Memo memo) {
        repository.insert(memo);
    }

    public void update(Memo memo) {
        repository.update(memo);
    }

    public void delete(Memo memo) {
        repository.delete(memo);
    }

    public LiveData<Memo> getMemoById(int uid) {
        return repository.getMemoById(uid);
    }
}
