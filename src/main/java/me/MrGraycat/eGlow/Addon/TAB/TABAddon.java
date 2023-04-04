package me.mrgraycat.eglow.addon.tab;

import me.mrgraycat.eglow.EGlow;
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

public class TABAddon {
    private boolean TAB_Supported = false;
    private boolean TAB_NametagPrefixSuffixEnabled;
    private boolean TAB_TeamPacketBlockingEnabled;

    public TABAddon(Plugin TAB_Plugin) {
        int TAB_Version = (TAB_Plugin != null) ? Integer.parseInt(TAB_Plugin.getDescription().getVersion().replaceAll("[^\\d]", "")) : 0;

        if (TAB_Version < 314) {
            ChatUtil.sendToConsole("&cWarning&f! &cThis version of eGlow requires TAB 3.1.4 or higher!", true);
            return;
        }

        loadTABSettings();

        TabAPI.getInstance().getEventBus().register(TabLoadEvent.class, event -> new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    TABAddon TAB_Addon = EGlow.getInstance().getTABAddon();
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
        }.runTaskAsynchronously(EGlow.getInstance()));

        setTABSupported();
    }

    public void loadTABSettings() {
        ConfigurationFile TAB_Config = TabAPI.getInstance().getConfig();

        setTABNametagPrefixSuffixEnabled(TAB_Config.getBoolean("scoreboard-teams.enabled", false));
        setTABTeamPacketBlockingEnabled(TAB_Config.getBoolean("scoreboard-teams.anti-override", false));

        if (MainConfig.SETTINGS_SMART_TAB_NAMETAG_HANDLER.getBoolean()) {
            if (!TAB_Config.getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
                TAB_Config.set("scoreboard-teams.unlimited-nametag-mode.enabled", true);
                ChatUtil.sendToConsole("&6Enabled unlimited-nametag-mode in TAB&f! &6Please reload TAB or restart the server&f.", true);
            }
        }
    }

    public void updateTABPlayer(EGlowPlayer ePlayer, ChatColor glowColor) {
        TabPlayer tabPlayer = getTABPlayer(ePlayer.getUUID());

        if (tabPlayer == null || TabAPI.getInstance().getTeamManager() == null)
            return;

        String tagPrefix;
        String color = (glowColor.equals(ChatColor.RESET)) ? "" : glowColor + "";

        try {
            tagPrefix = TabAPI.getInstance().getTeamManager().getOriginalPrefix(tabPlayer);
        } catch (Exception ex) {
            tagPrefix = color;
        }

        try {
            if (!MainConfig.SETTINGS_SMART_TAB_NAMETAG_HANDLER.getBoolean()) {
                TabAPI.getInstance().getTeamManager().setPrefix(tabPlayer, tagPrefix + color);
            } else {
                Property propertyCustomTagName = tabPlayer.getProperty(TabConstants.Property.CUSTOMTAGNAME);

                if (propertyCustomTagName == null) {
                    TabAPI.getInstance().getTeamManager().setPrefix(tabPlayer, tagPrefix + color);
                } else {
                    String originalTagName = propertyCustomTagName.getOriginalRawValue();

                    if (!propertyCustomTagName.getCurrentRawValue().equals(tagPrefix + originalTagName))
                        propertyCustomTagName.setTemporaryValue(tagPrefix + originalTagName);

                    TabAPI.getInstance().getTeamManager().setPrefix(tabPlayer, color);
                }
            }
        } catch (IllegalStateException | NullPointerException e) {
            //Wierd NPE on first join ignoring it
        }
    }

    public TabPlayer getTABPlayer(UUID uuid) {
        return TabAPI.getInstance().getPlayer(uuid);
    }

    public boolean blockEGlowPackets() {
        return (getTABNametagPrefixSuffixEnabled() && getTABTeamPacketBlockingEnabled());
    }

    //Getter
    public boolean getTABSupported() {
        return this.TAB_Supported;
    }

    public boolean getTABNametagPrefixSuffixEnabled() {
        return this.TAB_NametagPrefixSuffixEnabled;
    }

    public boolean getTABTeamPacketBlockingEnabled() {
        return this.TAB_TeamPacketBlockingEnabled;
    }

    //Setter
    private void setTABSupported() {
        this.TAB_Supported = true;
    }

    private void setTABNametagPrefixSuffixEnabled(boolean status) {
        this.TAB_NametagPrefixSuffixEnabled = status;
    }

    private void setTABTeamPacketBlockingEnabled(boolean status) {
        this.TAB_TeamPacketBlockingEnabled = status;
    }
}