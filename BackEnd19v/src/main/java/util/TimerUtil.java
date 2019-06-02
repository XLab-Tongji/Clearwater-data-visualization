package util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerUtil {

    private ScheduledExecutorService scheduledExecutorService;

    /**
     * @param runnable 方法
     * @param time 延迟执行时间
     * @param period 执行周期
     */
    public void scheduleAtFixedRate(Runnable runnable,Long time,int period ){
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(runnable, time, period, TimeUnit.SECONDS);
    }

    public void shutdown(){
        if(!scheduledExecutorService.isShutdown()){
            scheduledExecutorService.shutdown();
        }

    }

}
