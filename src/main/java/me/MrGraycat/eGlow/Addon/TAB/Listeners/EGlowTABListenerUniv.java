package me.mrgraycat.eglow.addon.tab.Listeners;

import me.mrgraycat.eglow.addon.tab.TABAddon;
import me.mrgraycat.eglow.api.event.GlowColorChangeEvent;
import me.mrgraycat.eglow.util.DebugUtil;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ConcurrentModificationException;

import static me.mrgraycat.eglow.EGlow.getEGlowInstance;


public class EGlowTABListenerUniv implements Listener {

	@EventHandler
	public void onColorChange(GlowColorChangeEvent event) {
		updateTABPlayer(event.getPlayer());
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		updateTABPlayer(event.getPlayer());
	}

	private void updateTABPlayer(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					TABAddon TAB_Addon = getEGlowInstance().getTABAddon();
					EGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);

					if (ePlayer == null)
						return;

					if (TAB_Addon != null && TAB_Addon.blockEGlowPackets()) {
						if (TAB_Addon.getTABPlayer(player.getUniqueId()) != null) {
							TAB_Addon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
						}
					} else if (DebugUtil.onBungee()) {
						DataManager.TABProxyUpdateRequest(player, (ePlayer.getActiveColor().equals(ChatColor.RESET) || !ePlayer.isGlowing()) ? "" : String.valueOf(ePlayer.getActiveColor()));
					}
				} catch (ConcurrentModificationException ignored) {
					//caused by updating to fast
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}.runTaskLaterAsynchronously(getEGlowInstance(), 2);
	}
}