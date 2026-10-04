package info.preva1l.fadah.utils.serialization;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@UtilityClass
public class ItemSerializer {
    public static String serialize(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack deserialize(String source) {
        if (source == null || source.isEmpty()) return null;
        try {
            return ItemStack.deserializeBytes(Base64.getDecoder().decode(source));
        } catch (RuntimeException e) {
            return deserializeLegacy(source);
        }
    }

    private static ItemStack deserializeLegacy(String source) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(Base64.getMimeDecoder().decode(source));
             BukkitObjectInputStream data = new BukkitObjectInputStream(input)) {
            data.readInt();
            return (ItemStack) data.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
