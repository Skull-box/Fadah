package info.preva1l.fadah.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sons d'interface {@code skullbox:*} du pack. Copie locale du catalogue {@code SkullboxSound} de SkullboxCore
 * (ce plugin n'en dépend pas) : mêmes ids, mêmes volumes. Un même type d'action joue toujours le même son.
 * <p>
 * Le son va au seul joueur et suit sa tête ({@link Sound.Emitter#self()}).
 * <p>
 * Un seul son par geste : un second son pour le même joueur dans les {@value #GESTURE_WINDOW_MS} ms est ignoré.
 * Un bouton qui joue son propre son (Annuler, Retour) puis rouvre un menu n'entend donc pas aussi le son d'ouverture.
 * <p>
 * Les sons d'ouverture ne se jouent que pour un menu ouvert directement (commande, objet) : voir
 * {@link #ouverture(Player)}. Un menu atteint par un clic reste muet, le bouton cliqué joue {@link #UI_CLICK}.
 */
public enum Sons {

    /** Ouverture de tout menu. */
    UI_OPEN("ui.open", 0.45f),
    /** Ouverture d'un menu-journal : historique. */
    UI_BOOK("ui.book", 0.5f),
    /** Ouverture d'un stockage : collection d'enchères. */
    UI_STORAGE("ui.storage", 0.5f),
    /** Tout clic de bouton qui agit (onglet, filtre, sélection). */
    UI_CLICK("ui.click", 0.35f),
    /** Bascule on/off. */
    UI_TOGGLE("ui.toggle", 0.35f),
    /** Page suivante ou précédente. */
    UI_PAGE("ui.page", 0.45f),
    /** Fermeture par un bouton « Fermer » ou « Retour ». */
    UI_CLOSE("ui.close", 0.4f),
    /** « Oui » dans un écran de confirmation, ou action menée à bien. */
    UI_CONFIRM("ui.confirm", 0.6f),
    /** « Non » ou « Annuler » dans un écran de confirmation. */
    UI_CANCEL("ui.cancel", 0.5f),
    /** Refus : pas assez d'argent, pas la permission, inventaire plein, cooldown. */
    UI_ERROR("ui.error", 0.5f),
    /** Achat en $ courant : hôtel des ventes, enchère. */
    SHOP_PURCHASE_SMALL("shop.purchase.small", 0.55f);

    private static final long GESTURE_WINDOW_MS = 200;
    /** Fenêtre après un clic de menu pendant laquelle une ouverture compte comme une navigation. */
    private static final long NAVIGATION_WINDOW_MS = 1000;
    private static final Map<UUID, Long> LAST_MENU_CLICK = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_PLAYED = new ConcurrentHashMap<>();

    private final Sound sound;

    Sons(String path, float volume) {
        // Catégorie des sons d'interface (UI, disponible depuis Minecraft 1.21.6).
        this.sound = Sound.sound(Key.key("skullbox", path), Sound.Source.UI, volume, 1.0f);
    }

    /** Vrai si le joueur a cliqué dans un menu (tout plugin) il y a moins d'une seconde. */
    public static boolean enNavigation(Player player) {
        Long at = LAST_MENU_CLICK.get(player.getUniqueId());
        return at != null && System.currentTimeMillis() - at < NAVIGATION_WINDOW_MS;
    }

    /**
     * Joue ce son d'ouverture de menu, sauf si le joueur navigue : un clic de menu vient d'avoir lieu, le bouton a
     * joué son propre clic et le menu d'arrivée reste muet.
     */
    public void ouverture(Player player) {
        if (player != null && !enNavigation(player)) {
            play(player);
        }
    }

    /**
     * Détecteur de navigation : note tout clic dans la partie haute d'un menu (n'importe quel plugin, zMenu compris)
     * au tout début du traitement, avant que le menu suivant ne s'ouvre. Copie locale du détecteur de SkullboxCore.
     */
    public static final class Navigation implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onMenuClick(InventoryClickEvent event) {
            Inventory top = event.getView().getTopInventory();
            InventoryType type = top.getType();
            if (type == InventoryType.CRAFTING || type == InventoryType.CREATIVE || type == InventoryType.PLAYER) {
                return; // inventaire du joueur seul : pas un menu
            }
            if (event.getClickedInventory() != top) {
                return; // clic dans la partie basse (inventaire du joueur)
            }
            LAST_MENU_CLICK.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent event) {
            LAST_MENU_CLICK.remove(event.getPlayer().getUniqueId());
        }
    }

    /** Joue le son pour ce joueur seulement (sans effet si un son d'interface vient déjà de lui être joué). */
    public void play(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        long now = System.currentTimeMillis();
        // La fenêtre court depuis le premier son du geste, pas depuis le dernier ignoré.
        boolean[] allowed = {true};
        LAST_PLAYED.compute(player.getUniqueId(), (id, last) -> {
            if (last != null && now - last < GESTURE_WINDOW_MS) {
                allowed[0] = false;
                return last;
            }
            return now;
        });
        if (!allowed[0]) {
            return;
        }
        if (LAST_PLAYED.size() > 256) {
            LAST_PLAYED.values().removeIf(t -> now - t > 10_000);
        }
        player.playSound(sound, Sound.Emitter.self());
    }
}
