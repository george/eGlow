package me.mrgraycat.eglow.addon.citizens;

import lombok.Getter;
import lombok.Setter;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class EGlowCitizensTrait extends Trait {
	@Getter
	@Setter
	private EGlowPlayer eGlowNPC;

	@Getter
	@Setter
	@Persist("LastEffect")
	private String lastEffect;

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
			setEGlowNPC(new EGlowPlayer(this.npc));
		}

		getEGlowNPC().disableGlow(true);
		getEGlowNPC().setDataFromLastGlow(getLastEffect());

		try {
			if (!this.npc.getOrAddTrait(EGlowCitizensTrait.class).getLastEffect().equals("none"))
				getEGlowNPC().activateGlow();
		} catch (NoSuchMethodError e) {
			ChatUtil.sendToConsole("&cYour Citizens version is outdated please update it", true);
		}
	}
}