package com.fredplugins.gauntlet.overlay;

public enum WidgetInfoPlus
{
    PRAYER_THICK_SKIN(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.THICK_SKIN),
    PRAYER_BURST_OF_STRENGTH(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.BURST_OF_STRENGTH),
    PRAYER_CLARITY_OF_THOUGHT(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.CLARITY_OF_THOUGHT),
    PRAYER_SHARP_EYE(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.SHARP_EYE),
    PRAYER_MYSTIC_WILL(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.MYSTIC_WILL),
    PRAYER_ROCK_SKIN(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.ROCK_SKIN),
    PRAYER_SUPERHUMAN_STRENGTH(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.SUPERHUMAN_STRENGTH),
    PRAYER_IMPROVED_REFLEXES(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.IMPROVED_REFLEXES),
    PRAYER_RAPID_RESTORE(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.RAPID_RESTORE),
    PRAYER_RAPID_HEAL(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.RAPID_HEAL),
    PRAYER_PROTECT_ITEM(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.PROTECT_ITEM),
    PRAYER_HAWK_EYE(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.HAWK_EYE),
    PRAYER_MYSTIC_LORE(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.MYSTIC_LORE),
    PRAYER_STEEL_SKIN(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.STEEL_SKIN),
    PRAYER_ULTIMATE_STRENGTH(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.ULTIMATE_STRENGTH),
    PRAYER_INCREDIBLE_REFLEXES(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.INCREDIBLE_REFLEXES),
    PRAYER_PROTECT_FROM_MAGIC(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.PROTECT_FROM_MAGIC),
    PRAYER_PROTECT_FROM_MISSILES(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.PROTECT_FROM_MISSILES),
    PRAYER_PROTECT_FROM_MELEE(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.PROTECT_FROM_MELEE),
    PRAYER_EAGLE_EYE(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.EAGLE_EYE),
    PRAYER_MYSTIC_MIGHT(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.MYSTIC_MIGHT),
    PRAYER_RETRIBUTION(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.RETRIBUTION),
    PRAYER_REDEMPTION(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.REDEMPTION),
    PRAYER_SMITE(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.SMITE),
    PRAYER_PRESERVE(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.PRESERVE),
    PRAYER_CHIVALRY(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.CHIVALRY),
    PRAYER_PIETY(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.PIETY),
    PRAYER_RIGOUR(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.RIGOUR),
    PRAYER_AUGURY(com.fredplugins.gauntlet.overlay.WidgetIDPlus.PRAYER_GROUP_ID, com.fredplugins.gauntlet.overlay.WidgetIDPlus.Prayer.AUGURY),
    GAUNTLET_MAP(com.fredplugins.gauntlet.overlay.WidgetIDPlus.GAUNTLET_MAP_GROUP_ID, WidgetIDPlus.GauntletMap.CONTAINER),
    ;

    private final int groupId;
    private final int childId;

    WidgetInfoPlus(int groupId, int childId)
    {
        this.groupId = groupId;
        this.childId = childId;
    }

    /**
     * Gets the ID of the group-child pairing.
     *
     * @return the ID
     */
    public int getId()
    {
        return groupId << 16 | childId;
    }

    /**
     * Gets the group ID of the pair.
     *
     * @return the group ID
     */
    public int getGroupId()
    {
        return groupId;
    }

    /**
     * Gets the ID of the child in the group.
     *
     * @return the child ID
     */
    public int getChildId()
    {
        return childId;
    }

    /**
     * Gets the packed widget ID.
     *
     * @return the packed ID
     */
    public int getPackedId()
    {
        return groupId << 16 | childId;
    }

    /**
     * Utility method that converts an ID returned by {@link #getId()} back
     * to its group ID.
     *
     * @param id passed group-child ID
     * @return the group ID
     */
    public static int TO_GROUP(int id)
    {
        return id >>> 16;
    }

    /**
     * Utility method that converts an ID returned by {@link #getId()} back
     * to its child ID.
     *
     * @param id passed group-child ID
     * @return the child ID
     */
    public static int TO_CHILD(int id)
    {
        return id & 0xFFFF;
    }

    /**
     * Packs the group and child IDs into a single integer.
     *
     * @param groupId the group ID
     * @param childId the child ID
     * @return the packed ID
     */
    public static int PACK(int groupId, int childId)
    {
        return groupId << 16 | childId;
    }

}