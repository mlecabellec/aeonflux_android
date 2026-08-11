/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards & Adapter Design Compliance
 * Reference: REQ-00020 / TSK-20260805-001 - Source Tree ExpandableListAdapter
 */
package com.aeonflux.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.aeonflux.app.R;
import com.aeonflux.app.core.database.models.SourceGroupDTO;
import com.aeonflux.app.core.database.models.SourceWithUnreadCount;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * [TSK-20260805-001] Adapter for rendering feeds and sources tree grouped by label.
 */
public class SourceTreeAdapter extends BaseExpandableListAdapter {

    @NonNull
    private final Context context;

    @NonNull
    private List<SourceGroupDTO> groups;

    public SourceTreeAdapter(@NonNull Context context) {
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.groups = new ArrayList<>();
    }

    public void setGroups(@NonNull List<SourceGroupDTO> groups) {
        this.groups = Objects.requireNonNull(groups, "groups must not be null");
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).sources.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).sources.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        SourceGroupDTO group = (SourceGroupDTO) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_source_group, parent, false);
        }

        TextView groupName = convertView.findViewById(R.id.text_group_name);
        TextView groupUnread = convertView.findViewById(R.id.text_group_unread);

        groupName.setText(group.labelName);
        groupUnread.setText(String.valueOf(group.getTotalUnreadCount()));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        SourceWithUnreadCount item = (SourceWithUnreadCount) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_source_child, parent, false);
        }

        TextView sourceName = convertView.findViewById(R.id.text_source_name);
        TextView sourceUnread = convertView.findViewById(R.id.text_source_unread);
        android.widget.ImageView sourceIcon = convertView.findViewById(R.id.image_source_icon);

        sourceName.setText(item.source != null && item.source.title != null ? item.source.title : "Feed");
        sourceUnread.setText(String.valueOf(item.unreadCount));

        if (sourceIcon != null) {
            sourceIcon.setImageResource(android.R.drawable.ic_menu_compass);
            String iconUrl = (item.source != null) ? item.source.iconUrl : null;
            if ((iconUrl == null || iconUrl.trim().isEmpty()) && item.source != null && item.source.url != null && !item.source.url.trim().isEmpty()) {
                try {
                    android.net.Uri uri = android.net.Uri.parse(item.source.url);
                    String host = uri.getHost();
                    if (host != null && !host.trim().isEmpty()) {
                        iconUrl = "https://" + host + "/favicon.ico";
                    }
                } catch (Exception ignored) {}
            }

            if (iconUrl != null && !iconUrl.trim().isEmpty()) {
                final String finalUrl = iconUrl;
                sourceIcon.setTag(finalUrl);
                java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        java.net.URL url = new java.net.URL(finalUrl);
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(3000);
                        conn.setReadTimeout(3000);
                        conn.setInstanceFollowRedirects(true);
                        if (conn.getResponseCode() == 200) {
                            try (java.io.InputStream is = conn.getInputStream()) {
                                final android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(is);
                                if (bmp != null) {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                        if (finalUrl.equals(sourceIcon.getTag())) {
                                            sourceIcon.setImageBitmap(bmp);
                                        }
                                    });
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                });
            }
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
