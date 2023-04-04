package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.gui.menus.EGlowMainMenu;
import me.mrgraycat.eglow.util.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class GUICommand extends SubCommand {

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public String getDescription() {
        return "Opens GUI.";
    }

    @Override
    public String getPermission() {
        return "eglow.command.gui";
    }

    @Override
    public String[] getSyntax() {
        return new String[]{"/eGlow"};
    }

    @Override
    public boolean isPlayerCmd() {
        return true;
    }

    @Override
    public void perform(CommandSender sender, EGlowPlayer ePlayer, String[] args) {
        if (ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
            ChatUtil.sendPlainMsg(sender, Message.UNSUPPORTED_GLOW.get(), true);

        if (ePlayer.isInBlockedWorld()) {
            ChatUtil.sendMsg(sender, Message.WORLD_BLOCKED.get(), true);
            return;
        }

        if (ePlayer.isInvisible()) {
            ChatUtil.sendMsg(sender, Message.INVISIBILITY_BLOCKED.get(), true);
            return;
        }

        new EGlowMainMenu(ePlayer.getPlayer()).openInventory();
    }
}
