package me.mrgraycat.eglow.command.subcommands.admin;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class UnsetCommand extends SubCommand {
    @Override
    public String getName() {
        return "unset";
    }

    @Override
    public String getDescription() {
        return "Stop the glowing of a player/NPC";
    }

    @Override
    public String getPermission() {
        return "eglow.command.unset";
    }

    @Override
    public String[] getSyntax() {
        return new String[]{"/eGlow unset <player/npc>"};
    }

    @Override
    public boolean isPlayerCmd() {
        return false;
    }

    @Override
    public void perform(CommandSender sender, EGlowPlayer ePlayer, String[] args) {
        List<EGlowPlayer> eTargets = getTarget(sender, args);

        if (eTargets == null) {
            sendSyntax(sender, "", true);
            return;
        }

        for (EGlowPlayer eTarget : eTargets) {
            if (eTarget == null)
                continue;

            if (eTarget.isGlowing()) {
                eTarget.disableGlow(false);

                if (eTarget.getEntityType().equals("PLAYER") && MainConfig.SETTINGS_NOTIFICATIONS_TARGET_COMMAND.getBoolean())
                    ChatUtil.sendMsg(eTarget.getPlayer(), Message.TARGET_NOTIFICATION_PREFIX.get() + Message.DISABLE_GLOW.get(), true);
            }

            ChatUtil.sendMsg(sender, Message.OTHER_CONFIRM_OFF.get(eTarget), true);
        }
    }
}