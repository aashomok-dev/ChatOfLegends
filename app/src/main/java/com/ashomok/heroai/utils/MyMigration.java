package com.ashomok.heroai.utils;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

//this will called when migrating from old version
public class MyMigration implements RealmMigration {
    public static final int SCHEMA_VERSION = 12;

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

    }
}

