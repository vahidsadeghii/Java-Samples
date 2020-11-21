package com.sample.jobmanagement;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobHandler {
    public enum Priority {
        HIGH, NORMAL, LOW
    }

    @Data
    @Builder
    @EqualsAndHashCode(callSuper = false)
    private static class Job implements Runnable {

        enum State {
            QUEUE, GOING_TO_RUN, RUNNING, SUCCESS, FAILED
        }

        private long id;
        private volatile State state;
        private Callable<Boolean> task;
        private int priority;

        @Override
        public void run() {
            state = State.RUNNING;
            boolean runTaskResultStatus;

            try {
                runTaskResultStatus = task.call();

            } catch (Exception e) {
                runTaskResultStatus = false;
            }

            state = runTaskResultStatus ? State.SUCCESS : State.FAILED;
        }
    }

    private final List<Job> jobs = new ArrayList<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100);

    public void addTask(Callable task) {
        addTask(task, Priority.NORMAL);
    }

    public void addTask(Callable task, Priority priority) {
        addTask(task, null, priority);
    }

    public void addTask(Callable task, boolean runImmediate) {
        addTask(task, runImmediate, Priority.NORMAL);
    }

    public void addTask(Callable task, boolean runImmediate, Priority priority) {
        if (runImmediate)
            addTask(task, LocalDateTime.now().plusSeconds(10), priority);

        else
            addTask(task, null, priority);
    }

    public void addTask(Callable task, LocalDateTime executeTime, Priority priority) {
        Job job = Job.builder()
                .id(System.currentTimeMillis())
                .state(Job.State.QUEUE)
                .task(task)
                .priority(
                        priority == Priority.HIGH ? Thread.MAX_PRIORITY :
                                priority == Priority.NORMAL ? Thread.NORM_PRIORITY : Thread.MIN_PRIORITY
                )
                .build();

        jobs.add(job);

        if (executeTime != null)
            startJob(job, executeTime);
    }

    public void runAllQueueJobs() {
        jobs.stream()
                .filter(job -> job.state == Job.State.QUEUE)
                .sorted(Comparator.comparing(Job::getPriority).reversed())
                .forEach(job -> {
                    startJob(job, LocalDateTime.now().plusSeconds(1));
                });
    }

    private void startJob(Job job, LocalDateTime executeTime) {
        job.setState(Job.State.GOING_TO_RUN);
        long delay = ChronoUnit.SECONDS.between(LocalDateTime.now(), executeTime);
        executorService.schedule(job, delay, TimeUnit.SECONDS);
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Job job : jobs)
            stringBuilder.append(String.format("Job with id:%d and status:%s", job.id, job.state.name()))
                    .append("\n");

        return stringBuilder.toString();
    }

    public void stopAllTasks() throws InterruptedException {
        executorService.shutdown();
        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
            System.out.println("Still not terminated...");
        }
    }
}
