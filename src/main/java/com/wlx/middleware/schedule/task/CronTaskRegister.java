package com.wlx.middleware.schedule.task;

import com.wlx.middleware.schedule.common.Constants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ScheduledFuture;

@Component("cronTaskRegister")
public class CronTaskRegister implements DisposableBean {

    @Resource
    private TaskScheduler taskScheduler;

    public void addCronTask(SchedulingRunnable task, String cronExpression) {
        if (null != Constants.scheduledTasks.get(task.getTaskId())) {
            removeCronTask(task.getTaskId());
        }
        CronTask cronTask = new CronTask(task, cronExpression);
        Constants.scheduledTasks.put(task.getTaskId(), scheduleTask(cronTask));
    }


    public ScheduledTaskFuture scheduleTask(CronTask cronTask) {
        ScheduledFuture<?> future = taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());
        return new ScheduledTaskFuture(future);
    }

    public void removeCronTask(String taskId) {
        ScheduledTaskFuture scheduledTaskFuture = Constants.scheduledTasks.get(taskId);
        if (scheduledTaskFuture != null) {
            scheduledTaskFuture.cancel();
        }
        Constants.scheduledTasks.remove(taskId);
    }

    @Override
    public void destroy() throws Exception {
        for (ScheduledTaskFuture future : Constants.scheduledTasks.values()) {
            future.cancel();
        }
        Constants.scheduledTasks.clear();
    }
}
