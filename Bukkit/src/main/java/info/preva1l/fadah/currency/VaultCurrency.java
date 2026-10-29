package info.preva1l.fadah.currency;

import info.preva1l.fadah.config.CurrencySettings;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

@Getter
public class VaultCurrency implements Currency {
    private final String id = "vault";
    private final String requiredPlugin = "Vault";

    @Override
    public String getName() {
        return CurrencySettings.i().getVault().getName();
    }

    @Override
    public char getSymbol() {
        return CurrencySettings.i().getPlayerPoints().getSymbol();
    }

    @Override
    public boolean isEnabled() {
        return CurrencySettings.i().getVault().isEnabled();
    }

    @Override
    public boolean withdraw(OfflinePlayer player, double amountToTake) {
        return requireEconomy().withdrawPlayer(player, amountToTake).transactionSuccess();
    }

    @Override
    public boolean add(OfflinePlayer player, double amountToAdd) {
        return requireEconomy().depositPlayer(player, amountToAdd).transactionSuccess();
    }


    @Override
    public double getBalance(OfflinePlayer player) {
        return requireEconomy().getBalance(player);
    }

    /**
     * Le fournisseur d'économie enregistré auprès de Vault, ou {@code null} s'il n'y en a pas (encore).
     * <p>
     * Relu à chaque appel : le plugin d'économie peut s'activer après Fadah (ordre de chargement, fournisseur
     * enregistré tardivement ou remplacé), et un fournisseur gardé en cache serait alors absent ou périmé.
     *
     * @return l'économie Vault courante, ou null.
     */
    public Economy getEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : rsp.getProvider();
    }

    private Economy requireEconomy() {
        Economy economy = getEconomy();
        if (economy == null) {
            throw new RuntimeException("Vault has no compatible economy plugin.");
        }
        return economy;
    }

    /**
     * Vault seul suffit au chargement : le fournisseur d'économie (SkullboxEssentials, EssentialsX...) peut
     * s'enregistrer après Fadah, il est donc résolu à l'usage et non ici.
     */
    @Override
    public boolean preloadChecks() {
        if (getEconomy() == null) {
            CurrencyService.instance.logger.warning("Vault has no Economy provider yet, it will be looked up when the currency is used.");
        }
        return true;
    }
}
