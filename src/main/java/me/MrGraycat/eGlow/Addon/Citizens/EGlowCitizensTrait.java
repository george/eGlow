package me.mrgraycat.eglow.addon.citizens;

import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class EGlowCitizensTrait extends Trait {
    EGlowPlayer eGlowNPC = null;

    @Persist("LastEffect")
    String lastEffect = "none";

    public EGlowCitizensTrait() {
        super("eGlow");
    }

    @Override
    public void load(DataKey key) {
        setLastEffect(key.getString("LastEffect", "none"));
    }

    @Override
    public void save(DataKey key) {
        if (getEGlowNPC() != null) {
            key.setString("LastEffect", (getEGlowNPC().isGlowing()) ? getEGlowNPC().getEffect().getName() : "none");
        }
    }

    @Override
    public void onSpawn() {
        if (getEGlowNPC() == null) {
            setEGlowNPC(new EGlowPlayer(npc));
        }

        getEGlowNPC().disableGlow(true);
        getEGlowNPC().setDataFromLastGlow(getLastEffect());

        try {
            if (!npc.getOrAddTrait(EGlowCitizensTrait.class).getLastEffect().equals("none"))
                getEGlowNPC().activateGlow();
        } catch (NoSuchMethodError e) {
            ChatUtil.sendToConsole("&cYour Citizens version is outdated please update it", true);
        }
    }

    @Override
    public void onDespawn() {
    }

    @Override
    public void onRemove() {
    }

    private void setEGlowNPC(EGlowPlayer entity) {
        this.eGlowNPC = entity;
    }

    public EGlowPlayer getEGlowNPC() {
        return this.eGlowNPC;
    }

    private void setLastEffect(String lastEffect) {
        this.lastEffect = lastEffect;
    }

    private String getLastEffect() {
        return this.lastEffect;
    }
}