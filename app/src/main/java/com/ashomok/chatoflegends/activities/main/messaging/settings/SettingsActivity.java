package com.ashomok.chatoflegends.activities.main.messaging.settings;

import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.DEFAULT_GPT_TOKENS_ALLOWED;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.DEFAULT_HISTORY_BUFFER_SIZE;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.DEFAULT_OUTGOING_MESSAGE_LENGTH;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.DEFAULT_TEMPERATURE_INT;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.EXTRA_CURRENT_USER;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.MIN_GPT_TOKENS_ALLOWED;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.MIN_HISTORY_BUFFER_SIZE;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.MIN_OUTGOING_MESSAGE_LENGTH;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.TAG_GPT_TOKENS_ALLOWED;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.TAG_HISTORY_BUFFER_SIZE;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.TAG_MESSAGE_MAX_LENGTH;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.TAG_TEMPERATURE;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.calculateMessageCost;
import static com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_INFINITE;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.activities.main.messaging.RequestsCounterDialogFragment;
import com.ashomok.chatoflegends.activities.main.messaging.billing.BillingViewModelImpl;
import com.ashomok.chatoflegends.activities.main.messaging.update_to_premium.ChatRequestsStateModel;
import com.ashomok.chatoflegends.model.realms.Model;
import com.ashomok.chatoflegends.utils.LogHelper;
import com.ashomok.chatoflegends.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = LogHelper.makeLogTag(SettingsActivity.class);

    private SettingsOptionAdapter adapter;
    private TextView tokensCosts;
    private Model gptModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        initToolbar();

        gptModel = getIntent().getParcelableExtra(EXTRA_CURRENT_USER);
        ArrayList<SettingDialogOption> itemsArrayList = getItemsList();
        adapter = new SettingsOptionAdapter(this, itemsArrayList);
        ListView itemsListView = findViewById(R.id.list_view);
        itemsListView.setAdapter(adapter);

        BillingViewModelImpl billingViewModel =
                new BillingViewModelImpl(getApplication(), ChatRequestsStateModel.Companion.getInstance(getApplication()));
        billingViewModel.getRequestsLiveData().observe(this, requests -> {
            TextView youHaveTokens = findViewById(R.id.you_have_tokens);
            if (null != requests) {
                if (REQUESTS_INFINITE != requests) {
                    youHaveTokens.setText(getString(R.string.you_have_n_tokens, String.valueOf(requests)));
                    youHaveTokens.setOnClickListener(view -> showRequestsCounterDialog(requests));
                } else {
                    youHaveTokens.setText(getString(R.string.you_are_premium));
                }
            }
        });

        Button howWeCalculateBtn = findViewById(R.id.how_we_calculate_btn);
        howWeCalculateBtn.setOnClickListener(view -> new AlertDialog.Builder(view.getContext())
                .setTitle(view.getResources().getString(R.string.how_we_calculate))
                .setMessage(view.getResources().getString(R.string.how_we_calculate_explanation))
                .setPositiveButton(view.getResources().getString(R.string.ok), (dialogInterface, i) -> {
                })
                .show());

        Button setDefault = findViewById(R.id.set_default_btn);
        setDefault.setOnClickListener(view -> setDefaultSettings());
        tokensCosts = findViewById(R.id.tokens_costs);
        tokensCosts.setText(getString(R.string.tokens_costs, String.valueOf(calculateMessageCost(gptModel))));
    }

    private void showRequestsCounterDialog(int requestCount) {
        RequestsCounterDialogFragment requestsCounterDialogFragment =
                RequestsCounterDialogFragment.newInstance(R.string.you_have_tokens, requestCount, requestCount);
        requestsCounterDialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    private void setDefaultSettings() {
        SharedPreferencesManager.setSettingOptionValue(TAG_GPT_TOKENS_ALLOWED, gptModel, DEFAULT_GPT_TOKENS_ALLOWED);
        SharedPreferencesManager.setSettingOptionValue(TAG_HISTORY_BUFFER_SIZE, gptModel, DEFAULT_HISTORY_BUFFER_SIZE);
        SharedPreferencesManager.setSettingOptionValue(TAG_MESSAGE_MAX_LENGTH, gptModel, DEFAULT_OUTGOING_MESSAGE_LENGTH);
        SharedPreferencesManager.setSettingOptionValue(TAG_TEMPERATURE, gptModel, DEFAULT_TEMPERATURE_INT);
        updateSettingsValues();
    }

    private ArrayList<SettingDialogOption> getItemsList() {
        Map<String, Object> allValuesMap = SharedPreferencesManager.getAllValues();
        int gptTokensAllowed = (int) allValuesMap.getOrDefault(gptModel.getUid() + "_" + TAG_GPT_TOKENS_ALLOWED, DEFAULT_GPT_TOKENS_ALLOWED);
        int historyBufferSize = (int) allValuesMap.getOrDefault(gptModel.getUid() + "_" + TAG_HISTORY_BUFFER_SIZE, DEFAULT_HISTORY_BUFFER_SIZE);
        int messageMaxLength = (int) allValuesMap.getOrDefault(gptModel.getUid() + "_" + TAG_MESSAGE_MAX_LENGTH, DEFAULT_OUTGOING_MESSAGE_LENGTH);
        int temperature = (int) allValuesMap.getOrDefault(gptModel.getUid() + "_" + TAG_TEMPERATURE, DEFAULT_TEMPERATURE_INT);

        ArrayList<SettingDialogOption> result = new ArrayList<>();
        result.add(new SettingDialogOption.Builder()
                .fragmentTag(TAG_MESSAGE_MAX_LENGTH)
                .listViewTitleStringResId(R.string.message_max_length)
                .gptModel(gptModel)
                .seekbarSettingTitleStringResId(R.string.symbols_quantity)
                .initialSettingOptionValue(messageMaxLength)
                .minSettingOptionValue(MIN_OUTGOING_MESSAGE_LENGTH)
                .maxSettingOptionValue(DEFAULT_OUTGOING_MESSAGE_LENGTH * 10)
                .dialogTitleStringResId(R.string.choose_message_length)
                .dialogDescriptionStringResId(R.string.maximum_sent_message_length)
                .defaultSettingOptionValue(DEFAULT_OUTGOING_MESSAGE_LENGTH)
                .bigValueWarningStringResId(R.string.long_message_warning)
                .build());
        result.add(new SettingDialogOption.Builder()
                .fragmentTag(TAG_HISTORY_BUFFER_SIZE)
                .gptModel(gptModel)
                .listViewTitleStringResId(R.string.history_buffer_size)
                .seekbarSettingTitleStringResId(R.string.buffer_size)
                .initialSettingOptionValue(historyBufferSize)
                .minSettingOptionValue(MIN_HISTORY_BUFFER_SIZE)
                .maxSettingOptionValue(DEFAULT_HISTORY_BUFFER_SIZE * 10)
                .dialogTitleStringResId(R.string.choose_history_buffer_size)
                .dialogDescriptionStringResId(R.string.default_history_buffer_size_means)
                .defaultSettingOptionValue(DEFAULT_HISTORY_BUFFER_SIZE)
                .bigValueWarningStringResId(R.string.big_buffer_warning)
                .build());
        result.add(new SettingDialogOption.Builder()
                .fragmentTag(TAG_GPT_TOKENS_ALLOWED)
                .gptModel(gptModel)
                .listViewTitleStringResId(R.string.gpt_tokens_allowed)
                .seekbarSettingTitleStringResId(R.string.gpt_tokens)
                .initialSettingOptionValue(gptTokensAllowed)
                .minSettingOptionValue(MIN_GPT_TOKENS_ALLOWED)
                .maxSettingOptionValue(DEFAULT_GPT_TOKENS_ALLOWED * 10)
                .dialogTitleStringResId(R.string.choose_gpt_token_allowed)
                .dialogDescriptionStringResId(R.string.token_will_be_used_means)
                .defaultSettingOptionValue(DEFAULT_GPT_TOKENS_ALLOWED)
                .bigValueWarningStringResId(R.string.many_tokens_usage_warning)
                .build());
        result.add(new SettingDialogOption.Builder()
                .fragmentTag(TAG_TEMPERATURE)
                .gptModel(gptModel)
                .listViewTitleStringResId(R.string.temperature)
                .seekbarSettingTitleStringResId(R.string.temperature_value)
                .initialSettingOptionValue(temperature)
                .minSettingOptionValue(0)
                .maxSettingOptionValue(20)
                .dialogTitleStringResId(R.string.choose_temperature)
                .dialogDescriptionStringResId(R.string.temperature_means)
                .defaultSettingOptionValue(DEFAULT_TEMPERATURE_INT)
                .bigValueWarningStringResId(0)
                .build());
        return result;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateSettingsValues() {
        adapter.notifyDataSetChanged();
        tokensCosts.setText(getString(R.string.tokens_costs, String.valueOf(calculateMessageCost(gptModel))));
    }
}

