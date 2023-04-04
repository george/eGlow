package me.mrgraycat.eglow.util.data;

import me.clip.placeholderapi.PlaceholderAPI;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.util.DebugUtil;
import me.mrgraycat.eglow.util.EnumUtil.GlowDisableReason;
import me.mrgraycat.eglow.util.EnumUtil.GlowTargetMode;
import me.mrgraycat.eglow.util.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.EnumUtil.GlowWorldAction;
import me.mrgraycat.eglow.util.packets.PacketUtil;
import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.packets.chat.EnumChatFormat;
import me.mrgraycat.eglow.util.text.ChatUtil;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.ScoreboardTrait;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EGlowPlayer {
    private final String entityType;

    private NPC citizensNPC;
    private Player player;
    private String name;
    private UUID uuid;
    private ProtocolVersion version = ProtocolVersion.SERVER_VERSION;

    private ChatColor activeColor = ChatColor.RESET;
    private boolean glowStatus = false;
    private boolean fakeGlowStatus = false;

    private EGlowEffect glowEffect;

    private final List<EGlowEffect> forcedEffects = new ArrayList<>();

    private boolean glowOnJoin;
    private boolean activeOnQuit;
    private boolean saveData = false;
    private GlowDisableReason glowDisableReason = GlowDisableReason.NONE;
    private GlowVisibility glowVisibility;

    private GlowTargetMode glowTarget = GlowTargetMode.ALL;
    private List<Player> customTargetList = new ArrayList<>();

    public EGlowPlayer(Player player) {
        this.entityType = "PLAYER";
        this.player = player;
        this.name = player.getName();
        this.uuid = player.getUniqueId();
        this.customTargetList = new ArrayList<>(Collections.singletonList(player));
        this.version = ProtocolVersion.getPlayerVersion(this);

        if (this.version.getNetworkId() <= 110 && !this.version.getFriendlyName().equals("1.9.4")) {
            this.glowVisibility = GlowVisibility.UNSUPPORTEDCLIENT;
        } else {
            this.glowVisibility = GlowVisibility.ALL;
        }

        setupForceGlows();
    }

    public EGlowPlayer(NPC npc) {
        this.entityType = "NPC";
        this.citizensNPC = npc;
    }

    // Glowing stuff
    public void setGlowing(boolean status, boolean fake) {
        if (!fake && status == getGlowStatus())
            return;

        setGlowStatus(status);
        setFakeGlowStatus(fake);

        switch (entityType) {
            case ("PLAYER"):
                PacketUtil.updateGlowing(this, status);
                break;
            case ("NPC"):
                try {
                    citizensNPC.data().setPersistent(NPC.Metadata.GLOWING, status);
                } catch (Exception e) {
                    ChatUtil.sendToConsole("&cYour Citizens version is outdated please use it's latest version", true);
                }
        }
    }

    public void setColor(ChatColor color, boolean status, boolean fake) {
        if (getSaveData())
            setSaveData(true);

        setFakeGlowStatus(fake);

        if (color.equals(ChatColor.RESET)) {
            setGlowing(false, fake);
        } else {
            setGlowing(status, fake);

            if (getActiveColor() != null && getActiveColor().equals(color))
                return;

            setGlowStatus(status);
            setActiveColor(color);

            switch (getEntityType()) {
                case ("PLAYER"):
                    PacketUtil.updateScoreboardTeam(DataManager.getEGlowPlayer(getPlayer()), getTeamName(), ((EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(this) : "") + color, (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(this) : "", EnumChatFormat.valueOf(color.name()));
                    break;
                case ("NPC"):
                    if (!fake && citizensNPC.isSpawned())
                        citizensNPC.getOrAddTrait(ScoreboardTrait.class).setColor(color);
                    break;
            }
        }

        if (getEntityType().equals("NPC"))
            return;

        updatePlayerTabname();
        DataManager.sendAPIEvent(this, fake);
    }

    public boolean isSameGlow(EGlowEffect newGlowEffect) {
        return getGlowStatus() && getEffect() != null && newGlowEffect.equals(getEffect());
    }

    public void activateGlow() {
        setGlowStatus(true);

        if (getEffect() != null) {
            activateGlow(getEffect());
        } else {
            setGlowStatus(false);
        }
    }

    public void activateGlow(EGlowEffect newGlow) {
        disableGlow(true);
        setEffect(newGlow);

        newGlow.activateForEntity(getEntity());
        setGlowing(true, false);
    }

    public void disableGlow(boolean hardReset) {
        if (getFakeGlowStatus() || getGlowStatus()) {
            if (getEffect() != null) {
                getEffect().deactivateForEntity(getEntity());
            }

            if (hardReset)
                setEffect(DataManager.getEGlowEffect("none"));

            setActiveColor(ChatColor.RESET);
            setGlowing(false, false);

            if (getPlayer() != null) {
                PacketUtil.updateScoreboardTeam(DataManager.getEGlowPlayer(getPlayer()), getTeamName(), (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagPrefix(this) : "", (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerTagSuffix(this) : "", EnumChatFormat.RESET);
                DataManager.sendAPIEvent(this, false);
                updatePlayerTabname();
            }

            if (this.citizensNPC != null)
                citizensNPC.getOrAddTrait(ScoreboardTrait.class).setColor(ChatColor.RESET);
        }
    }

    /**
     * Update the tabname of the player
     */
    public void updatePlayerTabname() {
        if (!MainConfig.FORMATTING_TABLIST_ENABLE.getBoolean())
            return;

        if (getPlayer() == null)
            return;

        String format = MainConfig.FORMATTING_TABLIST_FORMAT.getString();
        String prefix = (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerPrefix(this) : "";
        String suffix = (EGlow.getInstance().getVaultAddon() != null) ? EGlow.getInstance().getVaultAddon().getPlayerSuffix(this) : "";

        if (format.contains("%name%"))
            format = format.replace("%name%", player.getDisplayName());

        if (format.contains("%prefix%") || format.contains("%suffix%"))
            format = format.replace("%prefix%", prefix).replace("%suffix%", suffix);

        if (DebugUtil.isPAPIInstalled())
            format = PlaceholderAPI.setPlaceholders(getPlayer(), format);

        getPlayer().setPlayerListName(ChatUtil.translateColors(format));
    }

    public boolean isGlowing() {
        return (getGlowStatus() || getFakeGlowStatus());
    }

    public String getTeamName() {
        String playerName = name;
        return (playerName.length() > 15) ? "E" + playerName.substring(0, 14) : "E" + playerName;
    }

    public String getEntityType() {
        return entityType;
    }

    public Object getEntity() {
        if (player != null)
            return player;
        return citizensNPC;
    }

    public String getDisplayName() {
        return this.name;
    }

    public Player getPlayer() {
        return this.player;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public ProtocolVersion getVersion() {
        return this.version;
    }

    public void setupForceGlows() {
        if (!MainConfig.SETTINGS_JOIN_FORCE_GLOWS_ENABLE.getBoolean() || getPlayer() == null || isInBlockedWorld() && MainConfig.SETTINGS_JOIN_FORCE_GLOWS_BYPASS_BLOCKED_WORLDS.getBoolean())
            return;

        for (String permission : MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getConfigSection()) {
            if (getPlayer().hasPermission("eglow.force." + permission.toLowerCase())) {
                EGlowEffect effect = DataManager.getEGlowEffect(MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getString(permission));

                if (!forcedEffects.contains(effect))
                    forcedEffects.add(effect);
            }
        }
    }

    public boolean isForcedGlow(EGlowEffect effect) {
        return forcedEffects.contains(effect);
    }

    public EGlowEffect getForceGlow() {
        if (forcedEffects.isEmpty() || !MainConfig.SETTINGS_JOIN_FORCE_GLOWS_ENABLE.getBoolean() || getPlayer() == null || isInBlockedWorld() && MainConfig.SETTINGS_JOIN_FORCE_GLOWS_BYPASS_BLOCKED_WORLDS.getBoolean())
            return null;
        return forcedEffects.get(0);
    }

    public boolean isInBlockedWorld() {
        if (!MainConfig.WORLD_ENABLE.getBoolean())
            return false;

        GlowWorldAction action;

        try {
            action = GlowWorldAction.valueOf(MainConfig.WORLD_ACTION.getString().toUpperCase() + "ED");
        } catch (IllegalArgumentException e) {
            action = GlowWorldAction.UNKNOWN;
        }

        List<String> worldList = MainConfig.WORLD_LIST.getStringList();

        switch (action) {
            case BLOCKED:
                if (worldList.contains(getPlayer().getWorld().getName().toLowerCase()))
                    return true;
                break;
            case ALLOWED:
                if (!worldList.contains(getPlayer().getWorld().getName().toLowerCase()))
                    return true;
                break;
            case UNKNOWN:
                return false;
        }
        return false;
    }

    public boolean isInvisible() {
        if (!MainConfig.SETTINGS_DISABLE_GLOW_WHEN_INVISIBLE.getBoolean())
            return false;
        return getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    public ChatColor getActiveColor() {
        return this.activeColor;
    }

    public void setActiveColor(ChatColor color) {
        this.activeColor = color;
    }

    public boolean getGlowStatus() {
        return this.glowStatus;
    }

    public void setGlowStatus(boolean status) {
        this.glowStatus = status;
    }

    public boolean getFakeGlowStatus() {
        return this.fakeGlowStatus;
    }

    public void setFakeGlowStatus(boolean status) {
        this.fakeGlowStatus = status;
    }

    public GlowDisableReason getGlowDisableReason() {
        return this.glowDisableReason;
    }

    public boolean setGlowDisableReason(GlowDisableReason reason, boolean skip) {
        if (skip) {
            this.glowDisableReason = reason;
            return false;
        }

        switch (reason) {
            case BLOCKEDWORLD:
            case INVISIBLE:
                break;
            case DISGUISE:
                reason = GlowDisableReason.NONE;
                break;
            case NONE:
                if (!this.glowDisableReason.equals(reason)) {
                    if (isInBlockedWorld()) {
                        this.glowDisableReason = GlowDisableReason.BLOCKEDWORLD;
                        return false;
                    }

                    if (isInvisible()) {
                        this.glowDisableReason = GlowDisableReason.INVISIBLE;
                        return false;
                    }
                }
                break;
        }

        this.glowDisableReason = reason;
        return true;
    }

    public GlowVisibility getGlowVisibility() {
        return this.glowVisibility;
    }

    public void setGlowVisibility(GlowVisibility visibility) {
        if (!this.glowVisibility.equals(GlowVisibility.UNSUPPORTEDCLIENT))
            this.glowVisibility = visibility;
    }

    public GlowTargetMode getGlowTargetMode() {
        return glowTarget;
    }

    public void setGlowTargetMode(GlowTargetMode glowTarget) {
        if (glowTarget != this.glowTarget) {
            this.glowTarget = glowTarget;
            PacketUtil.updateGlowTarget(this);
        }
    }

    public List<Player> getGlowTargets() {
        return customTargetList;
    }

    public void addGlowTarget(Player p) {
        if (!customTargetList.contains(p)) {
            customTargetList.add(p);
            PacketUtil.glowTargetChange(this, p, true);
        }
        if (!customTargetList.contains(player))
            customTargetList.add(player);
        if (glowTarget.equals(GlowTargetMode.ALL))
            setGlowTargetMode(GlowTargetMode.CUSTOM);
    }

    public void removeGlowTarget(Player p) {
        if (customTargetList.contains(p))
            PacketUtil.glowTargetChange(this, p, false);
        customTargetList.remove(p);

        if (glowTarget.equals(GlowTargetMode.CUSTOM) && customTargetList.isEmpty())
            setGlowTargetMode(GlowTargetMode.ALL);
    }

    public void setGlowTargets(List<Player> targets) {
        if (targets == null) {
            customTargetList.clear();
            customTargetList.add(player);
        } else {
            if (!targets.contains(player))
                targets.add(player);

            customTargetList = targets;
        }

        if (glowTarget.equals(GlowTargetMode.ALL)) {
            setGlowTargetMode(GlowTargetMode.CUSTOM);
        } else {
            for (Player player : Objects.requireNonNull(targets, "Can't loop over 'null'")) {
                PacketUtil.glowTargetChange(this, player, true);
            }
        }
    }

    public void resetGlowTargets() {
        customTargetList.clear();
        setGlowTargetMode(GlowTargetMode.ALL);
    }

    public EGlowEffect getEffect() {
        return this.glowEffect;
    }

    public void setEffect(EGlowEffect effect) {
        this.glowEffect = effect;
    }

    public boolean getGlowOnJoin() {
        return this.glowOnJoin;
    }

    public void setGlowOnJoin(boolean status) {
        if (this.glowOnJoin != status) {
            if (getSaveData())
                setSaveData(true);
        }

        this.glowOnJoin = status;
    }

    public boolean getActiveOnQuit() {
        return this.activeOnQuit;
    }

    public void setActiveOnQuit(boolean status) {
        this.activeOnQuit = status;
    }

    public boolean getSaveData() {
        return !this.saveData;
    }

    public void setSaveData(boolean saveData) {
        this.saveData = saveData;
    }

    public void setDataFromLastGlow(String lastGlow) {
        EGlowEffect effect = DataManager.getEGlowEffect(lastGlow);
        setEffect(effect);
    }

    public String getLastGlowName() {
        return (getEffect() != null) ? getEffect().getDisplayName() : Message.COLOR.get("none");
    }

    public String getLastGlow() {
        return (getEffect() != null) ? getEffect().getName() : "none";
    }
}