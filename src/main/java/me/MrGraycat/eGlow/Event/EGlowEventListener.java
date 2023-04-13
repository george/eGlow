package me.mrgraycat.eglow.event;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.config.playerdata.EGlowPlayerdataManager;
import me.mrgraycat.eglow.gui.Menu;
import me.mrgraycat.eglow.util.EnumUtil.GlowDisableReason;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowEffect;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.packets.PipelineInjector;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class EGlowEventListener implements Listener {
	public EGlowEventListener() {
		EGlow.getEGlowInstance().getServer().getPluginManager().registerEvents(this, EGlow.getEGlowInstance());

		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			new EGlowEventListener113AndAbove();
		}
	}

	@EventHandler
	public void PlayerConnectEvent(PlayerJoinEvent e) {
		PlayerConnect(e.getPlayer(), e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void PlayerKickedEvent(PlayerKickEvent e) {
		PlayerDisconnect(e.getPlayer(), false);
	}

	@EventHandler
	public void PlayerDisconnectEvent(PlayerQuitEvent e) {
		PlayerDisconnect(e.getPlayer(), false);
	}

	@EventHandler
	public void onMenuClick(InventoryClickEvent e) {
		InventoryHolder holder = e.getInventory().getHolder();

		if (holder == null)
			return;

		if (holder instanceof Menu) {
			e.setCancelled(true);

			if (e.getView().getBottomInventory().equals(e.getClickedInventory()) || e.getCurrentItem() == null)
				return;

			Menu menu = (Menu) holder;
			menu.handleMenu(e);
		}
	}

	@EventHandler
	public void onPlayerWorldChange(PlayerChangedWorldEvent e) {
		Player p = e.getPlayer();
		EGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(p);

		if (eglowPlayer != null) {
			if (eglowPlayer.isInBlockedWorld()) {
				if (eglowPlayer.isGlowing()) {
					eglowPlayer.disableGlow(false);
					eglowPlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD, false);
					ChatUtil.sendMsg(p, Message.WORLD_BLOCKED.get(), true);
				}
			} else {
				if (eglowPlayer.getGlowDisableReason().equals(GlowDisableReason.BLOCKEDWORLD)) {
					if (eglowPlayer.setGlowDisableReason(GlowDisableReason.NONE, false)) {
						eglowPlayer.activateGlow();
						ChatUtil.sendMsg(p, Message.WORLD_ALLOWED.get(), true);
					}
				}
			}
		}
	}

	/**
	 * Code to initialise the player
	 *
	 * @param p player to initialise
	 */
	public static void PlayerConnect(Player p, UUID uuid) {
		//Fixes permanent player glows from old eGlow versions/other glow plugins that use Player#setGlowing(true)
		if (p.isGlowing())
			p.setGlowing(false);

		EGlowPlayer eglowPlayer = DataManager.addEGlowPlayer(p, uuid.toString());
		PipelineInjector.inject(eglowPlayer);
		PacketUtil.scoreboardPacket(eglowPlayer, true);

		new BukkitRunnable() {
			@Override
			public void run() {
				EGlowPlayerdataManager.loadPlayerdata(eglowPlayer);
				eglowPlayer.setSaveData(false);

				if (!EGlow.getEGlowInstance().isUpToDate() && MainConfig.SETTINGS_NOTIFICATIONS_UPDATE.getBoolean() && p.hasPermission("eglow.option.update"))
					ChatUtil.sendPlainMsg(p, "&aA new update is available&f!", true);

				if (EGlowPlayerdataManager.getMySQL_Failed() && p.hasPermission("eglow.option.update"))
					ChatUtil.sendPlainMsg(p, "&cMySQL failed to enable properly, have a look at this asap&f.", true);

				new BukkitRunnable() {
					@Override
					public void run() {
						PacketUtil.updatePlayer(eglowPlayer);
					}
				}.runTask(EGlow.getEGlowInstance());

				eglowPlayer.updatePlayerTabname();

				EGlowEffect effect = eglowPlayer.getForceGlow();

				if (effect != null) {
					if (eglowPlayer.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY) && !eglowPlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
						eglowPlayer.setGlowDisableReason(GlowDisableReason.INVISIBLE, false);
						ChatUtil.sendMsg(eglowPlayer.getPlayer(), Message.INVISIBILITY_DISABLED.get(), true);
					} else {
						eglowPlayer.activateGlow(effect);
						if (MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && eglowPlayer.getPlayer().hasPermission("eglow.option.glowstate"))
							ChatUtil.sendMsg(eglowPlayer.getPlayer(), Message.GLOWING_STATE_ON_JOIN.get(effect.getDisplayName()), true);
						return;
					}
					if (MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && eglowPlayer.getPlayer().hasPermission("eglow.option.glowstate"))
						ChatUtil.sendMsg(eglowPlayer.getPlayer(), Message.NON_GLOWING_STATE_ON_JOIN.get(), true);
					return;
				}

				if (eglowPlayer.getActiveOnQuit()) {
					if (eglowPlayer.getEffect() == null || !eglowPlayer.getGlowOnJoin() || !p.hasPermission("eglow.option.glowonjoin") || MainConfig.SETTINGS_JOIN_CHECK_PERMISSION.getBoolean() && !p.hasPermission(eglowPlayer.getEffect().getPermission()))
						return;

					if (eglowPlayer.isInBlockedWorld()) {
						if (eglowPlayer.isGlowing()) {
							eglowPlayer.disableGlow(false);
							eglowPlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD, false);
							ChatUtil.sendMsg(p, Message.WORLD_BLOCKED.get(), true);
							return;
						}
					}

					try {
						eglowPlayer.activateGlow();
					} catch (NullPointerException e) {
						//Prevent rare but useless message whenever something causes the player to disconnect whilst joining the server
					}

					if (MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && eglowPlayer.getPlayer().hasPermission("eglow.option.glowstate") && eglowPlayer.getEffect() != null)
						ChatUtil.sendMsg(eglowPlayer.getPlayer(), Message.GLOWING_STATE_ON_JOIN.get(eglowPlayer.getEffect().getDisplayName()), true);
					return;
				}

				if (MainConfig.SETTINGS_JOIN_MENTION_GLOW_STATE.getBoolean() && eglowPlayer.getPlayer().hasPermission("eglow.option.glowstate"))
					ChatUtil.sendMsg(eglowPlayer.getPlayer(), Message.NON_GLOWING_STATE_ON_JOIN.get(), true);
			}
		}.runTaskLaterAsynchronously(EGlow.getEGlowInstance(), 2L);
	}

	/**
	 * Code to unload the player from eGlow
	 *
	 * @param p player to unload
	 */
	public static void PlayerDisconnect(Player p, boolean shutdown) {
		EGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(p);
		PacketUtil.scoreboardPacket(eglowPlayer, false);

		if (!shutdown) {
			new BukkitRunnable() {
				@Override
				public void run() {
					PlayerDisconnectNext(eglowPlayer);
				}
			}.runTaskAsynchronously(EGlow.getEGlowInstance());
		} else {
			PlayerDisconnectNext(eglowPlayer);
		}
	}

	private static void PlayerDisconnectNext(EGlowPlayer eglowPlayer) {
		if (eglowPlayer != null) {
			eglowPlayer.setActiveOnQuit(eglowPlayer.isGlowing());
			EGlowPlayerdataManager.savePlayerdata(eglowPlayer);

			PipelineInjector.uninject(eglowPlayer);
			DataManager.removeEGlowPlayer(eglowPlayer.getPlayer());
			if (EGlow.getEGlowInstance().getAdvancedGlowVisibility() != null) {
				EGlow.getEGlowInstance().getAdvancedGlowVisibility().uncachePlayer(eglowPlayer);
			}
		}
	}
}