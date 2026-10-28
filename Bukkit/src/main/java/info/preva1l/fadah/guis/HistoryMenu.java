package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.utils.Sons;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import info.preva1l.fadah.utils.guis.PaginatedFastInv;
import info.preva1l.fadah.utils.guis.PaginatedItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HistoryMenu extends PaginatedFastInv {
    private final Player viewer;
    private final OfflinePlayer owner;
    private final String dateSearch;

    public HistoryMenu(Player viewer, OfflinePlayer owner, @Nullable String dateSearch) {
        super(LayoutService.MenuType.HISTORY.getLayout().guiSize(), LayoutService.MenuType.HISTORY.getLayout().formattedTitle(
                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                        : owner.getName()),
                Tuple.of("%username%", owner.getName())),
                viewer, LayoutService.MenuType.HISTORY,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
        this.viewer = viewer;
        this.owner = owner;
        this.dateSearch = dateSearch;

        fillers();
        setPaginationMappings(getLayout().paginationSlots());

        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected Sons openSound() {
        // L'historique est un menu-journal.
        return Sons.UI_BOOK;
    }

    @Override
    protected synchronized void fillPaginationItems() {
        getAndFilterHistoricItems().forEach(this::createAndAddPaginatedItem);
    }

    private void createAndAddPaginatedItem(@NotNull HistoricItem historicItem) {
        ItemBuilder itemBuilder = new ItemBuilder(historicItem.itemStack().clone());

        if (historicItem.playerUUID() != null) {
            addLoreWithPlayer(itemBuilder, historicItem);
        } else if (hasValidPrice(historicItem)) {
            addLoreWithPrice(itemBuilder, historicItem);
        } else {
            addBasicLore(itemBuilder, historicItem);
        }

        addPaginationItem(new PaginatedItem(itemBuilder.build()));
    }

    private void addLoreWithPlayer(@NotNull ItemBuilder itemBuilder, @NotNull HistoricItem historicItem) {
        String playerName = getPlayerName(historicItem.playerUUID());

        if (historicItem.action() == HistoricItem.LoggedAction.LISTING_SOLD) {
            itemBuilder.addLore(getLang().getLore(player, "lore-with-buyer",
                    Tuple.of("%action%", historicItem.action().getLocaleActionName()),
                    Tuple.of("%buyer%", playerName),
                    Tuple.of("%price%", formatPrice(historicItem.price())),
                    Tuple.of("%date%", TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()))
            ));
        } else {
            itemBuilder.addLore(getLang().getLore(player, "lore-with-seller",
                    Tuple.of("%action%", historicItem.action().getLocaleActionName()),
                    Tuple.of("%seller%", playerName),
                    Tuple.of("%price%", formatPrice(historicItem.price())),
                    Tuple.of("%date%", TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()))
            ));
        }
    }

    private void addLoreWithPrice(@NotNull ItemBuilder itemBuilder, @NotNull HistoricItem historicItem) {
        itemBuilder.addLore(getLang().getLore(player, "lore-with-price",
                Tuple.of("%action%", historicItem.action().getLocaleActionName()),
                Tuple.of("%price%", formatPrice(historicItem.price())),
                Tuple.of("%date%", TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()))
        ));
    }

    private void addBasicLore(@NotNull ItemBuilder itemBuilder, @NotNull HistoricItem historicItem) {
        itemBuilder.addLore(getLang().getLore(player, "lore",
                Tuple.of("%action%", historicItem.action().getLocaleActionName()),
                Tuple.of("%date%", TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()))
        ));
    }

    private boolean hasValidPrice(@Nullable HistoricItem historicItem) {
        return historicItem != null &&
                historicItem.price() != null &&
                historicItem.price() > 0.0;
    }

    private String formatPrice(@Nullable Double price) {
        if (price == null) return "0";

        try {
            return Config.i().getFormatting().numbers().format(price);
        } catch (Exception e) {
            return String.valueOf(price);
        }
    }

    private String getPlayerName(@Nullable UUID playerUUID) {
        if (playerUUID == null) return Lang.i().getWords().getNone();

        try {
            String player = Bukkit.getOfflinePlayer(playerUUID).getName();
            return player == null ? Lang.i().getWords().getNone() : player;
        } catch (Exception e) {
            return Lang.i().getWords().getNone();
        }
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.BACK, -1),
                Menus.i().getBackButton().itemStack(), withSound(Sons.UI_CLICK, e -> new ProfileMenu(viewer, owner).open(viewer)));

        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.SEARCH, -1),
                new ItemBuilder(getLang().getAsMaterial("search.icon", Material.OAK_SIGN))
                        .name(getLang().getStringFormatted("search.name", "&eSearch Date"))
                        .modelData(getLang().getInt("search.model-data"))
                        .lore(getLang().getLore("search.lore")).build(), withSound(Sons.UI_CLICK, e ->
                        new InputMenu<>(viewer, Menus.i().getSearchTitle(), getLang().getString("search.placeholder", "Ex: 21/04/2024 22:26"), String.class,
                                search -> {
                                    Sons.UI_CLICK.play(viewer);
                                    new HistoryMenu(viewer, owner, search).open(viewer);
                                })));
    }

    private List<HistoricItem> getAndFilterHistoricItems() {
        List<HistoricItem> items = new ArrayList<>(CacheAccess.getNotNull(History.class, owner.getUniqueId()).items());

        if (dateSearch != null && !dateSearch.trim().isEmpty())
            items.removeIf(item -> !doesItemMatchDateSearch(item));

        items.sort(HistoricItem::compareTo);
        return items;
    }

    private boolean doesItemMatchDateSearch(@NotNull HistoricItem item) {
        try {
            String formattedDate = TimeUtil.formatTimeToVisualDate(item.loggedDate());
            return formattedDate.toLowerCase().contains(dateSearch.toLowerCase().trim());
        } catch (Exception e) {
            return false;
        }
    }
}