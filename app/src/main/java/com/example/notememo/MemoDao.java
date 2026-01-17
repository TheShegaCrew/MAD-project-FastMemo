package com.example.notememo;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import java.util.List;

@Dao
public interface MemoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Memo memo);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Memo memo);

    @Delete
    void delete(Memo memo);

    @Query("SELECT * FROM memo_table ORDER BY updated_at DESC")
    LiveData<List<Memo>> getAllMemos();

    @Query("SELECT * FROM memo_table WHERE uid = :uid LIMIT 1")
    LiveData<Memo> getMemoById(int uid);

    @Query("SELECT * FROM memo_table WHERE category = :category ORDER BY updated_at DESC")
    LiveData<List<Memo>> getMemosByCategory(String category);

    @Query("SELECT * FROM memo_table WHERE category = :category ORDER BY created_at ASC")
    LiveData<List<Memo>> getMemosByCategorySortedByCreatedAsc(String category);

    @Query("SELECT * FROM memo_table WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    LiveData<List<Memo>> searchMemos(String query);

    @Query("SELECT * FROM memo_table WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY created_at ASC")
    LiveData<List<Memo>> searchMemosSortedByCreatedAsc(String query);

    @Query("SELECT * FROM memo_table WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY category ASC, updated_at DESC")
    LiveData<List<Memo>> searchMemosSortedByCategory(String query);

    @Query("SELECT * FROM memo_table WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND category = :category ORDER BY updated_at DESC")
    LiveData<List<Memo>> searchMemosByCategory(String query, String category);

    @Query("SELECT * FROM memo_table WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND category = :category ORDER BY created_at ASC")
    LiveData<List<Memo>> searchMemosByCategorySortedByCreatedAsc(String query, String category);

    @Query("SELECT * FROM memo_table WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND category = :category ORDER BY category ASC, updated_at DESC")
    LiveData<List<Memo>> searchMemosByCategorySortedByCategory(String query, String category);

    // Sorting queries
    @Query("SELECT * FROM memo_table ORDER BY created_at DESC")
    LiveData<List<Memo>> getAllMemosSortedByCreatedDesc();

    @Query("SELECT * FROM memo_table ORDER BY created_at ASC")
    LiveData<List<Memo>> getAllMemosSortedByCreatedAsc();

    @Query("SELECT * FROM memo_table ORDER BY category ASC, updated_at DESC")
    LiveData<List<Memo>> getAllMemosSortedByCategory();
}
