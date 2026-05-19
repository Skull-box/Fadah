package info.preva1l.fadah.records.collection;

import info.preva1l.fadah.data.DataService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created on 23/06/2025
 *
 * @author Preva1l
 */
public final class ImplExpiredItems implements ExpiredItems {
    private final UUID owner;
    private final CopyOnWriteArrayList<CollectableItem> collectableItems;

    public ImplExpiredItems(UUID owner, CopyOnWriteArrayList<CollectableItem> collectableItems) {
        this.owner = owner;
        this.collectableItems = collectableItems;
    }

    /**
     * Create a new empty expired items for a player.
     *
     * @param owner the player to create the expired items for.
     * @return the expired items instance.
     */
    public static ExpiredItems empty(UUID owner) {
        return new ImplExpiredItems(owner, new CopyOnWriteArrayList<>());
    }

    @Override
    public UUID owner() {
        return owner;
    }

    @Override
    public boolean contains(CollectableItem collectableItem) {
        return collectableItems.contains(collectableItem);
    }

    @Override
    public List<CollectableItem> items() {
        return collectableItems;
    }

    @Override
    public void add(CollectableItem collectableItem) {
        collectableItems.add(collectableItem);
        save();
    }

    @Override
    public void remove(CollectableItem collectableItem) {
        collectableItems.remove(collectableItem);
        save();
    }

    private void save() {
        DataService.instance.save(ExpiredItems.class, this);
    }
}
