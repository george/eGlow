package me.mrgraycat.eglow.addon;

import lombok.Getter;
import lombok.Setter;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.addon.tab.TABAddon;
import me.mrgraycat.eglow.util.DebugUtil;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.packets.chat.EnumChatFormat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import static me.mrgraycat.eglow.EGlow.getEGlowInstance;

public class LuckPermsAddon implements Listener {
	@Getter
	@Setter
	private EventSubscription<UserDataRecalculateEvent> luckPermsSub;
	@Getter
	@Setter
	private EventSubscription<GroupDataRecalculateEvent> luckPermsSub2;

	public LuckPermsAddon() {
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

		if (provider == null)
			return;

		EventBus lpEventBus = provider.getProvider().getEventBus();
		TABAddon tabAddon = getEGlowInstance().getTABAddon();
		VaultAddon vaultAddon = getEGlowInstance().getVaultAddon();

		setLuckPermsSub(lpEventBus.subscribe(UserDataRecalculateEvent.class, event -> {
			try {
				if (getEGlowInstance() == null || event.getUser().getUsername() == null)
					return;

				new BukkitRunnable() {
					@Override
					public void run() {
						EGlowPlayer ePlayer = DataManager.getEGlowPlayer(event.getUser().getUniqueId());

						if (ePlayer == null)
							return;

						if (tabAddon != null && tabAddon.isVersionSupported() && tabAddon.blockEGlowPackets()) {
							tabAddon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
						} else {
							if (!DebugUtil.isTABBridgeInstalled()) {
								ePlayer.updatePlayerTabname();
								PacketUtil.updateScoreboardTeam(ePlayer, ePlayer.getTeamName(), ((vaultAddon != null) ? vaultAddon.getPlayerTagPrefix(ePlayer) : "") + ePlayer.getActiveColor(), (vaultAddon != null) ? vaultAddon.getPlayerTagSuffix(ePlayer) : "", EnumChatFormat.valueOf(ePlayer.getActiveColor().name()));
							}
						}
					}
				}.runTaskLaterAsynchronously(EGlow.getEGlowInstance(), 20);
			} catch (IllegalPluginAccessException ignored) {
			}
		}));

		setLuckPermsSub2(lpEventBus.subscribe(GroupDataRecalculateEvent.class, event -> {
			try {
				if (getEGlowInstance() == null)
					return;

				new BukkitRunnable() {
					@Override
					public void run() {
						for (EGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
							if (tabAddon != null && tabAddon.isVersionSupported() && tabAddon.blockEGlowPackets()) {
								tabAddon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
							} else {
								if (!DebugUtil.isTABBridgeInstalled()) {
									PacketUtil.updateScoreboardTeam(ePlayer, ePlayer.getTeamName(), ((vaultAddon != null) ? vaultAddon.getPlayerTagPrefix(ePlayer) : "") + ePlayer.getActiveColor(), (vaultAddon != null) ? vaultAddon.getPlayerTagSuffix(ePlayer) : "", EnumChatFormat.valueOf(ePlayer.getActiveColor().name()));
								}
							}
						}
					}
				}.runTaskLaterAsynchronously(getEGlowInstance(), 20);
			} catch (IllegalPluginAccessException ignored) {
			}
		}));
	}

	public void unload() {
		try {
			getLuckPermsSub().close();
			getLuckPermsSub2().close();
		} catch (NoClassDefFoundError ignored) {
		}
	}
}