package com.ashomok.heroai.activities.main.messaging.settings;

import static com.ashomok.heroai.activities.main.messaging.settings.SettingDialogOption.BIG_VALUE_WARNING_STRING_RES_ID_TAG;
import static com.ashomok.heroai.activities.main.messaging.settings.SettingDialogOption.DEFAULT_SETTING_OPTION_VALUE_TAG;
import static com.ashomok.heroai.activities.main.messaging.settings.SettingDialogOption.DIALOG_DESCRIPTION_STRING_RES_ID_TAG;
import static com.ashomok.heroai.activities.main.messaging.settings.SettingDialogOption.DIALOG_TITLE_STRING_RES_ID_TAG;
import static com.ashomok.heroai.activities.main.messaging.settings.SettingDialogOption.FRAGMENT_TAG;
import static com.ashomok.heroai.activities.main.messaging.settings.SettingDialogOption.GPTMODEL_TAG;
import static com.ashomok.heroai.activities.main.messaging.settings.SettingDialogOption.MAX_SETTING_OPTION_VALUE_TAG;
import static com.ashomok.heroai.activities.main.messaging.settings.SettingDialogOption.MIN_SETTING_OPTION_VALUE_TAG;
import static com.ashomok.heroai.activities.main.messaging.settings.SettingDialogOption.SEEKBAR_SETTING_TITLE_STRING_RES_ID_TAG;
import static com.ashomok.heroai.activities.main.messaging.settings.TokenCalculatorUtil.calculatePointsPrice;
import static com.ashomok.heroai.activities.main.messaging.settings.TokenCalculatorUtil.convertToFloat;
import static com.ashomok.heroai.activities.main.messaging.settings.TokenCalculatorUtil.TAG_TEMPERATURE;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ashomok.heroai.R;
import com.ashomok.heroai.model.realms.Model;
import com.ashomok.heroai.utils.SharedPreferencesManager;

public class SeekBarSettingDialogFragment extends DialogFragment {

    private TextView seekBarSettingOptionValue;
    private TextView seekBarProgressRequestsNeeds;
    private int defaultSettingOptionValue;
    private int seekbarSettingTitleStringResId;

    private int minSettingOptionValue;
    private String fragmentTag;

    private TextView warningMessage;
    private int bigValueWarningStringResId;

    public static SeekBarSettingDialogFragment newInstance(SettingDialogOption settingDialogOption) {
        SeekBarSettingDialogFragment frag = new SeekBarSettingDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(GPTMODEL_TAG, settingDialogOption.getGptModel());
        args.putInt(DIALOG_TITLE_STRING_RES_ID_TAG, settingDialogOption.getDialogTitleStringResId());
        args.putInt(MIN_SETTING_OPTION_VALUE_TAG, settingDialogOption.getMinSettingOptionValue());
        args.putInt(SEEKBAR_SETTING_TITLE_STRING_RES_ID_TAG, settingDialogOption.getSeekbarSettingTitleStringResId());
        args.putInt(BIG_VALUE_WARNING_STRING_RES_ID_TAG, settingDialogOption.getBigValueWarningStringResId());
        args.putInt(DIALOG_DESCRIPTION_STRING_RES_ID_TAG, settingDialogOption.getDescriptionTitleStringResId());
        args.putInt(MAX_SETTING_OPTION_VALUE_TAG, settingDialogOption.getMaxSettingOptionValue());
        args.putInt(DEFAULT_SETTING_OPTION_VALUE_TAG, settingDialogOption.getDefaultSettingOptionValue());
        args.putString(FRAGMENT_TAG, settingDialogOption.getFragmentTag());
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.setting_option_dialog_layout, container);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Model gptModel = getArguments().getParcelable(GPTMODEL_TAG);
        int dialogTitleStringResId = getArguments().getInt(DIALOG_TITLE_STRING_RES_ID_TAG);
        minSettingOptionValue = getArguments().getInt(MIN_SETTING_OPTION_VALUE_TAG);
        seekbarSettingTitleStringResId = getArguments().getInt(SEEKBAR_SETTING_TITLE_STRING_RES_ID_TAG);
        bigValueWarningStringResId = getArguments().getInt(BIG_VALUE_WARNING_STRING_RES_ID_TAG);
        int dialogDescriptionStringResId = getArguments().getInt(DIALOG_DESCRIPTION_STRING_RES_ID_TAG);
        int maxSettingOptionValue = getArguments().getInt(MAX_SETTING_OPTION_VALUE_TAG);
        defaultSettingOptionValue = getArguments().getInt(DEFAULT_SETTING_OPTION_VALUE_TAG);
        fragmentTag = getArguments().getString(FRAGMENT_TAG);
        warningMessage = view.findViewById(R.id.warning_message);

