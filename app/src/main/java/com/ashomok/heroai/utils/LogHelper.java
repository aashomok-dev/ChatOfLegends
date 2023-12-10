/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ashomok.heroai.utils;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

//doc https://firebase.google.com/docs/crashlytics?authuser=1
public enum LogHelper {
    ;

    private static final String LOG_PREFIX = "heroai_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 50;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }
        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }


    public static void v(String tag, Object... messages) {
        log(tag, Log.VERBOSE, null, messages);
    }

    public static void d(String tag, Object... messages) {
        log(tag, Log.DEBUG, null, messages);
    }

    public static void i(String tag, Object... messages) {
        log(tag, Log.INFO, null, messages);
    }

    public static void w(String tag, Object... messages) {
        log(tag, Log.WARN, null, messages);
    }

    public static void w(String tag, Throwable t, Object... messages) {
        log(tag, Log.WARN, t, messages);
    }

    public static void e(String tag, Object... messages) {
        StringBuilder builder = new StringBuilder();
        for (Object o : messages) {
            builder.append(o).append(", ");
        }

        log(tag, Log.ERROR, null, messages);
        String error = tag + ": " + "ERROR: " + builder;
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.recordException(new Throwable(error));
    }

    public static void e(String tag, Throwable t, Object... messages) {
        log(tag, Log.ERROR, t, messages);
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.recordException(t);
        t.printStackTrace();
    }

    private static void log(String tag, int level, Throwable t, Object... messages) {
        String message;
        if (null == t && null != messages && 0 < messages.length) {
            // handle this common case without the extra cost of creating a stringbuffer:
            message = messages[0].toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (null != messages) for (Object m : messages) {
                sb.append(m);
            }
            if (null != t) {
                sb.append("\n").append(Log.getStackTraceString(t));
            }
            message = sb.toString();
        }
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.log(message);

        Log.println(level, tag, message);
    }
}
