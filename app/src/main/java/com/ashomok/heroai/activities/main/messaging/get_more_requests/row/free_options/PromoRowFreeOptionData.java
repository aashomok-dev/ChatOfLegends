package com.ashomok.heroai.activities.main.messaging.get_more_requests.row.free_options;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * Created by iuliia on 3/4/18.
 */
public class PromoRowFreeOptionData {
    private final String id;
    private final int requestsCost; //each promo gives some requests amount
    @DrawableRes
    private final int drawableIconId;
    @StringRes
    private final int titleStringId;

    @StringRes
    private final int subtitleStringId;

    public PromoRowFreeOptionData(String id, @DrawableRes int drawableIconId,
                                  @StringRes int titleStringId,
                                  @StringRes int subtitleStringId, int requestsCost) {
        this.drawableIconId = drawableIconId;
        this.titleStringId = titleStringId;
        this.id = id;
        this.requestsCost = requestsCost;
        this.subtitleStringId = subtitleStringId;
    }

    public int getDrawableIconId() {
        return drawableIconId;
    }

    public int getTitleStringId() {
        return titleStringId;
    }

    public int getSubtitleStringId() {
        return subtitleStringId;
    }

    public String getId() {
        return id;
    }

    public int getRequestsCost() {
        return requestsCost;
    }

    public boolean isSubtitleExists() {
        return 0 != subtitleStringId;
    }
}
