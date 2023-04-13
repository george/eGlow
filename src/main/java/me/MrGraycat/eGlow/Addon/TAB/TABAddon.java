package me.mrgraycat.eglow.addon.tab;

import lombok.Getter;
import lombok.Setter;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static me.mrgraycat.eglow.EGlow.getEGlowInstance;
import static me.neznamy.tab.api.TabAPI.getInstance;

public class TABAddon {
	@Getter
	@Setter
	private boolean versionSupported;
	@Getter
	@Setter
	private boolean settingNametagPrefixSuffixEnabled;
	@Getter
	@Setter
	private boolean settingTeamPacketBlockingEnabled;

	public TABAddon(Plugin TAB_Plugin) {
		int TAB_Version = (TAB_Plugin != null) ? Integer.parseInt(TAB_Plugin.getDescription().getVersion().replaceAll("[^\\d]", "")) : 0;

		if (TAB_Version < 314) {
			ChatUtil.sendToConsole(this.getOutdatedVersionMessage(), true);
			return;
		}

		loadTABSettings();

		TabAPI.getInstance().getEventBus().register(TabLoadEvent.class, event -> new BukkitRunnable() {
			@Override
			public void run() {
				try {
					TABAddon TAB_Addon = getEGlowInstance().getTABAddon();
					TAB_Addon.loadTABSettings();

					if (TAB_Addon.blockEGlowPackets()) {
						for (EGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
							if (ePlayer.getFakeGlowStatus() || ePlayer.getGlowStatus())
								TAB_Addon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
						}
					} else {
						cancel();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(getEGlowInstance()));

		setVersionSupported(true);
	}

	public void loadTABSettings() {
		ConfigurationFile TAB_Config = getInstance().getConfig();

		setSettingNametagPrefixSuffixEnabled(TAB_Config.getBoolean("scoreboard-teams.enabled", false));
		setSettingTeamPacketBlockingEnabled(TAB_Config.getBoolean("scoreboard-teams.anti-override", false));

		if (MainConfig.SETTINGS_SMART_TAB_NAMETAG_HANDLER.getBoolean()) {
			if (!TAB_Config.getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
				TAB_Config.set("scoreboard-teams.unlimited-nametag-mode.enabled", true);
				ChatUtil.sendToConsole("&6Enabled unlimited-nametag-mode in TAB&f! &6Please reload TAB or restart the server&f.", true);
			}
		}
	}

	public void updateTABPlayer(EGlowPlayer ePlayer, ChatColor glowColor) {
		TabPlayer tabPlayer = getTABPlayer(ePlayer.getUUID());

		if (tabPlayer == null || getInstance().getTeamManager() == null)
			return;

		String tagPrefix;
		String color = (glowColor.equals(ChatColor.RESET)) ? "" : glowColor.toString();

		try {
			tagPrefix = getInstance().getTeamManager().getOriginalPrefix(tabPlayer);
		} catch (Exception ex) {
			tagPrefix = color;
		}

		try {
			if (!MainConfig.SETTINGS_SMART_TAB_NAMETAG_HANDLER.getBoolean()) {
				getInstance().getTeamManager().setPrefix(tabPlayer, tagPrefix + color);
			} else {
				Property propertyCustomTagName = tabPlayer.getProperty(TabConstants.Property.CUSTOMTAGNAME);

				if (propertyCustomTagName == null) {
					getInstance().getTeamManager().setPrefix(tabPlayer, tagPrefix + color);
				} else {
					String originalTagName = propertyCustomTagName.getOriginalRawValue();

					if (!propertyCustomTagName.getCurrentRawValue().equals(tagPrefix + originalTagName))
						propertyCustomTagName.setTemporaryValue(tagPrefix + originalTagName);

					getInstance().getTeamManager().setPrefix(tabPlayer, color);
				}
			}
		} catch (IllegalStateException | NullPointerException ignored) {
		}
	}

	public TabPlayer getTABPlayer(UUID uuid) {
		return getInstance().getPlayer(uuid);
	}

	public boolean blockEGlowPackets() {
		return (isSettingNametagPrefixSuffixEnabled() && isSettingTeamPacketBlockingEnabled());
	}

	public String getOutdatedVersionMessage() {
		return "&cWarning&f! &cThis version of eGlow requires a higher TAB version&f!";
	}
}