        TextView title = view.findViewById(R.id.setting_option_title);
        title.setText(dialogTitleStringResId);
        TextView description = view.findViewById(R.id.setting_option_description);
        description.setText(dialogDescriptionStringResId);

        seekBarSettingOptionValue = view.findViewById(R.id.seekBar_progress_text);
        seekBarProgressRequestsNeeds = view.findViewById(R.id.seekBar_progress_requests_needs);
        SeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar.setMin(minSettingOptionValue);
        seekBar.setMax(maxSettingOptionValue);
        int initialSettingOptionValue =
                SharedPreferencesManager.getSettingOptionValue(fragmentTag, gptModel, defaultSettingOptionValue);
        seekBar.setProgress(initialSettingOptionValue);

        if (fragmentTag.equals("Temperature")) {
            seekBarSettingOptionValue.setText(String.format(getContext()
                    .getString(seekbarSettingTitleStringResId), convertToFloat(initialSettingOptionValue)));
            seekBarProgressRequestsNeeds.setText(String.format(
                    getContext().getString(R.string.tokens_needs), String.valueOf(1)));
        } else {
            seekBarSettingOptionValue.setText(String.format(getContext()
                    .getString(seekbarSettingTitleStringResId), String.valueOf(initialSettingOptionValue)));
            seekBarProgressRequestsNeeds.setText(String.format(
                    getContext().getString(R.string.tokens_needs), String.valueOf(
                            calculatePointsPrice(fragmentTag, initialSettingOptionValue, defaultSettingOptionValue))));
        }


        Button applyBtn = view.findViewById(R.id.apply_btn);
        applyBtn.setOnClickListener(view1 -> {
            SharedPreferencesManager.setSettingOptionValue(fragmentTag, gptModel, seekBar.getProgress());
            ((SettingsActivity) requireActivity()).updateSettingsValues();
            dismiss();
        });
        Button cancelBtn = view.findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(view1 -> dismiss());
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (progress == 0) {
                progress = minSettingOptionValue;
            }
            // updated continuously as the user slides the thumb
            if (fragmentTag.equals(TAG_TEMPERATURE)) {
                warningMessage.setVisibility(View.GONE);
                seekBarSettingOptionValue.setText(String.format(getContext()
                        .getString(seekbarSettingTitleStringResId), convertToFloat(progress)));
                seekBarProgressRequestsNeeds.setText(String.format(getContext().getString(R.string.tokens_needs), String.valueOf(1)));
            } else {
                seekBarSettingOptionValue.setText(String.format(getContext()
                        .getString(seekbarSettingTitleStringResId), String.valueOf(progress)));
                seekBarProgressRequestsNeeds.setText(String.format(getContext().getString(R.string.tokens_needs), String.valueOf(
                        calculatePointsPrice(fragmentTag, progress, defaultSettingOptionValue))));
                if (seekBar.getProgress() > defaultSettingOptionValue * 2) {
                    warningMessage.setVisibility(View.VISIBLE);
                    warningMessage.setText(bigValueWarningStringResId);
                } else {
                    warningMessage.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };
}