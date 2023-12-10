package com.ashomok.heroai.activities.main.messaging.settings;
import com.ashomok.heroai.model.realms.Model;

public class SettingDialogOption {
    public static final String GPTMODEL_TAG = "gpt_model";
    public static final String FRAGMENT_TAG = "fragment_tag";
    public static final String MIN_SETTING_OPTION_VALUE_TAG = "min_setting_option_value";
    public static final String DEFAULT_SETTING_OPTION_VALUE_TAG = "default_setting_option_value";
    public static final String MAX_SETTING_OPTION_VALUE_TAG = "max_setting_option_value";
    public static final String DIALOG_TITLE_STRING_RES_ID_TAG = "dialog_title_string_res_id";
    public static final String DIALOG_DESCRIPTION_STRING_RES_ID_TAG = "description_title_string_res_id";
    public static final String SEEKBAR_SETTING_TITLE_STRING_RES_ID_TAG = "seekbar_setting_title_string_res_id";
    public static final String BIG_VALUE_WARNING_STRING_RES_ID_TAG = "big_value_warning_string_res_id";
   private  String fragmentTag;
    private int minSettingOptionValue;
    private int maxSettingOptionValue;

    private int listViewTitleStringResId;
    private int dialogTitleStringResId;
    private int descriptionTitleStringResId;
    private int seekbarSettingTitleStringResId;
    private  int bigValueWarningStringResId;
    private  int defaultSettingOptionValue;
    private Model gptModel;

    public String getFragmentTag(){ return fragmentTag;}
    public int getListViewTitleStringResId(){ return listViewTitleStringResId;}
    public int getMinSettingOptionValue(){ return minSettingOptionValue;}
    public int getMaxSettingOptionValue(){ return maxSettingOptionValue;}
    public int getDialogTitleStringResId(){ return dialogTitleStringResId;}
    public int getDescriptionTitleStringResId(){ return descriptionTitleStringResId;}
    public int getSeekbarSettingTitleStringResId(){ return seekbarSettingTitleStringResId;}
    public int getBigValueWarningStringResId(){ return bigValueWarningStringResId;}
    public int getDefaultSettingOptionValue(){ return defaultSettingOptionValue;}
    public Model getGptModel(){ return gptModel;}

    public static class Builder {

        private String fragmentTag;
        private int listViewTitleStringResId;
        private int textviewInSettingListResId;
        private int initialSettingOptionValue;
        private int minSettingOptionValue;
        private int maxSettingOptionValue;
        private int dialogTitleStringResId;
        private int dialogDescriptionStringResId;
        private int seekbarSettingTitleStringResId;
        private int bigValueWarningStringResId;
        private int defaultSettingOptionValue;
        private Model gptModel;

        public Builder() {
        }

        Builder(String fragmentTag, int listViewTitleStringResId, int initialSettingOptionValue, int minSettingOptionValue, int maxSettingOptionValue, int dialogTitleStringResId, int dialogDescriptionStringResId, int seekbarSettingTitleStringResId, int bigValueWarningStringResId, int defaultSettingOptionValue, Model gptModel) {
            this.fragmentTag = fragmentTag;
            this.listViewTitleStringResId = listViewTitleStringResId;
            this.initialSettingOptionValue = initialSettingOptionValue;
            this.minSettingOptionValue = minSettingOptionValue;
            this.maxSettingOptionValue = maxSettingOptionValue;
            this.dialogTitleStringResId = dialogTitleStringResId;
            this.dialogDescriptionStringResId = dialogDescriptionStringResId;
            this.seekbarSettingTitleStringResId = seekbarSettingTitleStringResId;
            this.bigValueWarningStringResId = bigValueWarningStringResId;
            this.defaultSettingOptionValue = defaultSettingOptionValue;
            this.gptModel = gptModel;
        }

        public Builder listViewTitleStringResId(int listViewTitleStringResId){
            this.listViewTitleStringResId = listViewTitleStringResId;
            return Builder.this;
        }

        public Builder fragmentTag(String fragmentTag){
            this.fragmentTag = fragmentTag;
            return Builder.this;
        }

        public Builder initialSettingOptionValue(int initialSettingOptionValue){
            this.initialSettingOptionValue = initialSettingOptionValue;
            return Builder.this;
        }

        public Builder minSettingOptionValue(int minSettingOptionValue){
            this.minSettingOptionValue = minSettingOptionValue;
            return Builder.this;
        }

        public Builder maxSettingOptionValue(int maxSettingOptionValue){
            this.maxSettingOptionValue = maxSettingOptionValue;
            return Builder.this;
        }

        public Builder dialogTitleStringResId(int dialogTitleStringResId){
            this.dialogTitleStringResId = dialogTitleStringResId;
            return Builder.this;
        }

        public Builder dialogDescriptionStringResId(int dialogDescriptionStringResId){
            this.dialogDescriptionStringResId = dialogDescriptionStringResId;
            return Builder.this;
        }

        public Builder seekbarSettingTitleStringResId(int seekbarSettingTitleStringResId){
            this.seekbarSettingTitleStringResId = seekbarSettingTitleStringResId;
            return Builder.this;
        }

        public Builder bigValueWarningStringResId(int bigValueWarningStringResId){
            this.bigValueWarningStringResId = bigValueWarningStringResId;
            return Builder.this;
        }

        public Builder defaultSettingOptionValue(int defaultSettingOptionValue){
            this.defaultSettingOptionValue = defaultSettingOptionValue;
            return Builder.this;
        }

        public Builder gptModel(Model gptModel){
            this.gptModel = gptModel;
            return Builder.this;
        }

        public SettingDialogOption build() {
            return new SettingDialogOption(this);
        }
    }

    private SettingDialogOption(Builder builder) {
        this.fragmentTag = builder.fragmentTag;
        this.listViewTitleStringResId=builder.listViewTitleStringResId;
        this.minSettingOptionValue = builder.minSettingOptionValue;
        this.maxSettingOptionValue = builder.maxSettingOptionValue;
        this.dialogTitleStringResId = builder.dialogTitleStringResId;
        this.descriptionTitleStringResId = builder.dialogDescriptionStringResId;
        this.seekbarSettingTitleStringResId = builder.seekbarSettingTitleStringResId;
        this.bigValueWarningStringResId = builder.bigValueWarningStringResId;
        this.defaultSettingOptionValue = builder.defaultSettingOptionValue;
        this.gptModel = builder.gptModel;
    }

}
