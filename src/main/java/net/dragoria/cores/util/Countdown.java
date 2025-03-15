package net.dragoria.cores.util;

import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dragoria.cores.Cores;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class Countdown {

    private final Cores plugin;

    private final AtomicInteger seconds;
    private final Map<Integer, Runnable> runnableSeconds;
    private final Runnable onFinish;
    private final boolean asyncFinish;

    @Getter
    private boolean finished;

    @Setter
    private Runnable perSecondUpdateListener;

    private Task task;

    public void start() {
        this.task = new Task() {
            @Override
            public void onRun(int currentTick) {
                int currentSecond = Countdown.this.seconds.get();

                Runnable runnable = Countdown.this.runnableSeconds.get(currentSecond);
                if (runnable != null) {
                    runnable.run();
                }

                if (currentSecond <= 0) {
                    if (!asyncFinish) {
                        Server.getInstance().getScheduler().scheduleTask(plugin, onFinish, false);
                    } else {
                        onFinish.run();
                    }

                    Countdown.this.finished = true;
                    this.cancel();
                }

                if (Countdown.this.perSecondUpdateListener != null) {
                    Countdown.this.perSecondUpdateListener.run();
                }

                Countdown.this.seconds.decrementAndGet();
            }
        };

        Server.getInstance().getScheduler().scheduleRepeatingTask(this.plugin, this.task, 20);
    }

    public void cancel() {
        if (this.task != null) {
            this.task.cancel();
            this.finished = true;
        }
    }

    public int getSeconds() {
        return this.seconds.get();
    }

    @NotNull
    public static Countdown fire(int seconds, @NotNull Map<Integer, Runnable> runnableSeconds, @NotNull Runnable onFinish, boolean asyncFinish, Cores plugin) {
        Countdown countdown = new Countdown(plugin, new AtomicInteger(seconds), runnableSeconds, onFinish, asyncFinish);
        countdown.start();
        return countdown;
    }
}
