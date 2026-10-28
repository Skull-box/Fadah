package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Categories;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.Sons;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import info.preva1l.fadah.utils.guis.PaginatedItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainMenu extends BrowseMenu {
    public MainMenu(
            @Nullable Category category,
            @NotNull Player player,
            @Nullable String search,
            @Nullable SortingMethod sortingMethod,
            @Nullable SortingDirection sortingDirection) {
        super(
                player,
                player,
                LayoutService.MenuType.MAIN,
                () -> CacheAccess.getAll(Listing.class),
                search,
                sortingMethod,
                sortingDirection,
                category
        );
    }

    @Override
    public void fillScrollbarItems() {
        for (Category cat : Categories.getCategories()) {
            ItemBuilder itemBuilder = new ItemBuilder(cat.icon())
                    .name(Text.text(cat.name()))
                    .addLore(Text.list(cat.description()))
                    .modelData(cat.modelData())
                    .attributeSillyStuff();
            if (category == cat) {
                itemBuilder
                        .name(Text.text(cat.name() + "&r " + Lang.i().getCategorySelected()))
                        .glow(true);
            }

            addScrollbarItem(new PaginatedItem(itemBuilder.flags().build(), e -> {
                Sons.UI_CLICK.play(player);
                if (category != cat) {
                    this.category = cat;
                } else {
                    this.category = null;
                }

                updatePagination();
                updateScrollbar();
            }));
        }
    }

    protected void addFilterButtons() {
        super.addFilterButtons();

        // Search
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.SEARCH,-1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.SEARCH,-1),
                new ItemBuilder(getLang().getAsMaterial("filter.search.icon", Material.OAK_SIGN))
                        .name(getLang().getStringFormatted("filter.search.name", "&3&lSearch"))
                        .modelData(getLang().getInt("filter.search.model-data"))
                        .lore(getLang().getLore("filter.search.lore")).build(), withSound(Sons.UI_CLICK, e ->
                        new InputMenu<>(player, Menus.i().getSearchTitle(), getLang().getString("filter.search.placeholder", "Search Query..."), String.class, search ->
                        {
                            Sons.UI_CLICK.play(player);
                            new MainMenu(category, player, search, sortingMethod, sortingDirection).open(player);
                        })));
    }

    protected void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.PROFILE,-1),
                new ItemBuilder(getLang().getAsMaterial("profile-button.icon", Material.PLAYER_HEAD)).skullOwner(player)
                        .name(getLang().getStringFormatted("profile-button.name", "&e&lYour Profile"))
                        .addLore(getLang().getLore("profile-button.lore"))
                        .modelData(getLang().getInt("profile-button.model-data"))
                        .build(), withSound(Sons.UI_CLICK, e -> new ProfileMenu(player, player).open(player)));
    }
}
