package com.nemesis.fart;

/**
 * Faceplate intents that may mean My Menu / Custom on Gen5W.
 * Confirm the exact string(s) on-car before shipping the receiver.
 */
public final class MyMenuIntents {
    public static final String HKMC_MYMENU = "com.hkmc.intent.action.MYMENU_SHORTCUT";
    public static final String MOBIS_CUSTOM = "com.mobis.intent.action.CUSTOM_SHORTCUT";

    private MyMenuIntents() {}

    /** True if this broadcast should feed {@link PressCounter}. */
    public static boolean isTrigger(String action) {
        if (action == null) {
            return false;
        }
        return HKMC_MYMENU.equals(action) || MOBIS_CUSTOM.equals(action);
    }
}
