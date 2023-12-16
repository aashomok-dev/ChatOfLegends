package com.ashomok.chatoflegends.activities.main.messaging.update_to_premium;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.utils.LogHelper;

import java.util.List;

/**
 * Created by iuliia on 2/2/18.
 */

public class FeaturesListAdapter extends RecyclerView.Adapter<FeaturesListAdapter.ViewHolder> {
    private static final String TAG = LogHelper.makeLogTag(FeaturesListAdapter.class);
    private final List<FeaturesList.FeatureModel> dataList;
    private final Context context;

    FeaturesListAdapter(List<FeaturesList.FeatureModel> featuresList, Context context) {
        dataList = featuresList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.premium_feature_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FeaturesList.FeatureModel item = getItem(position);
        holder.icon.setImageDrawable(context.getResources().getDrawable(item.getDrawableId()));
        holder.text.setText(item.getStringId());
    }

    private FeaturesList.FeatureModel getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    // Provide a reference to the views for each data item
    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView icon;
        final TextView text;

        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            text = v.findViewById(R.id.text);
        }
    }
}
