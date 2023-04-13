package me.mrgraycat.eglow.api;

import me.mrgraycat.eglow.api.effect.EGlowBlink;
import me.mrgraycat.eglow.api.effect.EGlowColor;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowEffect;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.packets.PipelineInjector;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

import static me.mrgraycat.eglow.EGlow.getEGlowInstance;

public class EGlowAPI {
	/**
	 * Get the IEGlowEntity from eGlow
	 *
	 * @param player player to get the IEGlowPlayer for
	 * @return IEGlowEntity instance for the player
	 */
	public EGlowPlayer getEGlowPlayer(Player player) {
		return DataManager.getEGlowPlayer(player);
	}

	/**
	 * Get the IEGlowEntity from eGlow
	 *
	 * @param uuid uuid to get the IEGlowPlayer for
	 * @return IEGlowEntity instance for the uuid
	 */
	public EGlowPlayer getEGlowPlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null)
			return DataManager.getEGlowPlayer(player);
		return null;
	}

	/**
	 * Get the IEGlowEffect from eGlow
	 *
	 * @param name name for the effect
	 * @return IEGlowEffect is found, null if not
	 */
	public EGlowEffect getEGlowEffect(String name) {
		EGlowEffect effect = DataManager.getEGlowEffect(name);

		if (effect == null)
			ChatUtil.sendToConsole("(API) Unable to find effect for name: " + name, true);
		return effect;
	}

	/**
	 * Get the glow color from a player
	 *
	 * @param player player to get the glow color from
	 * @return Glow color as String (invisible)
	 */
	public String getGlowColor(EGlowPlayer player) {
		if (player == null)
			return "";

		return (player.isGlowing()) ? String.valueOf(player.getActiveColor()) : "";
	}

	/**
	 * Enable a specific effect for a player
	 *
	 * @param player to activate the effect for
	 * @param effect to enable
	 */
	public void enableGlow(EGlowPlayer player, EGlowEffect effect) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;

				player.activateGlow(effect);
			}
		}.runTaskLaterAsynchronously(getEGlowInstance(), 1);
	}

	/**
	 * Enable a solid glow color for a player
	 *
	 * @param player to activate the glow for
	 * @param color  to enable
	 */
	public void enableGlow(EGlowPlayer player, EGlowColor color) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;

				EGlowEffect effect = DataManager.getEGlowEffect(color.toString());
				player.activateGlow(effect);
			}
		}.runTaskLaterAsynchronously(getEGlowInstance(), 1);
	}

	/**
	 * Enable a blink effect for a player
	 *
	 * @param player to activate the blink for
	 * @param blink  to enable
	 */
	public void enableGlow(EGlowPlayer player, EGlowBlink blink) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;

				EGlowEffect effect = DataManager.getEGlowEffect(blink.toString());
				player.activateGlow(effect);
			}
		}.runTaskLaterAsynchronously(getEGlowInstance(), 1);
	}

	/**
	 * Enable an effect for a player
	 *
	 * @param player  to activate the effect for
	 * @param effects to enable
	 */
	public void enableGlow(EGlowPlayer player, me.mrgraycat.eglow.api.effect.EGlowEffect effects) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;

				EGlowEffect effect = DataManager.getEGlowEffect(effects.toString());
				player.activateGlow(effect);
			}
		}.runTaskLaterAsynchronously(getEGlowInstance(), 1);
	}

	/**
	 * Disable the glow for a player
	 *
	 * @param player to disable the glow for
	 */
	public void disableGlow(EGlowPlayer player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player == null)
					return;

				player.disableGlow(true);
			}
		}.runTaskLaterAsynchronously(getEGlowInstance(), 1);
	}

	/**
	 * add custom receiver for a player
	 *
	 * @param sender   player to add the custom receiver for
	 * @param receiver player that the sender will be able to see glowing
	 */
	public void addCustomGlowReceiver(EGlowPlayer sender, Player receiver) {
		if (sender == null)
			return;

		sender.addGlowTarget(receiver);

		PacketUtil.forceUpdateGlow(sender);
	}

	/**
	 * remove custom receiver for a player
	 *
	 * @param sender   player to remove the custom receiver for
	 * @param receiver player that the sender will no longer be able to see glowing
	 */
	public void removeCustomGlowReceiver(EGlowPlayer sender, Player receiver) {
		if (sender == null)
			return;

		sender.removeGlowTarget(receiver);
		PacketUtil.forceUpdateGlow(sender);
	}

	/**
	 * set custom receivers for a player
	 *
	 * @param sender    player to set the custom receivers for
	 * @param receivers players that the sender will be able to see glowing
	 */
	public void setCustomGlowReceivers(EGlowPlayer sender, List<Player> receivers) {
		if (sender == null)
			return;

		sender.setGlowTargets(receivers);
		PacketUtil.forceUpdateGlow(sender);
	}

	/**
	 * reset custom receivers for a player
	 *
	 * @param sender player to reset the custom receivers for
	 */
	public void resetCustomGlowReceivers(EGlowPlayer sender) {
		if (sender == null)
			return;

		sender.resetGlowTargets();
		PacketUtil.forceUpdateGlow(sender);
	}

	/**
	 * Enable/Disable eGlow from sending team packets
	 *
	 * @param status true to send packets, false for nothing
	 */
	public void setSendTeamPackets(boolean status) {
		PacketUtil.setSendTeamPackets(status);
	}

	/**
	 * Enable/Disable eGlow from blocking packets that could overwrite the glow color
	 *
	 * @param status true for packet blocking, false for nothing
	 */
	public void setPacketBlockerStatus(boolean status) {
		PipelineInjector.setBlockPackets(status);
	}
}