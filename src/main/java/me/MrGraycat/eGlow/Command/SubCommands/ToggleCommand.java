package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class ToggleCommand extends SubCommand {

    @Override
    public String getName() {
        return "toggle";
    }

    @Override
    public String getDescription() {
        return "Toggle your glow on/off";
    }

    @Override
    public String getPermission() {
        return "eglow.command.toggle";
    }

    @Override
    public String[] getSyntax() {
        return new String[]{"/eGlow toggle"};
    }

    @Override
    public boolean isPlayerCmd() {
        return true;
    }

    @Override
    public void perform(CommandSender sender, EGlowPlayer ePlayer, String[] args) {
        if (ePlayer.isInBlockedWorld()) {
            ChatUtil.sendMsg(sender, Message.WORLD_BLOCKED.get(), true);
            return;
        }

        if (ePlayer.isGlowing()) {
            ePlayer.disableGlow(false);
            ChatUtil.sendMsg(sender, Message.DISABLE_GLOW.get(), true);
        } else {
            if (ePlayer.getEffect() == null || ePlayer.getEffect().getName().equals("none")) {
                ChatUtil.sendMsg(sender, Message.NO_LAST_GLOW.get(), true);
            } else {
                if (ePlayer.isInvisible()) {
                    ChatUtil.sendMsg(sender, Message.INVISIBILITY_BLOCKED.get(), true);
                    return;
                }

                if (ePlayer.getPlayer().hasPermission(ePlayer.getEffect().getPermission()) || ePlayer.isForcedGlow(ePlayer.getEffect())) {
                    ePlayer.activateGlow();
                } else {
                    ChatUtil.sendMsg(sender, Message.NO_PERMISSION.get(), true);
                    return;
                }
                ChatUtil.sendMsg(sender, Message.NEW_GLOW.get(ePlayer.getLastGlowName()), true);
            }
        }
    }
}