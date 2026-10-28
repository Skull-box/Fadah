package info.preva1l.fadah.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

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
    private static final Map<UUID, Long> LAST_PLAYED = new ConcurrentHashMap<>();

    private final Sound sound;

    Sons(String path, float volume) {
        // Catégorie des sons d'interface. La catégorie UI n'existe qu'à partir de Minecraft 1.21.6 :
        // en 1.21.4, repli sur VOICE (curseur « Voix/Parole », inutilisé par le jeu vanilla), décidé le
        // 28 oct. 2026. Au passage du réseau en 1.21.6 ou plus, remplacer par Sound.Source.UI.
        this.sound = Sound.sound(Key.key("skullbox", path), Sound.Source.VOICE, volume, 1.0f);
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
