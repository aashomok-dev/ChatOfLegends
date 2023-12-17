package com.ashomok.chatoflegends.utils.heroes;

import com.ashomok.chatoflegends.R;

public class HeroesSystemSelector {

    public static int getSystemMessage(HeroType heroType) {
        switch (heroType) {
            case SOCRATIC:
                return R.string.system_msg_socratic;
            case HOMELESS:
                return R.string.system_msg_homeless;
            case EINSTEIN:
                return R.string.system_msg_einstein;
            case ELON_MUSK:
                return R.string.system_msg_elon_musk;
            case TESLA:
                return R.string.system_msg_tesla;
            case GYPSY_WOMAN:
                return R.string.system_msg_gypsy_woman;
            case GAMER:
                return R.string.system_msg_gamer;
            case BLOGGER:
                return R.string.system_msg_blogger;
            default:
                return 0;
        }
    }

}
