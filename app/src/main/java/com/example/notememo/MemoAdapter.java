package com.example.notememo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {
    private List<Memo> memos;
    private OnMemoClickListener listener;
    private OnMemoLongClickListener longClickListener;

    public interface OnMemoClickListener {
        void onMemoClick(Memo memo);
    }

    public interface OnMemoLongClickListener {
        boolean onMemoLongClick(Memo memo);
    }

    public void setOnMemoLongClickListener(OnMemoLongClickListener listener) {
        this.longClickListener = listener;
    }

    public MemoAdapter(OnMemoClickListener listener) {
        this.listener = listener;
        this.memos = java.util.Collections.emptyList();
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_memo, parent, false);
        return new MemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoViewHolder holder, int position) {
        Memo memo = memos.get(position);
        holder.bind(memo);
    }

    @Override
    public int getItemCount() {
        return memos != null ? memos.size() : 0;
    }

    public void updateMemos(List<Memo> newMemos) {
        if (memos == null) {
            memos = newMemos;
            notifyItemRangeInserted(0, newMemos.size());
            return;
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MemoDiffCallback(memos, newMemos));
        memos = newMemos;
        diffResult.dispatchUpdatesTo(this);
    }

    static class MemoDiffCallback extends DiffUtil.Callback {
        private List<Memo> oldList;
        private List<Memo> newList;

        MemoDiffCallback(List<Memo> oldList, List<Memo> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).uid == newList.get(newItemPosition).uid;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Memo oldMemo = oldList.get(oldItemPosition);
            Memo newMemo = newList.get(newItemPosition);
            return oldMemo.uid == newMemo.uid &&
                    java.util.Objects.equals(oldMemo.title, newMemo.title) &&
                    java.util.Objects.equals(oldMemo.content, newMemo.content) &&
                    java.util.Objects.equals(oldMemo.category, newMemo.category) &&
                    oldMemo.updatedAt == newMemo.updatedAt;
        }
    }

    class MemoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMemoTitle;
        private TextView tvMemoContent;
        private Chip chipCategory;
        private TextView tvTimestamp;
        private ImageView ivMicIcon;

        public MemoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemoTitle = itemView.findViewById(R.id.tvMemoTitle);
            tvMemoContent = itemView.findViewById(R.id.tvMemoContent);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivMicIcon = itemView.findViewById(R.id.ivMicIcon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION && position < memos.size()) {
                    listener.onMemoClick(memos.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (longClickListener != null && position != RecyclerView.NO_POSITION && position < memos.size()) {
                    longClickListener.onMemoLongClick(memos.get(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(Memo memo) {
            tvMemoTitle.setText(memo.title != null ? memo.title : "");
            tvMemoContent.setText(memo.content != null ? memo.content : "");
            chipCategory.setText(memo.category != null ? memo.category : "Personal");

            // Set category color
            int categoryColor = getCategoryColor(memo.category);
            chipCategory.setChipBackgroundColorResource(categoryColor);

            // Format timestamp (using long timestamp)
            tvTimestamp.setText(formatTimestamp(memo.createdAt));

            // Show mic icon for some memos (optional feature)
            if (ivMicIcon != null) {
                ivMicIcon.setVisibility(View.GONE);
            }
        }

        private int getCategoryColor(String category) {
            if (category == null) return R.color.category_personal;
            switch (category) {
                case "Classes":
                    return R.color.category_classes;
                case "Lecture Notes":
                    return R.color.category_lecture_notes;
                case "Assignments":
                    return R.color.category_assignments;
                case "Exams & Tests":
                    return R.color.category_exams;
                case "To-Do":
                    return R.color.category_todo;
                case "Reminders":
                    return R.color.category_reminders;
                case "Personal":
                default:
                    return R.color.category_personal;
            }
        }

        private String formatTimestamp(long timestamp) {
            if (timestamp == 0) return "";

            Date date = new Date(timestamp);
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            long days = diff / (24 * 60 * 60 * 1000);
            long hours = diff / (60 * 60 * 1000);
            long minutes = diff / (60 * 1000);

            if (days > 0) {
                if (days == 1) return "Yesterday";
                if (days < 7) return days + " days ago";
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(date);
            } else if (hours > 0) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (minutes > 0) {
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else {
                return "Just now";
            }
        }
    }
}
