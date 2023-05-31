package rankednetwork.NetworkLevelling.Boosters;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import rankednetwork.NetworkLevelling.Config.DiscordConfigDefaults;
import rankednetwork.NetworkLevelling.Notifiers.GameNotifier;
import rankednetwork.NetworkLevelling.Webhooks.DiscordWebhook;

import java.lang.reflect.Field;
import java.util.*;

public class BoosterQueue {
	private Queue<Booster<?>> personalBoosterQueue = new LinkedList<>();
	private Queue<Booster<?>> globalBoosterQueue = new LinkedList<>();

	//Serialize all booster releated queues


	public void addBooster(Booster<?> booster) {
		BoosterManager.getInstance().getBoosterActivationTimes().put(booster, System.currentTimeMillis());
		if (booster.getScope() == BoosterScope.PERSONAL) {
			if (personalBoosterQueue.contains(booster)) {
				return;
			}
			personalBoosterQueue.add(booster);

		} else if (booster.getScope() == BoosterScope.GLOBAL) {


			globalBoosterQueue.add(booster);
		}
	}

	public long getTimeUntilActive(Booster<?> booster) {
		Queue<Booster<?>> relevantQueue;
		if (booster.getScope() == BoosterScope.PERSONAL) {
			relevantQueue = personalBoosterQueue;
		} else if (booster.getScope() == BoosterScope.GLOBAL) {
			relevantQueue = globalBoosterQueue;
		} else {
			throw new IllegalArgumentException("Unknown booster scope");
		}
		if (!relevantQueue.contains(booster)) {
			throw new IllegalArgumentException("Booster is not in queue");
		}

		long totalTime = 0;
		for (Booster<?> b : relevantQueue) {
			if (b == booster) {
				break;
			}
			long boosterDuration;
			if (b.isActive()) {
				// If the booster is active, use the remaining time instead of the total duration
				boosterDuration = b.getRemainingTime();
			} else {
				// If the booster is not active, use its total duration
				boosterDuration = b.getDurationInMinutes();  // This is in minutes
			}
			totalTime += boosterDuration;
		}

		return totalTime + 1;
	}


	public void displayBoosterQueue(Player player) {
		player.sendMessage(ChatColor.GOLD + "----- Booster Queue -----");
		if (getGlobalBoosterQueue().isEmpty()) {
			player.sendMessage(ChatColor.YELLOW + "The booster queue is empty.");
			player.sendMessage(BoosterManager.getInstance().getBoosterActivationTimes().toString());
			player.sendMessage(getGlobalBoosterQueue().toString());
		} else {
			int position = 1;
			for (Booster<?> booster : getGlobalBoosterQueue()) {
				String boosterName = booster.getBoosterName();
				String timeStatus;
				if (booster.isActive()) {
					timeStatus = (booster.getRemainingTime() < 0) ? " ending soon!" : booster.getRemainingTime() + " minutes remaining! ";
				} else if (getGlobalBoosterQueue().peek().equals(booster)) {
					timeStatus = "1 minute until activation! ";
				} else {
					timeStatus = getTimeUntilActive(booster) + " minutes until activation! ";
				}
				player.sendMessage(ChatColor.YELLOW + "Position " + position + ": " + boosterName + " (" + timeStatus + ") " + booster.getStatus());
				position++;
			}
		}
		player.sendMessage(ChatColor.GOLD + "-------------------------");
	}

	public Queue<Booster<?>> getPersonalBoosterQueue() {
		return personalBoosterQueue;
	}

	public Queue<Booster<?>> getGlobalBoosterQueue() {
		return globalBoosterQueue;
	}

	public Runnable checkAndActivateNextBooster() {
		return new Runnable() {
			@Override
			public void run() {
				// Check and activate next global booster
				checkAndActivateBoosterInQueue(globalBoosterQueue);
				// Check and activate next personal booster for each player
				checkAndActivateBoosterInQueue(personalBoosterQueue);
			}


			private void checkAndActivateBoosterInQueue(Queue<Booster<?>> boosterQueue) {
				if (!boosterQueue.isEmpty() && boosterQueue.peek().getRemainingTime() <= 0) {
					// Remove the expired booster
					Booster<?> expiredBooster = boosterQueue.poll();
					BoosterManager.getInstance().getBoosterActivationTimes().remove(expiredBooster);
					expiredBooster.getPlayer().sendMessage(ChatColor.GOLD + " Your booster has expired!");

					if (!boosterQueue.isEmpty()) {
						// If there's another booster in the queue, activate it
						Booster<?> nextBooster = boosterQueue.peek();
						nextBooster.activate();

						// Inform the player about the booster activation
						Player player = nextBooster.getPlayer();
						String boosterName = nextBooster.getBoosterName();
						player.sendMessage(ChatColor.YELLOW + "Your booster " + boosterName + " is now being used!");

						// Send notification message
						GameNotifier gameNotifier = new GameNotifier();
						gameNotifier.sendActivatedMessage(nextBooster);

						// Send discord notification
						if(nextBooster.getScope().equals(BoosterScope.GLOBAL)){
							sendGlobalDiscordMessage(nextBooster);
						}
						//TODO Move discord to a method so that when players that activate boosters first in queue get notified.
						// Record the activation time
						long activationTime = System.currentTimeMillis();
						BoosterManager.getInstance().getBoosterActivationTimes().put(nextBooster, activationTime);
					}
				}
			}
		};
	}

	public void sendGlobalDiscordMessage(Booster booster) {
		YamlConfiguration discordConfigDefaults = DiscordConfigDefaults.getDiscordSettings();

		ConfigurationSection general = discordConfigDefaults.getConfigurationSection("general");



		if(general == null){
			general = discordConfigDefaults.createSection("general");
		}

		if(!general.isSet("webhook_url")){
			general.set("webhook_url", "");
		}

		String url = general.get("webhook_url").toString();

		DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject()
				.setColor(randomColor())
				.setTitle(booster.getBoosterName() + " Booster Activation!")
				.setThumbnail("https://png.pngtree.com/png-clipart/20191120/original/pngtree-store-icon-in-line-style-png-image_5053711.jpg")
				.setFooter("Thank you for the support!", "https://png.pngtree.com/png-clipart/20191120/original/pngtree-store-icon-in-line-style-png-image_5053711.jpg")
				.setDescription(booster.getPlayer().getName() + " has activated a " + booster.getBoosterType().getBoostIncreasePercentage() + "% " + booster.getStatistic().getName() + " Booster!");
		BoosterManager.getInstance().sendDiscordMessage(url, booster, embedObject);
	}

	public Color randomColor() {
		Field[] declaredFields = Color.class.getDeclaredFields();
		List<Color> colors = new ArrayList<>();
		for (Field field : declaredFields) {
			if (field.getType() == Color.class) {
				try {
					colors.add((Color) field.get(null));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return colors.get(new Random().nextInt(colors.size() - 1));
	}
}

