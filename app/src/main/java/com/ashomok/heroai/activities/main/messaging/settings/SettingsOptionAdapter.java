package com.ashomok.heroai.activities.main.messaging.settings;


import static com.ashomok.heroai.activities.main.messaging.settings.TokenCalculatorUtil.TAG_TEMPERATURE;
import static com.ashomok.heroai.activities.main.messaging.settings.TokenCalculatorUtil.calculatePointsPrice;
import static com.ashomok.heroai.activities.main.messaging.settings.TokenCalculatorUtil.convertToFloat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.ashomok.heroai.R;
import com.ashomok.heroai.utils.SharedPreferencesManager;

import java.util.ArrayList;

public class SettingsOptionAdapter extends BaseAdapter {
    Context context;

    ArrayList<SettingDialogOption> items;

    public SettingsOptionAdapter(Context activity, ArrayList<SettingDialogOption> settingDialogOptions) {
        this.context = activity;
        this.items = settingDialogOptions;
    }

    @Override
    public int getCount() {
        return items.size(); //returns total of items in the list
    }

    @Override
    public SettingDialogOption getItem(int position) {
        return items.get(position); //returns list item at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_setting, parent, false);
        }

        SettingDialogOption settingDialogOption = (SettingDialogOption) getItem(position);
        TextView settingTitle = convertView.findViewById(R.id.settingTitle);
        TextView settingValue = convertView.findViewById(R.id.settingValue);
        TextView messagePrice = convertView.findViewById(R.id.message_price);

        messagePrice.setText(context.getString(R.string.x_num, String.valueOf(
                calculatePointsPrice(settingDialogOption.getFragmentTag(),
                        SharedPreferencesManager.getSettingOptionValue(
                                settingDialogOption.getFragmentTag(),
                                settingDialogOption.getGptModel(),
                                settingDialogOption.getDefaultSettingOptionValue()),
                        settingDialogOption.getDefaultSettingOptionValue()
                ))));
        settingTitle.setText(settingDialogOption.getListViewTitleStringResId());
        if (settingDialogOption.getFragmentTag().equals(TAG_TEMPERATURE)) {
            settingValue.setText(String.valueOf(convertToFloat(SharedPreferencesManager.getSettingOptionValue(
                    settingDialogOption.getFragmentTag(),
                    settingDialogOption.getGptModel(),
                    settingDialogOption.getDefaultSettingOptionValue()))));
        } else {
            settingValue.setText(String.valueOf(SharedPreferencesManager.getSettingOptionValue(
                    settingDialogOption.getFragmentTag(),
                    settingDialogOption.getGptModel(),
                    settingDialogOption.getDefaultSettingOptionValue())));
        }

        View settingLayout = convertView.findViewById(R.id.settingLayout);
        settingLayout.setOnClickListener(
                view -> {
                    SeekBarSettingDialogFragment dialogFragment =
                            SeekBarSettingDialogFragment.newInstance(settingDialogOption);
                    if (context != null) {
                        dialogFragment.show(((FragmentActivity) context).getSupportFragmentManager(),
                                settingDialogOption.getFragmentTag());
                    }
                });
        return convertView;
    }
}