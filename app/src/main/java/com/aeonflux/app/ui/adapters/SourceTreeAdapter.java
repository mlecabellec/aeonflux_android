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

        sourceName.setText(item.source.title);
        sourceUnread.setText(String.valueOf(item.unreadCount));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
