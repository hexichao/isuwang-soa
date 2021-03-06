package com.isuwang.dapeng.container.filter.slowtime;

import com.isuwang.dapeng.container.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tangliu on 2016/2/1.
 */
public class TaskManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerUtil.SLOWTIME_LOG);

    private boolean live = false;

    private List<Task> tasks = Collections.synchronizedList(new ArrayList<>());

    private Map<Thread, String> lastStackInfo = new ConcurrentHashMap<>();

    private static final long DEFAULT_SLEEP_TIME = 3000L;
    private static final long MAX_PROCESS_TIME = 10 * 1000;

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void remove(Task task) {
        lastStackInfo.remove(task.getCurrentThread());
        tasks.remove(task);
    }

    public boolean hasStarted() {
        return live;
    }

    public void start() {
        live = true;

        final Thread targetThread = new Thread("Check task time Thread") {
            @Override
            public void run() {
                while (live) {
                    try {
                        checkSampleTask();
                    } catch (Exception e) {
                        LOGGER.error("Check task time thread error", e);
                    }
                }
            }
        };
        targetThread.start();
    }

    public void stop() {
        live = false;
        tasks.clear();
    }

    protected void checkSampleTask() throws InterruptedException {

        final List<Task> tasksCopy = new ArrayList<>(tasks);
        final Iterator<Task> iterator = tasksCopy.iterator();

        while (iterator.hasNext()) {
            final long currentTime = System.currentTimeMillis();
            final Task task = iterator.next();

            final long ptime = currentTime - task.getStartTime();
            if (ptime >= MAX_PROCESS_TIME) {
                final StackTraceElement[] stackElements = task.getCurrentThread().getStackTrace();

                if (stackElements != null && stackElements.length > 0) {
                    final StringBuilder builder = new StringBuilder(task.toString());
                    builder.append(" ").append(ptime).append("ms");

                    final String firstStackInfo = stackElements[0].toString();

                    if (lastStackInfo.containsKey(task.getCurrentThread()) && lastStackInfo.get(task.getCurrentThread()).equals(firstStackInfo))
                        builder.append("\n\t").append("Same as last check...");
                    else {
                        lastStackInfo.put(task.getCurrentThread(), firstStackInfo);

                        builder.append("\n\tat ").append(firstStackInfo.toString());
                        for (int i = 1; i < stackElements.length; i++) {
                            builder.append("\n\tat " + stackElements[i]);
                        }
                    }

                    LOGGER.info("SlowProcess:{}", builder.toString());
                }
            }
        }

        tasksCopy.clear();

        Thread.sleep(DEFAULT_SLEEP_TIME);
    }
}
