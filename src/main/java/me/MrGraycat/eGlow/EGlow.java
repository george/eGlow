package me.mrgraycat.eglow;

import me.mrgraycat.eglow.addon.LuckPermsAddon;
import me.mrgraycat.eglow.addon.PlaceholderAPIAddon;
import me.mrgraycat.eglow.addon.VaultAddon;
import me.mrgraycat.eglow.addon.citizens.CitizensAddon;
import me.mrgraycat.eglow.addon.internal.AdvancedGlowVisibilityAddon;
import me.mrgraycat.eglow.addon.tab.Listeners.EGlowTABListenerUniv;
import me.mrgraycat.eglow.addon.tab.TABAddon;
import me.mrgraycat.eglow.api.EGlowAPI;
import me.mrgraycat.eglow.command.EGlowCommand;
import me.mrgraycat.eglow.config.EGlowCustomEffectsConfig;
import me.mrgraycat.eglow.config.EGlowMainConfig;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig;
import me.mrgraycat.eglow.config.playerdata.EGlowPlayerdataManager;
import me.mrgraycat.eglow.event.EGlowEventListener;
import me.mrgraycat.eglow.util.DebugUtil;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.packets.NMSHook;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

public class EGlow extends JavaPlugin {
	private static EGlow instance;
	private static EGlowAPI API;
	private boolean UP_TO_DATE = true;

	//Addons
	private AdvancedGlowVisibilityAddon glowAddon;
	private CitizensAddon citizensAddon;
	private TABAddon tabAddon;
	private LuckPermsAddon lpAddon;
	private VaultAddon vaultAddon;

	//TODO
	/*
	Optimize glow blocking system (reduce repeating code)
	 */

	@Override
	public void onEnable() {
		setInstance(this);
		setAPI(new EGlowAPI());

		if (versionIsCompactible()) {
			ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);

			NMSHook.initialize();

			loadConfigs();

			DataManager.initialize();

			registerEventsAndCommands();
			checkForUpdates();
			runAddonHooks();
			runPlayerCheckOnEnable();
		} else {
			ChatUtil.sendToConsole("Disabling eGlow! Your server version is not compatible! (" + DebugUtil.getServerVersion() + ")", false);
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		if (getAdvancedGlowVisibility() != null) {
			getAdvancedGlowVisibility().shutdown();
		}

		if (getLPAddon() != null) {
			getLPAddon().unload();
		}

		runPlayerCheckOnDisable();
	}

	private boolean versionIsCompactible() {
		return !DebugUtil.getServerVersion().equals("v_1_9_R1") && DebugUtil.getMinorVersion() >= 9 && DebugUtil.getMinorVersion() <= 19;
	}

	private void loadConfigs() {
		EGlowMainConfig.initialize();
		EGlowMessageConfig.initialize();
		EGlowCustomEffectsConfig.initialize();
		EGlowPlayerdataManager.initialize();
	}

	private void registerEventsAndCommands() {
		Objects.requireNonNull(getCommand("eglow")).setExecutor(new EGlowCommand());
		new EGlowEventListener();
	}

	private void runAddonHooks() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (MainConfig.ADVANCED_GLOW_VISIBILITY_ENABLE.getBoolean() && getAdvancedGlowVisibility() == null)
					setAdvancedGlowVisibility(new AdvancedGlowVisibilityAddon());
				if (DebugUtil.pluginCheck("PlaceholderAPI"))
					new PlaceholderAPIAddon();
				if (DebugUtil.pluginCheck("Vault"))
					setVaultAddon(new VaultAddon());
				if (DebugUtil.pluginCheck("Citizens") && getCitizensAddon() == null)
					setCitizensAddon(new CitizensAddon());
				if (DebugUtil.pluginCheck("TAB")) {
					try {
						Plugin TAB_Plugin = DebugUtil.getPlugin("TAB");

						if (TAB_Plugin != null && TAB_Plugin.getClass().getName().startsWith("me.neznamy.tab"))
							setTABAddon(new TABAddon(TAB_Plugin));
					} catch (NoClassDefFoundError e) {
						ChatUtil.sendToConsole("&cWarning&f! &cThis version of eGlow requires TAB 3.1.4 or higher!", true);
					}
				}

				EGlow.getEGlowInstance().getServer().getPluginManager().registerEvents(new EGlowTABListenerUniv(), getEGlowInstance());

				if (DebugUtil.pluginCheck("LuckPerms")) {
					setLPAddon(new LuckPermsAddon());
				}
			}
		}.runTask(this);
	}

	private void runPlayerCheckOnEnable() {
		if (!getServer().getOnlinePlayers().isEmpty()) {
			for (Player player : getServer().getOnlinePlayers()) {
				if (DataManager.getEGlowPlayer(player) == null)
					EGlowEventListener.PlayerConnect(player, player.getUniqueId());
			}
		}
	}

	private void runPlayerCheckOnDisable() {
		if (!getServer().getOnlinePlayers().isEmpty()) {
			for (Player player : getServer().getOnlinePlayers()) {
				if (DataManager.getEGlowPlayer(player) == null)
					EGlowEventListener.PlayerDisconnect(player, true);
			}
		}
	}

	private void checkForUpdates() {
		try {
			URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=63295");
			String currentVersion = getEGlowInstance().getDescription().getVersion();
			String latestVersion = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream())).readLine();

			if (currentVersion.contains("PRE")) {
				String betaVersion = currentVersion.split("-")[0];
				setUpToDate(!betaVersion.equals(latestVersion));
			} else {
				if (!latestVersion.contains(currentVersion)) {
					setUpToDate(false);
				}
			}
		} catch (Exception e) {
			//None would care if this fails
		}
	}

	//Setter
	private static void setInstance(EGlow instance) {
		EGlow.instance = instance;
	}

	private void setAPI(EGlowAPI api) {
		EGlow.API = api;
	}

	private void setUpToDate(boolean up_to_date) {
		this.UP_TO_DATE = up_to_date;
	}

	public void setAdvancedGlowVisibility(AdvancedGlowVisibilityAddon glowAddon) {
		this.glowAddon = glowAddon;
	}

	private void setCitizensAddon(CitizensAddon citizensAddon) {
		this.citizensAddon = citizensAddon;
	}

	private void setTABAddon(TABAddon tabAddon) {
		this.tabAddon = tabAddon;
	}

	private void setLPAddon(LuckPermsAddon lpAddon) {
		this.lpAddon = lpAddon;
	}

	private void setVaultAddon(VaultAddon vaultAddon) {
		this.vaultAddon = vaultAddon;
	}

	//Getter
	public static EGlow getEGlowInstance() {
		return EGlow.instance;
	}

	public static EGlowAPI getAPI() {
		return API;
	}

	public boolean isUpToDate() {
		return UP_TO_DATE;
	}

	public AdvancedGlowVisibilityAddon getAdvancedGlowVisibility() {
		return this.glowAddon;
	}

	public CitizensAddon getCitizensAddon() {
		return this.citizensAddon;
	}

	public TABAddon getTABAddon() {
		return this.tabAddon;
	}

	public LuckPermsAddon getLPAddon() {
		return this.lpAddon;
	}

	public VaultAddon getVaultAddon() {
		return this.vaultAddon;
	}
}