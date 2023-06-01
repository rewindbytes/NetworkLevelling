package rankednetwork.NetworkLevelling.Boosters.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import rankednetwork.NetworkLevelling.Boosters.Booster;
import rankednetwork.NetworkLevelling.Boosters.BoosterScope;
import rankednetwork.NetworkLevelling.Boosters.BoosterType;
import rankednetwork.NetworkLevelling.NetworkStatistic;

/**
 * This event is called every 1 minute
 */
public class BoosterActiveEvent extends BoosterEvent {

	private static final HandlerList handlers = new HandlerList();

	public BoosterActiveEvent(Player player, String boosterName, Double boostAmount, BoosterScope scope, BoosterType boosterType, NetworkStatistic statistic, Booster.Status status) {
		super(player, boosterName, boostAmount, scope, boosterType, statistic, status);
	}

	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}
}

