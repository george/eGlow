package me.mrgraycat.eglow.command.subcommands;

import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.util.EnumUtil.GlowVisibility;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowEffect;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.command.CommandSender;

public class EffectCommand extends SubCommand {
    @Override
    public String getName() {
        return "effect";
    }

    @Override
    public String getDescription() {
        return "Activate a glow effect";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String[] getSyntax() {
        return new String[]{"/eGlow <color>", "/eGlow blink <color> <speed>", "/eGlow <effect> <speed>"};
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

        if (ePlayer.isInvisible()) {
            ChatUtil.sendMsg(sender, Message.INVISIBILITY_BLOCKED.get(), true);
            return;
        }

        EGlowEffect effect = null;

        switch (args.length) {
            case (1):
                effect = DataManager.getEGlowEffect(args[0].replace("off", "none").replace("disable", "none"));

                if (effect == null && ePlayer.getEffect() != null) {
                    EGlowEffect effectNew = null;

                    if (ePlayer.getEffect().getName().contains(args[0].toLowerCase())) {
                        effectNew = switchEffectSpeed(ePlayer.getEffect().getName());
                    } else if (DataManager.getEGlowEffect(args[0].toLowerCase() + ePlayer.getEffect().getName() + "slow") != null) {
                        effectNew = DataManager.getEGlowEffect(args[0].toLowerCase() + ePlayer.getEffect().getName() + "slow");
                    }

                    if (effectNew != null) {
                        if (!sender.hasPermission(effectNew.getPermission())) {
                            ChatUtil.sendMsg(sender, Message.NO_PERMISSION.get(), true);
                            return;
                        }

                        ePlayer.disableGlow(true);
                        ePlayer.activateGlow(effectNew);
                        ChatUtil.sendMsg(sender, Message.NEW_GLOW.get(effectNew.getDisplayName()), true);
                        return;
                    }
                }
                break;
            case (2):
                effect = DataManager.getEGlowEffect(args[0] + args[1]);

                if (effect == null && ePlayer.getEffect() != null && ePlayer.getEffect().getName().contains(args[0].toLowerCase() + args[1].toLowerCase())) {
                    EGlowEffect effectNew = switchEffectSpeed(ePlayer.getEffect().getName());

                    if (effectNew != null) {
                        if (!sender.hasPermission(effectNew.getPermission())) {
                            ChatUtil.sendMsg(sender, Message.NO_PERMISSION.get(), true);
                            return;
                        }

                        ePlayer.disableGlow(true);
                        ePlayer.activateGlow(effectNew);
                        ChatUtil.sendMsg(sender, Message.NEW_GLOW.get(effectNew.getDisplayName()), true);
                        return;
                    }
                }
                break;
            case (3):
                effect = DataManager.getEGlowEffect(args[0] + args[1] + args[2]);
                break;
        }

        if (effect == null) {
            sendSyntax(sender, "", true);
            sendSyntax(sender, getSyntax()[0], false);
            sendSyntax(sender, getSyntax()[1], false);
            sendSyntax(sender, getSyntax()[2], false);
            return;
        }

        if (ePlayer.getPlayer().hasPermission(effect.getPermission()) || DataManager.isCustomEffect(effect.getName()) && ePlayer.getPlayer().hasPermission("eglow.effect.*")) {
            if (effect.getName().equals("none")) {
                if (ePlayer.getGlowStatus() || ePlayer.getFakeGlowStatus()) {
                    ePlayer.disableGlow(false);
                }
                ChatUtil.sendMsg(sender, Message.DISABLE_GLOW.get(), true);
                return;
            }

            if (!ePlayer.isSameGlow(effect)) {
                ePlayer.disableGlow(true);
                ePlayer.activateGlow(effect);
                ChatUtil.sendMsg(sender, Message.NEW_GLOW.get(effect.getDisplayName()), true);

                if (ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
                    ChatUtil.sendMsg(sender, Message.UNSUPPORTED_GLOW.get(), true);
                return;
            }

            ChatUtil.sendMsg(sender, Message.SAME_GLOW.get(), true);
            return;
        }
        ChatUtil.sendMsg(sender, Message.NO_PERMISSION.get(), true);
    }

    private EGlowEffect switchEffectSpeed(String effectName) {
        if (effectName.contains("slow")) {
            return DataManager.getEGlowEffect(effectName.replace("slow", "fast"));
        } else if (effectName.contains("fast")) {
            return DataManager.getEGlowEffect(effectName.replace("fast", "slow"));
        } else {
            return null;
        }
    }
}