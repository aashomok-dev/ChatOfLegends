package com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.free_options;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.utils.LogHelper;

import java.util.List;

/**
 * Created by iuliia on 3/2/18.
 */

public class PromoListFreeOptionsAdapter extends RecyclerView.Adapter<FreeOptionRowViewHolder>
        implements FreeOptionRowViewHolder.OnButtonClickListener {
    private static final String TAG = LogHelper.makeLogTag(PromoListFreeOptionsAdapter.class);
    private final List<PromoRowFreeOptionData> dataList;

    public final UiDelegatesFactoryFree uiDelegatesFactoryFree;

    public PromoListFreeOptionsAdapter(List<PromoRowFreeOptionData> rowData, UiDelegatesFactoryFree uiDelegatesFactoryFree) {
        dataList = rowData;
        this.uiDelegatesFactoryFree = uiDelegatesFactoryFree;
    }

    @NonNull
    @Override
    public FreeOptionRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.promo_list_free_option_row, parent, false);

        return new FreeOptionRowViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull FreeOptionRowViewHolder holder, int position) {
        PromoRowFreeOptionData data = getItem(position);
        uiDelegatesFactoryFree.onBindViewHolder(data, holder);
    }

    private PromoRowFreeOptionData getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onButtonClicked(int position) {
        PromoRowFreeOptionData data = getItem(position);
        uiDelegatesFactoryFree.onButtonClicked(data);
    }
}
