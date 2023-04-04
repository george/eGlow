package me.mrgraycat.eglow.addon;

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

public class LuckPermsAddon implements Listener {

    private EventSubscription<UserDataRecalculateEvent> luckPermsSub;
    private EventSubscription<GroupDataRecalculateEvent> luckPermsSub2;

    public LuckPermsAddon() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

        if (provider == null)
            return;

        EventBus LP_EventBus = provider.getProvider().getEventBus();
        TABAddon TAB_Addon = EGlow.getInstance().getTABAddon();
        VaultAddon Vault_Addon = EGlow.getInstance().getVaultAddon();

        luckPermsSub = LP_EventBus.subscribe(UserDataRecalculateEvent.class, event -> {
            try {
                if (EGlow.getInstance() == null)
                    return;

                if (event.getUser().getUsername() == null)
                    return;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        EGlowPlayer ePlayer = DataManager.getEGlowPlayer(event.getUser().getUniqueId());

                        if (ePlayer == null)
                            return;

                        if (TAB_Addon != null && TAB_Addon.getTABSupported() && TAB_Addon.blockEGlowPackets()) {
                            TAB_Addon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
                        } else {
                            if (!DebugUtil.isTABBridgeInstalled()) {
                                ePlayer.updatePlayerTabname();
                                PacketUtil.updateScoreboardTeam(ePlayer, ePlayer.getTeamName(), ((Vault_Addon != null) ? Vault_Addon.getPlayerTagPrefix(ePlayer) : "") + ePlayer.getActiveColor(), (Vault_Addon != null) ? Vault_Addon.getPlayerTagSuffix(ePlayer) : "", EnumChatFormat.valueOf(ePlayer.getActiveColor().name()));
                            }
                        }
                    }
                }.runTaskLaterAsynchronously(EGlow.getInstance(), 20);
            } catch (IllegalPluginAccessException e) {
                //Prevent error spam when eGlow is unloading
            }
        });

        luckPermsSub2 = LP_EventBus.subscribe(GroupDataRecalculateEvent.class, event -> {
            try {
                if (EGlow.getInstance() == null)
                    return;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (EGlowPlayer ePlayer : DataManager.getEGlowPlayers()) {
                            if (TAB_Addon != null && TAB_Addon.getTABSupported() && TAB_Addon.blockEGlowPackets()) {
                                TAB_Addon.updateTABPlayer(ePlayer, ePlayer.getActiveColor());
                            } else {
                                if (!DebugUtil.isTABBridgeInstalled()) {
                                    PacketUtil.updateScoreboardTeam(ePlayer, ePlayer.getTeamName(), ((Vault_Addon != null) ? Vault_Addon.getPlayerTagPrefix(ePlayer) : "") + ePlayer.getActiveColor(), (Vault_Addon != null) ? Vault_Addon.getPlayerTagSuffix(ePlayer) : "", EnumChatFormat.valueOf(ePlayer.getActiveColor().name()));

                                }
                            }
                        }
                    }
                }.runTaskLaterAsynchronously(EGlow.getInstance(), 20);
            } catch (IllegalPluginAccessException e) {
                //Prevent error spam when eGlow is unloading
            }
        });
    }

    public void unload() {
        try {
            luckPermsSub.close();
            luckPermsSub2.close();
        } catch (NoClassDefFoundError e) {
            //Rare error when disabling eGlow no clue how this could happen
        }
    }
}