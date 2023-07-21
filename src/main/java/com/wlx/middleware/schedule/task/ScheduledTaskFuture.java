package com.wlx.middleware.schedule.task;

import java.util.concurrent.ScheduledFuture;

public class ScheduledTaskFuture {

    private ScheduledFuture<?> scheduledFuture;

    public ScheduledTaskFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public void cancel() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }
}
