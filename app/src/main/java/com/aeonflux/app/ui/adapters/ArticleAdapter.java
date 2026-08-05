/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards & Article Adapter Design Compliance
 * Reference: REQ-00030 / REQ-00040 / TSK-20260805-001 - Article RecyclerView Adapter
 */
package com.aeonflux.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aeonflux.app.R;
import com.aeonflux.app.core.database.entities.ArticleEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * [TSK-20260805-001] RecyclerView Adapter for dynamic infinite scroll article feed items.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    public interface OnArticleClickListener {
        void onArticleClick(@NonNull ArticleEntity article);
    }

    @NonNull
    private List<ArticleEntity> articles;

    private OnArticleClickListener listener;

    public ArticleAdapter() {
        this.articles = new ArrayList<>();
    }

    public void setArticles(@NonNull List<ArticleEntity> articles) {
        this.articles = Objects.requireNonNull(articles, "articles must not be null");
        notifyDataSetChanged();
    }

    public void setOnArticleClickListener(OnArticleClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    public ArticleEntity getArticleAt(int position) {
        return articles.get(position);
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article_card, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        ArticleEntity article = articles.get(position);
        holder.bind(article, listener);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView authorDateText;
        private final TextView summaryText;
        private final ImageView starredImg;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_article_title);
            authorDateText = itemView.findViewById(R.id.text_article_author_date);
            summaryText = itemView.findViewById(R.id.text_article_summary);
            starredImg = itemView.findViewById(R.id.img_starred);
        }

        public void bind(@NonNull ArticleEntity article, OnArticleClickListener listener) {
            titleText.setText(article.title);
            String authorStr = article.author != null ? article.author : "Unknown Author";
            authorDateText.setText(authorStr + " • " + article.publishedAt);
            summaryText.setText(article.contentCleaned != null ? article.contentCleaned : (article.aiSummary != null ? article.aiSummary : "No content preview."));
            starredImg.setVisibility(article.isBookmarked == 1 ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onArticleClick(article);
                }
            });
        }
    }
}
