package net.dragoria.cores.listener;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityFallingBlock;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityBlockChangeEvent;
import cn.nukkit.level.Location;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import net.dragoria.cores.protection.SpawnProtectionManager;

@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class SpawnProtectionListener implements Listener {

    private final SpawnProtectionManager protectionManager;

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location blockLocation = event.getBlock().getLocation();

        if (protectionManager.isWithinSpawnProtection(blockLocation)) {
            event.setCancelled(true);
        } else if (protectionManager.isWithinCoreProtection(blockLocation)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location blockLocation = event.getBlock().getLocation();

        if (protectionManager.isWithinSpawnProtection(blockLocation)) {
            event.setCancelled(true);
        } else if (protectionManager.isWithinCoreProtection(blockLocation)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFallingBlock(EntityBlockChangeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof EntityFallingBlock) {
            Location blockLocation = entity.getLocation();

            if (protectionManager.isWithinCoreProtection(blockLocation)) {
                event.setCancelled(true);
                //todo entity remove
            }
        }
    }
}
