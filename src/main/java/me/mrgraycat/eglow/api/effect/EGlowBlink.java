package me.mrgraycat.eglow.api.effect;

public enum EGlowBlink {
    RED_SLOW,
    RED_FAST,
    DARK_RED_SLOW,
    DARK_RED_FAST,
    GOLD_SLOW,
    GOLD_FAST,
    YELLOW_SLOW,
    YELLOW_FAST,
    GREEN_SLOW,
    GREEN_FAST,
    DARK_GREEN_SLOW,
    DARK_GREEN_FAST,
    AQUA_SLOW,
    AQUA_FAST,
    DARK_AQUA_SLOW,
    DARK_AQUA_FAST,
    BLUE_SLOW,
    BLUE_FAST,
    DARK_BLUE_SLOW,
    DARK_BLUE_FAST,
    PURPLE_SLOW,
    PURPLE_FAST,
    PINK_SLOW,
    PINK_FAST,
    WHITE_SLOW,
    WHITE_FAST,
    GRAY_SLOW,
    GRAY_FAST,
    DARK_GRAY_SLOW,
    DARK_GRAY_FAST,
    BLACK_SLOW,
    BLACK_FAST;

    @Override
    public String toString() {
        return "blink" + super.toString().toLowerCase().replace("_", "");
    }
}