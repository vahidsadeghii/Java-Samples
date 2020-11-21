package com.sample.jobmanagement;


import java.util.Random;

public class JobManagement {
    public static void main(String[] args) {
        JobHandler jobHandler = new JobHandler();


        jobHandler.addTask(
                () -> {
                    String value = new Random().ints(97, 123).limit(10).collect(
                            StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append
                    ).toString();

                    System.out.println(String.format("Generated Random Sentence : %s", value));
                    return true;
                }
        );

        jobHandler.addTask(
                () -> {
                    String value = new Random().ints(97, 123).limit(10).collect(
                            StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append
                    ).toString();

                    System.out.println(String.format("Generated Random Sentence : %s", value));
                    return true;
                }
                , JobHandler.Priority.HIGH);


        jobHandler.addTask(
                () -> {
                    int value = new Random().nextInt(100);

                    try {
                        Thread.sleep(2000);

                    } catch (InterruptedException e) {
                        return false;
                    }

                    System.out.println(String.format("Generated Random Value : %d", value));

                    return true;
                }, true
        );

        jobHandler.addTask(
                () -> {
                    System.out.println("Try already failed task !");
                    return false;
                }
        );

        jobHandler.runAllQueueJobs();

        System.out.println(jobHandler.toString());

        try {
            jobHandler.stopAllTasks();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(jobHandler.toString());
    }
}
