package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.utils.Sons;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created on 28/04/2025
 *
 * @author Preva1l
 */
public class PlaceBidMenu extends PurchaseMenu {
    public PlaceBidMenu(BidListing listing,
                        Player player,
                        Runnable returnFunction) {
        this(listing, player, listing.getPrice() + (listing.getPrice() / 2), returnFunction);
    }

    public PlaceBidMenu(BidListing listing,
                        Player player,
                        double amount,
                        Runnable returnFunction
    ) {
        super(listing,
                amount,
                returnFunction,
                () -> {
                    player.closeInventory();
                    listing.newBid(player, amount);
                },
                LayoutService.MenuType.PLACE_BID
        );

        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.ADJUST_BID, -1),
                new ItemBuilder(getLang().getAsMaterial("adjust-bid.icon", Material.ANVIL))
                        .name(getLang().getStringFormatted("adjust-bid.name", "&3&lAdjust Bid Amount"))
                        .modelData(getLang().getInt("adjust-bid.model-data"))
                        .lore(getLang().getLore("adjust-bid.lore",
                                Tuple.of("%current%", Config.i().getFormatting().numbers().format(listing.getPrice())),
                                Tuple.of("%bid%", Config.i().getFormatting().numbers().format(amount))))
                        .build(), withSound(Sons.UI_CLICK, e ->
                    new InputMenu<>(
                            player,
                            getLang().getString("input.title", ""),
                            getLang().getString("input.placeholder", ""),
                            Double.class,
                            a -> {
                                if (a == null || a <= listing.getPrice()) {
                                    Sons.UI_ERROR.play(player);
                                    Lang.sendMessage(player, Lang.i().getErrors().getBidTooLow());
                                    return;
                                }
                                Sons.UI_CLICK.play(player);
                                new PlaceBidMenu(listing, player, a, returnFunction).open(player);
                            }
                    )
                ));
    }
}