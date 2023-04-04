package me.mrgraycat.eglow.api.effect;

public enum EGlowColor {
    RED,
    DARK_RED,
    GOLD,
    YELLOW,
    GREEN,
    DARK_GREEN,
    AQUA,
    DARK_AQUA,
    BLUE,
    DARK_BLUE,
    PURPLE,
    PINK,
    WHITE,
    GRAY,
    DARK_GRAY,
    BLACK,
    NONE;

    @Override
    public String toString() {
        return super.toString().toLowerCase().replace("_", "");
    }
}