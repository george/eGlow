package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowCustomEffectsConfig.Effect;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class ListCommand extends SubCommand {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Shows a list of all available colors/effects.";
    }

    @Override
    public String getPermission() {
        return "eglow.command.list";
    }

    @Override
    public String[] getSyntax() {
        return new String[]{"/eGlow list"};
    }

    @Override
    public boolean isPlayerCmd() {
        return false;
    }

    @Override
    public void perform(CommandSender sender, EGlowPlayer ePlayer, String[] args) {
        ChatUtil.sendPlainMsg(sender, "&m        &r &fColors & effects for &eeGlow&f: &m          ", false);
        ChatUtil.sendPlainMsg(sender, "&fColors:", false);
        ChatUtil.sendPlainMsg(sender, ChatUtil.getEffectName("red") + ", " + ChatUtil.getEffectName("darkred") + ", " + ChatUtil.getEffectName("gold") + ",", false);
        ChatUtil.sendPlainMsg(sender, ChatUtil.getEffectName("yellow") + ", " + ChatUtil.getEffectName("green") + ", " + ChatUtil.getEffectName("darkgreen") + ",", false);
        ChatUtil.sendPlainMsg(sender, ChatUtil.getEffectName("aqua") + ", " + ChatUtil.getEffectName("darkaqua") + ", " + ChatUtil.getEffectName("blue") + ",", false);
        ChatUtil.sendPlainMsg(sender, ChatUtil.getEffectName("darkblue") + ", " + ChatUtil.getEffectName("purple") + ", " + ChatUtil.getEffectName("pink") + ",", false);
        ChatUtil.sendPlainMsg(sender, ChatUtil.getEffectName("white") + ", " + ChatUtil.getEffectName("gray") + ", " + ChatUtil.getEffectName("darkgray") + ",", false);
        ChatUtil.sendPlainMsg(sender, ChatUtil.getEffectName("black") + ".", false);
        ChatUtil.sendPlainMsg(sender, "&eoff, disable, " + ChatUtil.getEffectName("none") + " &ewill stop the glow.", false);
        ChatUtil.sendPlainMsg(sender, "&fEffects:", false);
        ChatUtil.sendPlainMsg(sender, ChatUtil.getEffectName("rainbowslow") + ", " + ChatUtil.getEffectName("rainbowfast"), false);
        ChatUtil.sendPlainMsg(sender, "&fCustom effects:", false);

        StringBuilder text = new StringBuilder();
        int i = 1;

        for (String effect : Effect.GET_ALL_EFFECTS.get()) {
            text.append(ChatUtil.getEffectName(effect)).append(", ");

            if (i == 3) {
                ChatUtil.sendPlainMsg(sender, text.toString(), false);
                text = new StringBuilder();
                i = 0;
            }
            i++;
        }

        if (text.length() > 0)
            ChatUtil.sendPlainMsg(sender, text.toString(), false);
        ChatUtil.sendPlainMsg(sender, "&f&m                                                       ", false);
    }
}