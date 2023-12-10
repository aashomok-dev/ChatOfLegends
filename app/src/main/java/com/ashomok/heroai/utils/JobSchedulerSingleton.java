package com.ashomok.heroai.utils;

import android.app.job.JobScheduler;
import android.content.Context;

public enum JobSchedulerSingleton {
    ;
    private static JobScheduler jobScheduler;

    public static JobScheduler getInstance() {
        if (null == jobScheduler) {
            JobSchedulerSingleton.jobScheduler = (JobScheduler) MyApp.context().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        return JobSchedulerSingleton.jobScheduler;
    }
}
