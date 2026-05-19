package info.preva1l.fadah.security.impl;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.security.AwareCollectableDataProvider;
import lombok.AllArgsConstructor;

import java.util.concurrent.ExecutorService;

/**
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
@AllArgsConstructor
public final class ExpiredListingsAwareDataProvider implements AwareCollectableDataProvider<ExpiredItems> {
    private final ExecutorService executor;

    @Override
    public void execute(ExpiredItems box, CollectableItem item, Runnable action) {
        CacheAccess.get(ExpiredItems.class, box.owner())
                .ifPresent(b -> {
                    if (!b.contains(item)) return;
                    checkDatabase(b, item, action);
                });
    }

    private void checkDatabase(ExpiredItems box, CollectableItem item, Runnable action) {
        DataService.instance.get(ExpiredItems.class, box.owner())
                .thenAcceptAsync(it -> it.ifPresent(b -> {
                    if (!b.contains(item)) return;
                    action.run();
                }), executor);
    }
}
