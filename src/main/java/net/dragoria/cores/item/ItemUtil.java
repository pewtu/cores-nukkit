package net.dragoria.cores.item;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;
import com.google.inject.Singleton;
import net.dragoria.cores.game.object.team.Team;

@Singleton
public class ItemUtil {

    public void resetPlayer(Player player) {
        for (Effect effect : player.getEffects().values()) {
            player.removeEffect(effect.getId());
        }

        player.setHealth(player.getMaxHealth());
        player.getFoodData().setLevel(20);

        PlayerInventory inventory = player.getInventory();
        inventory.clearAll();
        inventory.setArmorContents(new cn.nukkit.item.Item[4]);

        player.setGamemode(Player.SURVIVAL);
    }

    public void giveItems(Player player, Team team) {
        Item helmet = Item.get(ItemID.LEATHER_CAP);
        CompoundTag helmetTag = new CompoundTag().putInt("customColor", team.getArmorColor());
        helmet.setNamedTag(helmetTag);
        helmet.setCustomName(TextFormat.GRAY + ">> " + TextFormat.GOLD + "Helm");

        Item chestplate = Item.get(ItemID.LEATHER_TUNIC);
        CompoundTag chestplateTag = new CompoundTag().putInt("customColor", team.getArmorColor());
        chestplate.setNamedTag(chestplateTag);
        chestplate.setCustomName(TextFormat.GRAY + ">> " + TextFormat.GOLD + "Brustplatte");

        Item leggings = Item.get(ItemID.LEATHER_PANTS);
        CompoundTag leggingsTag = new CompoundTag().putInt("customColor", team.getArmorColor());
        leggings.setNamedTag(leggingsTag);
        leggings.setCustomName(TextFormat.GRAY + ">> " + TextFormat.GOLD + "Hose");

        Item boots = Item.get(ItemID.LEATHER_BOOTS);
        CompoundTag bootsTag = new CompoundTag().putInt("customColor", team.getArmorColor());
        boots.setNamedTag(bootsTag);
        boots.setCustomName(TextFormat.GRAY + ">> " + TextFormat.GOLD + "Schuhe");

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);

        Item sword = Item.get(ItemID.IRON_SWORD);
        Item bow = Item.get(ItemID.BOW);
        Item axe = Item.get(ItemID.IRON_AXE);
        Item woodPlanks = Item.get(BlockID.WOODEN_PLANKS, 1, 64);
        Item goldenApple = Item.get(ItemID.GOLDEN_APPLE, 0, 16);
        Item arrow = Item.get(ItemID.ARROW, 0, 5);
        Item wood = Item.get(BlockID.WOOD, 0, 32);
        Item pickaxe = Item.get(ItemID.IRON_PICKAXE);

        player.getInventory().setItem(0, sword);
        player.getInventory().setItem(1, bow);
        player.getInventory().setItem(2, axe);
        player.getInventory().setItem(3, woodPlanks);
        player.getInventory().setItem(4, goldenApple);
        player.getInventory().setItem(5, arrow);
        player.getInventory().setItem(6, wood);
        player.getInventory().setItem(7, pickaxe);

    }

    private static boolean isEmptyInventory(Item[] inventory) {
        for (Item item : inventory) {
            if (item != null && item.getId() != BlockID.AIR) {
                return false;
            }
        }
        return true;
    }
}
