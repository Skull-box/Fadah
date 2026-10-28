package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.Sons;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

public class ProfileMenu extends FastInv {
    private final Player viewer;
    private final OfflinePlayer owner;

    public ProfileMenu(@NotNull Player viewer, @NotNull OfflinePlayer owner) {
        super(LayoutService.MenuType.PROFILE.getLayout().guiSize(),
                LayoutService.MenuType.PROFILE.getLayout().formattedTitle(
                        Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                : owner.getName()),
                        Tuple.of("%username%", owner.getName())),
                LayoutService.MenuType.PROFILE);
        this.viewer = viewer;
        this.owner = owner;

        fillers();
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.BACK, -1),
                Menus.i().getBackButton().itemStack(),
                withSound(Sons.UI_CLICK, e -> new MainMenu(null, viewer, null, null, null).open(viewer)));

        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.PROFILE_SUMMARY, -1),
                new ItemBuilder(getLang().getAsMaterial("profile-button.icon")).skullOwner(owner)
                        .modelData(getLang().getInt("profile-button.model-data"))
                        .name(getLang().getStringFormatted("profile-button.name", "&e&l%dynamic% Profile",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName())))
                        .addLore(getLang().getLore("profile-button.lore",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName()))).build());

        activeListingsButton();
        collectionBoxButton();
        expiredListingsButton();
        historyButton();
    }

    private void activeListingsButton() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.PROFILE_ACTIVE_LISTINGS, -1),
                new ItemBuilder(getLang().getAsMaterial("your-listings.icon", Material.EMERALD))
                        .name(getLang().getStringFormatted("your-listings.name", "&1%dynamic% listings",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName())))
                        .modelData(getLang().getInt("your-listings.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("your-listings.lore",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName()),
                                Tuple.of("%amount%", CacheAccess.amountByPlayer(Listing.class, owner.getUniqueId())))
                        ).build(), e -> {
                    if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.active-listings"))
                            || viewer.getUniqueId() == owner.getUniqueId()) {
                        Sons.UI_CLICK.play(viewer);
                        new ViewListingsMenu(
                                viewer,
                                owner,
                                null,
                                null,
                                null
                        ).open(viewer);
                    } else {
                        Sons.UI_ERROR.play(viewer);
                    }
                });
    }

    private void collectionBoxButton() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.PROFILE_COLLECTION_BOX, -1),
                new ItemBuilder(getLang().getAsMaterial("collection-box.icon", Material.CHEST_MINECART))
                        .name(getLang().getStringFormatted("collection-box.name", "&e%dynamic% Collection Box",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName())))
                        .modelData(getLang().getInt("collection-box.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("collection-box.lore",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName()),
                                Tuple.of("%amount%", CacheAccess.amountByPlayer(CollectionBox.class, owner.getUniqueId())))
                        ).build(), e -> {
                    if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.collection-box"))
                            || viewer.getUniqueId() == owner.getUniqueId()) {
                        Sons.UI_CLICK.play(viewer);
                        new CollectionMenu(viewer, owner, LayoutService.MenuType.COLLECTION_BOX).open(viewer);
                    } else {
                        Sons.UI_ERROR.play(viewer);
                    }
                });
    }

    private void expiredListingsButton() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.PROFILE_EXPIRED_LISTINGS, -1),
                new ItemBuilder(getLang().getAsMaterial("expired-items.icon", Material.ENDER_CHEST))
                        .name(getLang().getStringFormatted("expired-items.name", "&c%dynamic% Expired Listings",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName())))
                        .modelData(getLang().getInt("expired-items.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("expired-items.lore",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName()),
                                Tuple.of("%amount%", CacheAccess.amountByPlayer(ExpiredItems.class, owner.getUniqueId())))
                        ).build(), e -> {
                    if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.expired-listings"))
                            || viewer.getUniqueId() == owner.getUniqueId()) {
                        Sons.UI_CLICK.play(viewer);
                        new CollectionMenu(viewer, owner, LayoutService.MenuType.EXPIRED_LISTINGS).open(viewer);
                    } else {
                        Sons.UI_ERROR.play(viewer);
                    }
                });
    }

    private void historyButton() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.PROFILE_HISTORY, -1),
                new ItemBuilder(getLang().getAsMaterial("historic-items.icon", Material.ENDER_CHEST))
                        .name(getLang().getStringFormatted("historic-items.name", "&c%dynamic% History",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName())))
                        .modelData(getLang().getInt("historic-items.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("historic-items.lore",
                                Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                        ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                        : owner.getName()),
                                Tuple.of("%username%", owner.getName()))).build(), e -> {
                    if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.history"))
                            || viewer.getUniqueId() == owner.getUniqueId()) {
                        Sons.UI_CLICK.play(viewer);
                        new HistoryMenu(viewer, owner, null).open(viewer);
                    } else {
                        Sons.UI_ERROR.play(viewer);
                    }
                });
    }
}