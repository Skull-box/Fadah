package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.utils.Sons;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import info.preva1l.fadah.watcher.AuctionWatcher;
import info.preva1l.fadah.watcher.Watching;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class WatchMenu extends FastInv {
    private final Watching watching;
    private final Player player;

    public WatchMenu(Player player) {
        super(LayoutService.MenuType.WATCH);
        this.player = player;
        this.watching = AuctionWatcher.getWatchingListings().getOrDefault(player.getUniqueId(), Watching.base(player));

        fillers();
        buttons();
    }

    private void buttons() {
        setSearch();
        setMinPrice();
        setMaxPrice();
        setComplete();
    }

    private void setSearch() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.SEARCH, -1),
                new ItemBuilder(getLang().getAsMaterial("search.icon"))
                        .name(getLang().getStringFormatted("search.name"))
                        .modelData(getLang().getInt("search.model-data"))
                        .lore(getLang().getLore("search.lore",
                                Tuple.of("%current%", watching.getSearch() == null ? Lang.i().getWords().getNone() : watching.getSearch()))
                        ).build(), withSound(Sons.UI_CLICK, e ->
                        new InputMenu<>(player, Menus.i().getSearchTitle(), getLang().getString("search.placeholder", "Search Query..."), String.class, search -> {
                            watching.setSearch(search);
                            buttons();
                            Sons.UI_CLICK.play(player);
                            open(player);
                        })));
    }

    private void setMinPrice() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.MIN_PRICE, -1),
                new ItemBuilder(getLang().getAsMaterial("min-price.icon"))
                        .name(getLang().getStringFormatted("min-price.name"))
                        .modelData(getLang().getInt("min-price.model-data"))
                        .lore(getLang().getLore("min-price.lore",
                                Tuple.of("%current%", Config.i().getFormatting().numbers().format(watching.getMinPrice())))).build(), withSound(Sons.UI_CLICK, e ->
                        new InputMenu<>(player, Menus.i().getSearchTitle(), getLang().getString("min-price.placeholder", "Ex: 100"), Double.class, search -> {
                            if (search == null) search = -1D;
                            watching.setMinPrice(search);
                            buttons();
                            Sons.UI_CLICK.play(player);
                            open(player);
                        })));
    }

    private void setMaxPrice() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.MAX_PRICE, -1),
                new ItemBuilder(getLang().getAsMaterial("max-price.icon"))
                        .name(getLang().getStringFormatted("max-price.name"))
                        .modelData(getLang().getInt("max-price.model-data"))
                        .lore(getLang().getLore("max-price.lore",
                                Tuple.of("%current%", Config.i().getFormatting().numbers().format(watching.getMaxPrice())))).build(), withSound(Sons.UI_CLICK, e ->
                        new InputMenu<>(player, Menus.i().getSearchTitle(), getLang().getString("max-price.placeholder", "Ex: 10k"), Double.class, search -> {
                            if (search == null) search = -1D;
                            watching.setMaxPrice(search);
                            buttons();
                            Sons.UI_CLICK.play(player);
                            open(player);
                        })));
    }

    private void setComplete() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.LISTING_START, -1),
                new ItemBuilder(getLang().getAsMaterial("start.icon", Material.EMERALD))
                        .name(getLang().getStringFormatted("start.name"))
                        .modelData(getLang().getInt("start.model-data"))
                        .addLore(getLang().getLore("start.lore"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .build(), e -> {
                    AuctionWatcher.watch(watching);
                    e.getWhoClicked().closeInventory();
                    Sons.UI_CONFIRM.play(player);
                });
    }
}
