package me.mrgraycat.eglow.addon;

import me.clip.placeholderapi.PlaceholderAPI;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.util.DebugUtil;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.text.ChatUtil;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultAddon {
    private Chat chat;

    /**
     * Get vault's chat & check if PlaceholderAPI is installed for placeholder support
     */
    public VaultAddon() {
        RegisteredServiceProvider<Chat> rsp = EGlow.getInstance().getServer().getServicesManager().getRegistration(Chat.class);

        if (rsp != null)
            chat = rsp.getProvider();
    }

    /**
     * Get the players prefix following eGlow's Layout settings
     *
     * @param player IEGlowEntity to get the prefix from
     * @return Formatted prefix as String
     */
    public String getPlayerTagPrefix(EGlowPlayer player) {
        if (!MainConfig.FORMATTING_TAGNAME_ENABLE.getBoolean())
            return "";

        Player p = player.getPlayer();
        String prefix = MainConfig.FORMATTING_TAGNAME_PREFIX.getString();

        if (prefix.contains("%prefix%"))
            prefix = prefix.replace("%prefix%", getPlayerPrefix(player));

        if (DebugUtil.isPAPIInstalled())
            prefix = PlaceholderAPI.setPlaceholders(p, prefix);

        if (prefix.length() > 14 && ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12)
            prefix = prefix.substring(0, 14);

        return (!prefix.isEmpty()) ? ChatUtil.translateColors(prefix) : prefix;
    }

    /**
     * Get the players suffix following eGlow's Layout settings
     *
     * @param player IEGlowEntity to get the suffix from
     * @return Formatted suffix as String
     */
    public String getPlayerTagSuffix(EGlowPlayer player) {
        if (!MainConfig.FORMATTING_TAGNAME_ENABLE.getBoolean())
            return "";

        Player p = player.getPlayer();
        String suffix = MainConfig.FORMATTING_TAGNAME_SUFFIX.getString();

        if (suffix.contains("%suffix%"))
            suffix = suffix.replace("%suffix%", getPlayerSuffix(player));

        if (DebugUtil.isPAPIInstalled())
            suffix = PlaceholderAPI.setPlaceholders(p, suffix);

        return (!suffix.isEmpty()) ? ChatUtil.translateColors(suffix) : "";
    }

    /**
     * Get the players prefix from Vault
     *
     * @param player IEGlowEntity to get the prefix from
     * @return Vault prefix + glow color (cut to 16 chars if needed)
     */
    public String getPlayerPrefix(EGlowPlayer player) {
        if (EGlow.getInstance().getVaultAddon() == null || chat == null)
            return "";

        Player p = player.getPlayer();
        String prefix = chat.getPlayerPrefix(p);

        if (prefix != null && !prefix.equals(""))
            return (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && prefix.length() > 14) ? ((player.getActiveColor().equals(ChatColor.RESET)) ? (prefix.length() > 16) ? prefix.substring(0, 16) : prefix : prefix.substring(0, 14) + player.getActiveColor()) : prefix;
        return "";
    }

    /**
     * Get the players suffix from Vault
     *
     * @param player IEGlowEntity to get the suffix from
     * @return Vault suffix + glow color (cut to 16 chars if needed)
     */
    public String getPlayerSuffix(EGlowPlayer player) {
        if (EGlow.getInstance().getVaultAddon() == null || chat == null)
            return "";

        Player p = player.getPlayer();
        String suffix = chat.getPlayerSuffix(p);

        if (suffix != null && !suffix.equals(""))
            return (ProtocolVersion.SERVER_VERSION.getMinorVersion() <= 12 && suffix.length() > 16) ? suffix.substring(0, 16) : suffix;
        return "";
    }
}