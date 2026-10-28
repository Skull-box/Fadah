package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.Sons;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import org.bukkit.Material;

/**
 * Created on 28/04/2025
 *
 * @author Preva1l
 */
public abstract class PurchaseMenu extends FastInv {
    public PurchaseMenu(Listing listing,
                        double amount,
                        Runnable returnFunction,
                        Runnable confirmFunction,
                        LayoutService.MenuType menuType) {
        super(menuType.getLayout().guiSize(), menuType.getLayout().formattedTitle(), menuType);

        fillers();
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.CONFIRM, -1),
                new ItemBuilder(getLang().getAsMaterial("confirm.icon", Material.LIME_CONCRETE))
                        .name(getLang().getStringFormatted("confirm.name", "&a&lCONFIRM"))
                        .modelData(getLang().getInt("confirm.model-data"))
                        .lore(getLang().getLore("confirm.lore",
                                Tuple.of("%price%", Config.i().getFormatting().numbers().format(amount))))
                        .build(),
                e -> confirmFunction.run());

        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.CANCEL, -1),
                new ItemBuilder(getLang().getAsMaterial("cancel.icon", Material.RED_CONCRETE))
                        .name(getLang().getStringFormatted("cancel.name", "&c&lCANCEL"))
                        .modelData(getLang().getInt("cancel.model-data"))
                        .lore(getLang().getLore("cancel.lore")).build(), withSound(Sons.UI_CANCEL, e -> returnFunction.run()));

        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.ITEM_TO_PURCHASE, -1),
                listing.getItemStack().clone());
    }
}
