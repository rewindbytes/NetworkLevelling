package me.rewind.server.NetworkLevelling.Boosters;

import me.rewind.server.NetworkLevelling.NetworkStatistic;
import org.bukkit.entity.Player;

public class Booster<T extends NetworkStatistic> {



	private Player player;
	private T statistic;
	private double boostAmount;
	private long duration;
	BoosterScope scope;
	BoosterType boosterType;

	public Booster(Player player, T statistic, BoosterType boosterType, BoosterScope scope) {
		this.player = player;
		this.statistic = statistic;
		this.boostAmount = boosterType.getBoostIncreasePercentage();
		this.duration = boosterType.getBoosterTime();
		this.scope = scope;
		this.boosterType = boosterType;
	}

	// getters

	public Player getPlayer() {
		return player;
	}

	public T getStatistic() {
		return statistic;
	}

	public double getBoostAmount() {
		return boostAmount;
	}

	public long getDuration() {
		return duration;
	}

	public BoosterScope getScope() {
		return scope;
	}

	public void setScope(BoosterScope scope) {
		this.scope = scope;
	}

	public BoosterType getBoosterType() {
		return boosterType;
	}

	// other methods
}



