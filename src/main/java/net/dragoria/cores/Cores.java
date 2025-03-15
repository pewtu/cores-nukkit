package net.dragoria.cores;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import lombok.Getter;
import net.dragoria.cores.action.ActionBarHandler;
import net.dragoria.cores.command.CommandSetup;
import net.dragoria.cores.config.ConfigService;
import net.dragoria.cores.config.CoresConfig;
import net.dragoria.cores.listener.*;
import net.dragoria.cores.protection.SpawnProtectionManager;

public class Cores extends PluginBase {

    @Getter
    private static Cores instance;
    @Getter
    private final SpawnProtectionManager protectionManager = new SpawnProtectionManager(6, 2);

    @Override
    public void onEnable() {
        instance = this;

        Injector injector = Guice.createInjector(binder -> binder.bind(Cores.class).toInstance(this), new AbstractModule() {
            @Provides
            public CoresConfig provideCoresConfig(ConfigService configService) {
                return configService.getConfig();
            }
        });

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(injector.getInstance(ChatListener.class), this);
        pluginManager.registerEvents(injector.getInstance(CoreBreakListener.class), this);
        pluginManager.registerEvents(injector.getInstance(EntityDamageListener.class), this);
        pluginManager.registerEvents(injector.getInstance(PlayerJoinListener.class), this);
        pluginManager.registerEvents(injector.getInstance(PlayerQuitListener.class), this);

        this.getServer().getCommandMap().register("cores", injector.getInstance(CommandSetup.class));

        injector.getInstance(ActionBarHandler.class).start();
    }

    @Override
    public void onDisable() {
    }
}
