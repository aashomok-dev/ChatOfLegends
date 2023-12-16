package com.ashomok.chatoflegends.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.model.realms.Model;
import com.ashomok.chatoflegends.utils.heroes.HeroType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum SharedPreferencesManager {
    ;

    private static SharedPreferences mSharedPref;


    public static synchronized void init(Context context) {
        if (null == mSharedPref)
            SharedPreferencesManager.mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setLastActive(long currentTimeMillis) {
        SharedPreferencesManager.mSharedPref.edit().putLong("last_active", currentTimeMillis).apply();
    }

    public static Model getHomelessModel() {
        Model model = new Model();
        model.setBestChoice(false);
        model.setTokenNeeds(1);
        model.setUid("gpt-3-5-homeless");
        model.setModelNamePretty("Homeless");
        model.setModelName("gpt-3.5-turbo");
        model.setHeroType(HeroType.HOMELESS.name());
        model.setIntro(R.string.intro_homeless);
        model.setSystemMsg(R.string.system_msg_homeless);
        return model;
    }

    public static Model getSocraticModel() {
        Model model = new Model();
        model.setBestChoice(false);
        model.setTokenNeeds(1);
        model.setUid("gpt-3-5-socratic");
        model.setModelNamePretty("Socratic philosopher");
        model.setModelName("gpt-3.5-turbo");
        model.setHeroType(HeroType.SOCRATIC.name());
        model.setIntro(R.string.intro_socratic);
        model.setSystemMsg(R.string.system_msg_socratic);
        return model;
    }

    public static Model getEinsteinModel() {
        Model model = new Model();
        model.setBestChoice(false);
        model.setTokenNeeds(1);
        model.setUid("gpt-3-5-einstein");
        model.setModelNamePretty("Einstein scientist");
        model.setModelName("gpt-3.5-turbo");
        model.setHeroType(HeroType.EINSTEIN.name());
        model.setIntro(R.string.intro_einstein);
        model.setSystemMsg(R.string.system_msg_einstein);
        return model;
    }

    public static Model getTeslaModel() {
        Model model = new Model();
        model.setBestChoice(false);
        model.setTokenNeeds(1);
        model.setUid("gpt-3-5-tesla");
        model.setModelNamePretty("Tesla scientist");
        model.setModelName("gpt-3.5-turbo");
        model.setHeroType(HeroType.TESLA.name());
        model.setIntro(R.string.intro_tesla);
        model.setSystemMsg(R.string.system_msg_tesla);
        return model;
    }

    public static Model getElonMuskModel() {
        Model model = new Model();
        model.setBestChoice(false);
        model.setTokenNeeds(1);
        model.setUid("gpt-3-5-musk");
        model.setModelNamePretty("Elon Musk");
        model.setModelName("gpt-3.5-turbo");
        model.setHeroType(HeroType.ELON_MUSK.name());
        model.setIntro(R.string.intro_elon_musk);
        model.setSystemMsg(R.string.system_msg_elon_musk);
        return model;
    }

    public static Model getGamerModel() {
        Model model = new Model();
        model.setBestChoice(false);
        model.setTokenNeeds(1);
        model.setUid("gpt-3-5-gamer");
        model.setModelNamePretty("PC Gamer");
        model.setModelName("gpt-3.5-turbo");
        model.setHeroType(HeroType.GAMER.name());
        model.setIntro(R.string.intro_gamer);
        model.setSystemMsg(R.string.system_msg_gamer);
        return model;
    }

    public static Model getGypsyWomanModel() {
        Model model = new Model();
        model.setBestChoice(false);
        model.setTokenNeeds(1);
        model.setUid("gpt-3-5-gypsy_woman");
        model.setModelNamePretty("Gypsy woman");
        model.setModelName("gpt-3.5-turbo");
        model.setHeroType(HeroType.GYPSY_WOMAN.name());
        model.setIntro(R.string.intro_gypsy_woman);
        model.setSystemMsg(R.string.system_msg_gypsy_woman);
        return model;
    }

    public static Model getBloggerModel() {
        Model model = new Model();
        model.setBestChoice(false);
        model.setTokenNeeds(1);
        model.setUid("gpt-3-5-blogger");
        model.setModelNamePretty("Blogger");
        model.setModelName("gpt-3.5-turbo");
        model.setHeroType(HeroType.BLOGGER.name());
        model.setIntro(R.string.intro_blogger);
        model.setSystemMsg(R.string.system_msg_blogger);
        return model;
    }

    public static Map<String,Model> getModels() {
        Map<String,Model> models = new HashMap<>();
        models.put(getHomelessModel().getUid(), getHomelessModel());
        models.put(getSocraticModel().getUid(), getSocraticModel());
        models.put(getEinsteinModel().getUid(), getEinsteinModel());
        models.put(getTeslaModel().getUid(), getTeslaModel());
        models.put(getGypsyWomanModel().getUid(), getGypsyWomanModel());
        models.put(getElonMuskModel().getUid(), getElonMuskModel());
        models.put(getBloggerModel().getUid(), getBloggerModel());
        models.put(getGamerModel().getUid(), getGamerModel());
        return models;
    }

    public static int getTodayUsedRequestsCount() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return SharedPreferencesManager.mSharedPref.getInt("xxx" + date, 0);
    }

    public static void incrementTodayUsedRequestsCount() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (!SharedPreferencesManager.mSharedPref.contains("xxx" + date)) {
            for (String key : SharedPreferencesManager.mSharedPref.getAll().keySet()) {
                if (key.startsWith("xxx")) {
                    SharedPreferencesManager.mSharedPref.edit().remove(key).apply();
                }
            }
        }
        int todayUsedRequestsCount = SharedPreferencesManager.mSharedPref.getInt("xxx" + date, 0);
        SharedPreferencesManager.mSharedPref.edit().putInt("xxx" + date, ++todayUsedRequestsCount).apply();
    }

    public static void setSettingOptionValue(String fragmentTag, Model gptModel, int progress) {
        SharedPreferencesManager.mSharedPref.edit()
                .putInt(gptModel.getUid()+"_"+fragmentTag, progress).apply();
    }

    public static int getSettingOptionValue(String fragmentTag, Model gptModel, int defaultValue) {
        return SharedPreferencesManager.mSharedPref
                .getInt(gptModel.getUid()+"_"+fragmentTag, defaultValue);
    }

    public static Map<String, Object> getAllValues() {
        return (Map<String, Object>) SharedPreferencesManager.mSharedPref.getAll();
    }
}