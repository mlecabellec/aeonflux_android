/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-00010 / REQ-00020 / REQ-00030 / REQ-00040 / REQ-00050 / TSK-20260805-001
 * MainActivity with Dual Drawers, Expandable Tree Adapter, Swipe Gestures, and 3-dots Menu.
 */
package com.aeonflux.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.ItemTouchHelper;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aeonflux.app.core.database.entities.ArticleEntity;
import com.aeonflux.app.core.database.entities.SourceEntity;
import com.aeonflux.app.core.database.models.SourceGroupDTO;
import com.aeonflux.app.core.database.models.SourceWithUnreadCount;
import com.aeonflux.app.ui.ItemViewActivity;
import com.aeonflux.app.ui.SettingsActivity;
import com.aeonflux.app.ui.adapters.ArticleAdapter;
import com.aeonflux.app.ui.adapters.SourceTreeAdapter;

import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import java.util.List;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerArticles;
    private ExpandableListView expandableListView;
    private TextView quickSummaryText;

    private ArticleAdapter articleAdapter;
    private SourceTreeAdapter sourceTreeAdapter;
    private final List<ArticleEntity> sampleArticles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        recyclerArticles = findViewById(R.id.recycler_articles);
        expandableListView = findViewById(R.id.expandable_sources_list);
        quickSummaryText = findViewById(R.id.text_quick_summary);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupArticleList();
        setupSourceTree();
        setupGestures();
        setupDrawerCommands();
    }

    private void setupDrawerCommands() {
        findViewById(R.id.btn_add_source).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.aeonflux.app.ui.AddSourceActivity.class));
            drawerLayout.closeDrawers();
        });

        android.view.View containerImportExport = findViewById(R.id.container_import_export_commands);
        findViewById(R.id.btn_toggle_import_export).setOnClickListener(v -> {
            boolean isVisible = containerImportExport.getVisibility() == android.view.View.VISIBLE;
            containerImportExport.setVisibility(isVisible ? android.view.View.GONE : android.view.View.VISIBLE);
        });

        findViewById(R.id.btn_import_opml).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.aeonflux.app.ui.ImportOpmlActivity.class));
            drawerLayout.closeDrawers();
        });

        findViewById(R.id.btn_export_opml).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.aeonflux.app.ui.ExportOpmlActivity.class));
            drawerLayout.closeDrawers();
        });
    }


    @javax.inject.Inject
    com.aeonflux.app.core.database.DatabaseService databaseService;

    private String selectedSourceId = null;

    private void setupArticleList() {
        recyclerArticles.setLayoutManager(new LinearLayoutManager(this));
        articleAdapter = new ArticleAdapter();
        recyclerArticles.setAdapter(articleAdapter);

        articleAdapter.setOnArticleClickListener(article -> {
            android.util.Log.d("AeonFlux_MainActivity", "[DEBUG-LOG] onArticleClick triggered for article ID=" + article.id + ", title='" + article.title + "', url='" + article.url + "'");
            quickSummaryText.setText("Selected: " + article.title + "\n\n" + (article.aiSummary != null ? article.aiSummary : article.title));

            try {
                Intent intent = new Intent(MainActivity.this, ItemViewActivity.class);
                intent.putExtra(ItemViewActivity.EXTRA_ARTICLE_ID, article.id);
                intent.putExtra(ItemViewActivity.EXTRA_TITLE, article.title);
                intent.putExtra(ItemViewActivity.EXTRA_SUMMARY, article.contentCleaned);
                intent.putExtra(ItemViewActivity.EXTRA_URL, article.url);
                intent.putExtra(ItemViewActivity.EXTRA_AUTHOR, article.author);
                intent.putExtra(ItemViewActivity.EXTRA_PUBLISHED_AT, article.publishedAt);
                intent.putExtra(ItemViewActivity.EXTRA_IS_READ, article.isRead == 1);
                android.util.Log.d("AeonFlux_MainActivity", "[DEBUG-LOG] Starting ItemViewActivity intent with extras.");
                startActivity(intent);
            } catch (Exception e) {
                android.util.Log.e("AeonFlux_MainActivity", "[DEBUG-LOG] Exception launching ItemViewActivity!", e);
            }
        });

        observeArticles();
    }

    private boolean isAscendingSort = false;
    private boolean showOnlyUnread = false;

    private void applySortAndFilterAndSetArticles(List<ArticleEntity> articles) {
        if (articles == null || articles.isEmpty()) {
            articleAdapter.setArticles(new ArrayList<>());
            return;
        }
        List<ArticleEntity> filtered = new ArrayList<>();
        for (ArticleEntity a : articles) {
            if (showOnlyUnread && a.isRead == 1) {
                continue;
            }
            filtered.add(a);
        }
        filtered.sort((a1, a2) -> {
            if (isAscendingSort) {
                return Long.compare(a1.publishedAt, a2.publishedAt);
            } else {
                return Long.compare(a2.publishedAt, a1.publishedAt);
            }
        });
        articleAdapter.setArticles(filtered);
    }

    private androidx.lifecycle.Observer<List<ArticleEntity>> currentArticleObserver = null;
    private LiveData<List<ArticleEntity>> currentArticlesLiveData = null;

    private void observeArticles() {
        if (currentArticlesLiveData != null && currentArticleObserver != null) {
            currentArticlesLiveData.removeObserver(currentArticleObserver);
        }

        currentArticleObserver = articles -> applySortAndFilterAndSetArticles(articles);

        if (selectedSourceId == null) {
            currentArticlesLiveData = databaseService.getAllArticlesLiveData();
        } else {
            currentArticlesLiveData = databaseService.getArticlesForSourceLiveData(selectedSourceId);
        }

        currentArticlesLiveData.observe(this, currentArticleObserver);
    }

    private void setupSourceTree() {
        sourceTreeAdapter = new SourceTreeAdapter(this);
        expandableListView.setAdapter(sourceTreeAdapter);

        databaseService.getSourcesWithUnreadCountLiveData().observe(this, sourcesWithUnread -> {
            List<SourceGroupDTO> groups = new ArrayList<>();
            SourceGroupDTO defaultGroup = new SourceGroupDTO("grp_1", "All Feeds & Sources", "#3B82F6");

            if (sourcesWithUnread != null && !sourcesWithUnread.isEmpty()) {
                for (SourceWithUnreadCount swu : sourcesWithUnread) {
                    defaultGroup.addSource(swu);
                }
            } else {
                SourceEntity s1 = new SourceEntity("src_1", "https://news.ycombinator.com/rss", "Hacker News", "Tech news", null, "RSS", 60, System.currentTimeMillis(), 0);
                defaultGroup.addSource(new SourceWithUnreadCount(s1, 3, System.currentTimeMillis()));
            }

            groups.add(defaultGroup);
            sourceTreeAdapter.setGroups(groups);
        });

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            SourceWithUnreadCount child = (SourceWithUnreadCount) sourceTreeAdapter.getChild(groupPosition, childPosition);
            if (child != null && child.source != null) {
                selectedSourceId = child.source.id;
                Toast.makeText(MainActivity.this, "Filtering items for: " + child.source.title, Toast.LENGTH_SHORT).show();
                observeArticles();
                if (recyclerArticles != null) {
                    recyclerArticles.scrollToPosition(0);
                }
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupGestures() {
        swipeRefreshLayout.setOnRefreshListener(this::triggerBackgroundFetch);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    ArticleEntity removed = sampleArticles.remove(pos);
                    articleAdapter.setArticles(new ArrayList<>(sampleArticles));
                    Toast.makeText(MainActivity.this, "Marked read: " + removed.title, Toast.LENGTH_SHORT).show();
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerArticles);
    }

    private void triggerBackgroundFetch() {
        Toast.makeText(MainActivity.this, "Triggering background RSS fetch...", Toast.LENGTH_SHORT).show();
        androidx.work.OneTimeWorkRequest fetchWorkRequest =
            new androidx.work.OneTimeWorkRequest.Builder(com.aeonflux.app.core.fetch.RssFetchWorker.class).build();
        androidx.work.WorkManager.getInstance(getApplicationContext()).enqueue(fetchWorkRequest);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadSampleData() {
        ArticleEntity a1 = new ArticleEntity("art_1", "src_1", "guid_1", "AeonFlux Android Architecture Overview", "<p>Content</p>", "Clean architecture overview for offline-first feed ingestion.", "Aeon Team", System.currentTimeMillis(), "https://aeonflux.dev/arch");
        a1.aiSummary = "Summary of architecture overview.";

        ArticleEntity a2 = new ArticleEntity("art_2", "src_1", "guid_2", "Reactive MVVM & Room Persistence", "<p>Content 2</p>", "Detailed guide on LiveData, Room DAOs, and SQLite state binding.", "Data Specialist", System.currentTimeMillis() - 3600000, "https://aeonflux.dev/room");
        a2.isBookmarked = 1;

        sampleArticles.clear();
        sampleArticles.add(a1);
        sampleArticles.add(a2);
        articleAdapter.setArticles(new ArrayList<>(sampleArticles));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            triggerBackgroundFetch();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, com.aeonflux.app.ui.AboutActivity.class));
            return true;
        } else if (id == R.id.action_toggle_label_grouping) {
            item.setChecked(!item.isChecked());
            Toast.makeText(this, item.isChecked() ? "Grouping by Labels enabled" : "Grouping by Labels disabled", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_sort_item_timestamp_asc) {
            isAscendingSort = true;
            Toast.makeText(this, "Sorted items by Timestamp (ASC)", Toast.LENGTH_SHORT).show();
            if (currentArticlesLiveData != null && currentArticlesLiveData.getValue() != null) {
                applySortAndFilterAndSetArticles(currentArticlesLiveData.getValue());
            }
            return true;
        } else if (id == R.id.action_sort_item_timestamp_desc) {
            isAscendingSort = false;
            Toast.makeText(this, "Sorted items by Timestamp (DESC)", Toast.LENGTH_SHORT).show();
            if (currentArticlesLiveData != null && currentArticlesLiveData.getValue() != null) {
                applySortAndFilterAndSetArticles(currentArticlesLiveData.getValue());
            }
            return true;
        } else if (id == R.id.action_toggle_read_items) {
            item.setChecked(!item.isChecked());
            showOnlyUnread = !item.isChecked();
            Toast.makeText(this, item.isChecked() ? "Showing all items" : "Showing only unread items", Toast.LENGTH_SHORT).show();
            if (currentArticlesLiveData != null && currentArticlesLiveData.getValue() != null) {
                applySortAndFilterAndSetArticles(currentArticlesLiveData.getValue());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
