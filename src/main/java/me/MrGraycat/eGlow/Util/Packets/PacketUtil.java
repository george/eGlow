package me.mrgraycat.eglow.util.packets;

import com.google.common.collect.Sets;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.util.DebugUtil;
import me.mrgraycat.eglow.util.EnumUtil.GlowTargetMode;
import me.mrgraycat.eglow.util.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.packets.chat.EnumChatFormat;
import me.mrgraycat.eglow.util.packets.chat.IChatBaseComponent;
import me.mrgraycat.eglow.util.packets.outgoing.PacketPlayOutActionBar;
import me.mrgraycat.eglow.util.packets.outgoing.PacketPlayOutChat;
import me.mrgraycat.eglow.util.packets.outgoing.PacketPlayOutEntityMetadata;
import me.mrgraycat.eglow.util.packets.outgoing.PacketPlayOutScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PacketUtil {
	private static boolean sendPackets = true;

	public static void updatePlayer(EGlowPlayer ePlayer) {
		ProtocolVersion pVersion = ePlayer.getVersion();

		for (EGlowPlayer ep : DataManager.getEGlowPlayers()) {
			if (!(ep.getEntity() instanceof Player) || ep.equals(ePlayer))
				continue;

			if (sendPackets && MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
				if (EGlow.getEGlowInstance().getTABAddon() == null || !EGlow.getEGlowInstance().getTABAddon().blockEGlowPackets() || !DebugUtil.isTABBridgeInstalled()) {
					try {
						NMSHook.sendPacket(ePlayer, new PacketPlayOutScoreboardTeam(ep.getTeamName(), ((EGlow.getEGlowInstance().getVaultAddon() != null) ? EGlow.getEGlowInstance().getVaultAddon().getPlayerTagPrefix(ep) : "") + ep.getActiveColor(), (EGlow.getEGlowInstance().getVaultAddon() != null) ? EGlow.getEGlowInstance().getVaultAddon().getPlayerTagSuffix(ep) : "", (MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), Sets.newHashSet(ep.getDisplayName()), 21).setColor(EnumChatFormat.valueOf(ep.getActiveColor().name())).toNMS(pVersion));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (!ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT)) {
				if (!ep.getGlowStatus() && !ep.getFakeGlowStatus())
					return;

				Object glowingEntity = ep.getEntity();
				int glowingEntityID = ep.getPlayer().getEntityId();
				PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

				try {
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, true));
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				if (ep.getGlowTargetMode().equals(GlowTargetMode.ALL) || ep.getGlowTargetMode().equals(GlowTargetMode.CUSTOM) && ep.getGlowTargets().contains(ePlayer.getPlayer())) {
					if (ePlayer.getGlowVisibility().equals(GlowVisibility.ALL)) {
						try {
							NMSHook.sendPacket(ePlayer, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ePlayer.getVersion()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public static synchronized void scoreboardPacket(EGlowPlayer to, boolean join) {
		try {
			if (sendPackets && MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
				if (to == null || EGlow.getEGlowInstance() == null)
					return;

				if (to.getVersion().getMinorVersion() >= 8) {
					if (EGlow.getEGlowInstance().getTABAddon() == null || !EGlow.getEGlowInstance().getTABAddon().blockEGlowPackets() || !DebugUtil.isTABBridgeInstalled()) {
						for (EGlowPlayer players : DataManager.getEGlowPlayers()) {
							NMSHook.sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName()).toNMS(to.getVersion()));
						}
					}
				}

				if (join) {
					if (EGlow.getEGlowInstance().getTABAddon() == null || !EGlow.getEGlowInstance().getTABAddon().blockEGlowPackets() || !DebugUtil.isTABBridgeInstalled()) {
						for (EGlowPlayer players : DataManager.getEGlowPlayers()) {
							NMSHook.sendPacket(players, new PacketPlayOutScoreboardTeam(to.getTeamName(), (EGlow.getEGlowInstance().getVaultAddon() != null) ? EGlow.getEGlowInstance().getVaultAddon().getPlayerTagPrefix(to) : "", (EGlow.getEGlowInstance().getVaultAddon() != null) ? EGlow.getEGlowInstance().getVaultAddon().getPlayerTagSuffix(to) : "", (MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), Sets.newHashSet(to.getDisplayName()), 21).setColor(EnumChatFormat.RESET).toNMS(to.getVersion()));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateScoreboardTeam(EGlowPlayer entity, String teamName, String prefix, String suffix, EnumChatFormat color) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, (MainConfig.ADVANCED_TEAMS_NAMETAG_VISIBILITY.getBoolean() ? "always" : "never"), (MainConfig.ADVANCED_TEAMS_ENTITY_COLLISION.getBoolean() ? "always" : "never"), 21).setColor(color);

		if (sendPackets && MainConfig.ADVANCED_TEAMS_SEND_PACKETS.getBoolean()) {
			if (EGlow.getEGlowInstance() == null)
				return;

			if (EGlow.getEGlowInstance().getTABAddon() != null && EGlow.getEGlowInstance().getTABAddon().blockEGlowPackets() || DebugUtil.isTABBridgeInstalled())
				return;

			if (entity == null)
				return;

			for (EGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
				try {
					NMSHook.sendPacket(ePlayer.getPlayer(), packet.toNMS(ePlayer.getVersion()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void updateGlowing(EGlowPlayer entity, boolean status) {
		if (entity == null || EGlow.getEGlowInstance() == null)
			return;

		Object glowingEntity = entity.getEntity();
		int glowingEntityID = entity.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		try {
			packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, status));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (status) {
			if (!PipelineInjector.glowingEntities.containsKey(glowingEntityID))
				PipelineInjector.glowingEntities.put(glowingEntityID, entity);

			for (Player p : (entity.getGlowTargetMode().equals(GlowTargetMode.ALL)) ? Bukkit.getOnlinePlayers() : entity.getGlowTargets()) {
				EGlowPlayer ep = DataManager.getEGlowPlayer(p);

				if (ep == null)
					continue;

				switch (entity.getGlowVisibility()) {
					case ALL:
						try {
							NMSHook.sendPacket(p, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ep.getVersion()));
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case OWN:
						if (entity.getPlayer().equals(p))
							try {
								NMSHook.sendPacket(p, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ep.getVersion()));
							} catch (Exception e) {
								e.printStackTrace();
							}
						break;
					default:
						break;
				}
			}
		} else {
			if (PipelineInjector.glowingEntities.containsKey(glowingEntityID))
				PipelineInjector.glowingEntities.remove(glowingEntityID, entity);

			for (Player player : Bukkit.getOnlinePlayers()) {
				EGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(player);
				if (eglowPlayer != null)
					try {
						NMSHook.sendPacket(player, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(eglowPlayer.getVersion()));
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}
	}

	//check glow visibility of main then continue
	public static void glowTargetChange(EGlowPlayer main, Player change, boolean type) {
		EGlowPlayer target = DataManager.getEGlowPlayer(change);

		if (target == null)
			return;

		Object glowingEntity = main.getEntity();
		int glowingEntityID = main.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		if (type && !(main.getGlowStatus() || main.getFakeGlowStatus()))
			type = false;

		switch (target.getGlowVisibility()) {
			case ALL:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, type));
				break;
			case OWN:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, (change.equals(main.getPlayer()) && type)));
				break;
			case NONE:
				packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, false));
				break;
			case UNSUPPORTEDCLIENT:
				break;
		}

		try {
			NMSHook.sendPacket(target, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(target.getVersion()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateGlowTarget(EGlowPlayer ePlayer) {
		Collection<EGlowPlayer> players = DataManager.getEGlowPlayers();
		List<Player> customTargets = ePlayer.getGlowTargets();

		Object glowingEntity = ePlayer.getEntity();
		int glowingEntityID = ePlayer.getPlayer().getEntityId();
		PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

		try {
			packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(glowingEntityID, NMSHook.setGlowFlag(glowingEntity, ePlayer.getGlowTargetMode().equals(GlowTargetMode.ALL) && ePlayer.getGlowStatus()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		switch (ePlayer.getGlowTargetMode()) {
			case ALL:
				for (EGlowPlayer ep : players) {
					if (ep.getGlowVisibility().equals(GlowVisibility.ALL) || ep.equals(ePlayer) && ep.getGlowVisibility().equals(GlowVisibility.OWN)) {
						try {
							NMSHook.sendPacket(ep, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ep.getVersion()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
			//TODO keep in mind glow visibility
			case CUSTOM:
				for (EGlowPlayer ep : players) {
					if (!customTargets.contains(ep.getPlayer())) {
						try {
							NMSHook.sendPacket(ep, Objects.requireNonNull(packetPlayOutEntityMetadata).toNMS(ep.getVersion()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
		}
	}

	public static void forceUpdateGlow(EGlowPlayer ePlayer) {
		for (Player p : (ePlayer.getGlowTargetMode().equals(GlowTargetMode.ALL)) ? Bukkit.getOnlinePlayers() : ePlayer.getGlowTargets()) {
			EGlowPlayer ep = DataManager.getEGlowPlayer(p);
			boolean isGlowing = ep.getGlowStatus() || ep.getFakeGlowStatus();
			PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = null;

			switch (ePlayer.getGlowVisibility()) {
				case ALL:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, isGlowing));
					break;
				case OWN:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, (p.equals(ePlayer.getPlayer()) && isGlowing)));
					break;
				case NONE:
					packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(p.getEntityId(), NMSHook.setGlowFlag(p, false));
					break;
				case UNSUPPORTEDCLIENT:
					return;
			}

			try {
				NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutEntityMetadata.toNMS(ePlayer.getVersion()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void sendActionbar(EGlowPlayer ePlayer, String text) {
		if (text.isEmpty())
			return;

		IChatBaseComponent formattedText = IChatBaseComponent.optimizedComponent(text);

		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 19 && !ProtocolVersion.SERVER_VERSION.getFriendlyName().equals("1.19")) {
			PacketPlayOutActionBar packetPlayOutActionBar = new PacketPlayOutActionBar(formattedText);

			try {
				NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutActionBar.toNMS(ePlayer.getVersion()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(formattedText, PacketPlayOutChat.ChatMessageType.GAME_INFO);

			try {
				NMSHook.sendPacket(ePlayer.getPlayer(), packetPlayOutChat.toNMS(ePlayer.getVersion()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void setSendTeamPackets(boolean status) {
		sendPackets = status;
	}
}