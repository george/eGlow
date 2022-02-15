package me.MrGraycat.eGlow.Addon;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Addon.TAB.TABAddon;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Packets.PacketUtil;
import me.MrGraycat.eGlow.Util.Packets.MultiVersion.EnumChatFormat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

public class LuckPermsAddon implements Listener {
	
	public LuckPermsAddon() {
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		
		if (provider == null)
			return;
		
		EventBus LP_EventBus = provider.getProvider().getEventBus();
		TABAddon TAB_Addon = EGlow.getInstance().getTABAddon();
		VaultAddon Vault_Addon = EGlow.getInstance().getVaultAddon();
		
		LP_EventBus.subscribe(UserDataRecalculateEvent.class, event -> {
			if (EGlow.getInstance() == null)
				return;
			
			new BukkitRunnable() {
				public void run() {
					IEGlowPlayer ePlayer = DataManager.getEGlowPlayer(event.getUser().getUsername());
					
					if (ePlayer == null)
						return;
					
					if (TAB_Addon != null && TAB_Addon.getTABSupported() && TAB_Addon.blockEGlowPackets()) {
						TAB_Addon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
					} else {
						if (Vault_Addon != null) {
							Vault_Addon.updatePlayerTabname(ePlayer);
							PacketUtil.updateScoreboardTeam(ePlayer, ePlayer.getTeamName(), Vault_Addon.getPlayerTagPrefix(ePlayer) + ePlayer.getActiveColor(), Vault_Addon.getPlayerTagSuffix(ePlayer), true, true, EnumChatFormat.valueOf(ePlayer.getActiveColor().name()));
						}
					}
				}
			}.runTaskLater(EGlow.getInstance(), 5);
		});
		
		LP_EventBus.subscribe(GroupDataRecalculateEvent.class, event -> {
			if (EGlow.getInstance() == null)
				return;
			
			new BukkitRunnable() {
				public void run() {
					for (IEGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
						if (TAB_Addon != null && TAB_Addon.getTABSupported() && TAB_Addon.blockEGlowPackets()) {
							TAB_Addon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
						} else {
							if (Vault_Addon != null) {
								PacketUtil.updateScoreboardTeam(ePlayer, ePlayer.getTeamName(), Vault_Addon.getPlayerTagPrefix(ePlayer) + ePlayer.getActiveColor(), Vault_Addon.getPlayerTagSuffix(ePlayer), true, true, EnumChatFormat.valueOf(ePlayer.getActiveColor().name()));
							}
						}
					}
				}
			}.runTaskLater(EGlow.getInstance(), 5);
		});
	}
}
