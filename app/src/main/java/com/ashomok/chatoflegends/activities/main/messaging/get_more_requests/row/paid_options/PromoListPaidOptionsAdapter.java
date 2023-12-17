package com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.ProductDetails;
import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class PromoListPaidOptionsAdapter extends RecyclerView.Adapter<PaidOptionRowViewHolder>
        implements PaidOptionRowViewHolder.OnRowClickListener {
    private static final String TAG = LogHelper.makeLogTag(PromoListPaidOptionsAdapter.class);

    private final List<ProductDetails> dataList = new ArrayList<>();
    private final UiDelegatesFactoryPaid uiDelegatesFactoryPaid;

    public PromoListPaidOptionsAdapter(UiDelegatesFactoryPaid uiDelegatesFactoryPaid) {
        this.uiDelegatesFactoryPaid = uiDelegatesFactoryPaid;
    }

    public List<ProductDetails> getDataList() {
        return dataList;
    }

    @NonNull
    @Override
    public PaidOptionRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.promo_list_paid_option_row, parent, false);
        return new PaidOptionRowViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull PaidOptionRowViewHolder holder, int position) {
        ProductDetails data = getItem(position);
        uiDelegatesFactoryPaid.onBindViewHolder(data, holder);
    }

    private ProductDetails getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onRowClicked(int position) {
        ProductDetails data = getItem(position);
        uiDelegatesFactoryPaid.onButtonClicked(data);
    }
}
