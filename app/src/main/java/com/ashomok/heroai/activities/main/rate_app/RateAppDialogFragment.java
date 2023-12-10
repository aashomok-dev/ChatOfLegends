package com.ashomok.heroai.activities.main.rate_app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ashomok.heroai.R;

/**
 * Created by iuliia on 10/5/16.
 */

public class RateAppDialogFragment extends DialogFragment {
    private OnNeverAskReachedListener onStopAskListener;

    public static RateAppDialogFragment newInstance() {
        return new RateAppDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.rate_app_dialog_layout, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button laterBtn = view.findViewById(R.id.later);
        Button neverBtn = view.findViewById(R.id.never);
        Button okBtn = view.findViewById(R.id.ok);

        laterBtn.setOnClickListener(view1 -> onLaterBtnClicked());
        neverBtn.setOnClickListener(view1 -> onNeverBtnClicked());
        okBtn.setOnClickListener(view1 -> onOkBtnClicked());
    }

    private void onOkBtnClicked() {
        rate();
        onStopAskListener.onStopAsk();
        dismiss();
    }

    private void onNeverBtnClicked() {
        onStopAskListener.onStopAsk();
        dismiss();
    }

    private void onLaterBtnClicked() {
        dismiss();
    }

    public void setOnStopAskListener(OnNeverAskReachedListener onStopAskListener) {
        this.onStopAskListener = onStopAskListener;
    }

    private void rate() {
        RateAppUtils rateAppUtils = new RateAppUtils();
        rateAppUtils.rate(getActivity());
    }
}
