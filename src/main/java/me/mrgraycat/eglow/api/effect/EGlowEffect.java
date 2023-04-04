package me.mrgraycat.eglow.api.effect;

public enum EGlowEffect {
    RAINBOW_SLOW,
    RAINBOW_FAST;

    @Override
    public String toString() {
        return super.toString().toLowerCase().replace("_", "");
    }
}