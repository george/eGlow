package me.mrgraycat.eglow.addon;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.util.EnumUtil;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowEffect;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.entity.Player;

public class PlaceholderAPIAddon extends PlaceholderExpansion {
    /**
     * Register all eGlow placeholders in PlaceholderAPI
     */
    public PlaceholderAPIAddon() {
        register();
    }

    @Override
    public String getAuthor() {
        return EGlow.getInstance().getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return EGlow.getInstance().getDescription().getVersion();
    }

    @Override
    public String getIdentifier() {
        return "eglow";
    }

    @Override
    public String getRequiredPlugin() {
        return "eGlow";
    }

    @Override
    public boolean canRegister() {
        return EGlow.getInstance() != null;
    }

    @Override
    public boolean register() {
        if (!canRegister())
            return false;
        return super.register();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null)
            return "";

        EGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(player);

        if (eglowPlayer == null)
            return "";

        switch (identifier.toLowerCase()) {
            case ("client_version"):
                return eglowPlayer.getVersion().getFriendlyName();
            case ("glowcolor"):
                return (eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? eglowPlayer.getActiveColor() + "" : "";
            case ("colorchar"):
                return (eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? eglowPlayer.getActiveColor().getChar() + "" : "r";
            case ("activeglow"):
                return (!eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? Message.COLOR.get("none") : ChatUtil.getEffectChatName(eglowPlayer);
            case ("activeglow_raw"):
                return (!eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? ChatUtil.setToBasicName(Message.COLOR.get("none")) : ChatUtil.setToBasicName(ChatUtil.getEffectChatName(eglowPlayer));
            case ("lastglow"):
                return (eglowPlayer.getLastGlowName());
            case ("lastglow_raw"):
                return ChatUtil.setToBasicName(eglowPlayer.getLastGlowName());
            case ("glow_speed"):
                return getSpeedFromEffect(eglowPlayer.getEffect(), false);
            case ("glow_speed_raw"):
                return getSpeedFromEffect(eglowPlayer.getEffect(), true);
            case ("glowstatus"):
                return (eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? Message.GUI_YES.get() : Message.GUI_NO.get();
            case ("glowstatus_raw"):
                return (eglowPlayer.getGlowStatus() && !eglowPlayer.getFakeGlowStatus()) ? "true" : "false";
            case ("glowstatus_join"):
                return (eglowPlayer.getGlowOnJoin()) ? Message.GUI_YES.get() : Message.GUI_NO.get();
            case ("glowstatus_join_raw"):
                return (eglowPlayer.getGlowOnJoin()) ? "true" : "false";
            case ("glow_visibility"):
                return (eglowPlayer.getGlowVisibility().equals(EnumUtil.GlowVisibility.UNSUPPORTEDCLIENT)) ? Message.VISIBILITY_UNSUPPORTED.get() : Message.valueOf("VISIBILITY_" + eglowPlayer.getGlowVisibility().toString()).get();
            default:
                boolean raw = identifier.toLowerCase().endsWith("_raw");
                if (identifier.toLowerCase().contains("has_permission_")) {
                    EGlowEffect effect = DataManager.getEGlowEffect(identifier.toLowerCase().replace("has_permission_", "").replace("_raw", ""));
                    if (effect != null) {
                        if (player.hasPermission(effect.getPermission())) {
                            return (raw) ? "true" : Message.GUI_YES.get();
                        } else {
                            return (raw) ? "false" : Message.GUI_NO.get();
                        }
                    } else {
                        return "Invalid effect";
                    }
                }
        }
        return null;
    }

    private String getSpeedFromEffect(EGlowEffect effect, boolean raw) {
        if (effect == null) {
            return (raw) ? "none" : Message.COLOR.get("none");
        }

        String effectName = effect.getName();

        if (effectName.contains("slow")) {
            return (raw) ? "slow" : Message.COLOR.get("slow");
        }

        if (effectName.contains("fast")) {
            return (raw) ? "fast" : Message.COLOR.get("fast");
        }

        return (raw) ? "none" : Message.COLOR.get("none");
    }
}