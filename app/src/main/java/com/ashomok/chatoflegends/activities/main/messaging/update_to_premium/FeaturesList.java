package com.ashomok.chatoflegends.activities.main.messaging.update_to_premium;

/**
 * Created by iuliia on 2/2/18.
 */

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class FeaturesList {
    public static final String TAG = LogHelper.makeLogTag(FeaturesList.class);

    public static List<FeatureModel> getList() {
        List<FeatureModel> result = new ArrayList<>();
        result.add(new FeatureModel(R.drawable.baseline_block_24, R.string.no_ads));
        result.add(new FeatureModel(R.drawable.baseline_all_inclusive_24, R.string.unlimited_tokens));
        result.add(new FeatureModel(R.drawable.baseline_language_24, R.string.languages_supported_for_heroai));
        result.add(new FeatureModel(R.drawable.baseline_speed_24, R.string.fast_responses));
        return result;
    }

    public static class FeatureModel {
        @DrawableRes
        private final int drawableId;
        @StringRes
        private final int stringId;

        public FeatureModel(@DrawableRes int drawableId, @StringRes int stringId) {
            this.drawableId = drawableId;
            this.stringId = stringId;
        }

        public int getDrawableId() {
            return drawableId;
        }

        public int getStringId() {
            return stringId;
        }
    }
}
