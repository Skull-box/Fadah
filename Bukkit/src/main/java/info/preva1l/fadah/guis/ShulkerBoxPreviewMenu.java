package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.Sons;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerBoxPreviewMenu extends FastInv {
    public ShulkerBoxPreviewMenu(Listing listing,
                                 Runnable returnFunction) {
        super(36, Text.extractItemName(listing.getItemStack()), LayoutService.MenuType.SHULKER_PREVIEW);

        if (listing.getItemStack().getItemMeta() instanceof BlockStateMeta im) {
            if (im.getBlockState() instanceof ShulkerBox shulker) {
                for (int i = 0; i < shulker.getInventory().getSize(); i++) {
                    ItemStack itemStack = shulker.getInventory().getItem(i);
                    if (itemStack == null) {
                        itemStack = new ItemBuilder(Material.AIR).build();
                    }
                    setItem(i, itemStack);
                }
            }
        }

        setItem(31, Menus.i().getCloseButton().itemStack(), withSound(Sons.UI_CLICK, e -> returnFunction.run()));

        setItems(new int[]{27, 28, 29, 30, 32, 33, 34, 35}, Menus.i().getBorder().itemStack());
    }
}
