package com.ashomok.heroai.activities.main.messaging;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.ashomok.heroai.R;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.GetMoreRequestsActivity;
import com.ashomok.heroai.activities.main.messaging.update_to_premium.UpdateToPremiumActivity;

public class RequestsCounterDialogFragment extends DialogFragment {

    public static RequestsCounterDialogFragment newInstance(@StringRes int message, int messageCost, int requestCount) {
        RequestsCounterDialogFragment frag = new RequestsCounterDialogFragment();
        Bundle args = new Bundle();
        args.putInt("message", message);
        args.putInt("messageCost", messageCost);
        args.putInt("requestCount", requestCount);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.request_counter_dialog_layout, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button getMoreBtn = view.findViewById(R.id.how_we_calculate_btn);
        Button checkPremiumBtn = view.findViewById(R.id.check_premium_btn);
        TextView message = view.findViewById(R.id.message);
        TextView counterText = view.findViewById(R.id.counter_text);

        getMoreBtn.setOnClickListener(view1 -> startGetMoreRequestsActivity());
        checkPremiumBtn.setOnClickListener(view12 -> startUpdateToPremiumActivity());

        int messageResId = getArguments().getInt("message");
        int requestCount = getArguments().getInt("requestCount");
        int messageCost = getArguments().getInt("messageCost");

        String messageText = getString(messageResId, String.valueOf(messageCost), String.valueOf(requestCount));
        message.setText(messageText);
        counterText.setText(String.valueOf(requestCount));
    }

    private void startGetMoreRequestsActivity() {
        Intent intent = new Intent(getContext(), GetMoreRequestsActivity.class);
        startActivity(intent, new Bundle());
        dismissAllowingStateLoss();
    }

    private void startUpdateToPremiumActivity() {
        Intent intent = new Intent(getContext(), UpdateToPremiumActivity.class);
        startActivity(intent, new Bundle());
        dismissAllowingStateLoss();
    }
}