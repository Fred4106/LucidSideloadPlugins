package com.fredplugins.mixology;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ObjectID;

public enum AlchemyObject {
    MOX_LEVER(ObjectID.MM_LAB_SWITCH_MOX, new WorldPoint(1395, 9324, 0)),
    AGA_LEVER(ObjectID.MM_LAB_SWITCH_AGA, new WorldPoint(1394, 9324, 0)),
    LYE_LEVER(ObjectID.MM_LAB_SWITCH_LYE, new WorldPoint(1393, 9324, 0)),
    MIXING_VESSEL(ObjectID.MM_LAB_VESSEL, new WorldPoint(1394, 9326, 0)),
    ALEMBIC(ObjectID.MM_LAB_MACHINE_ALEMBIC, new WorldPoint(1391, 9325, 0)),
    AGITATOR(ObjectID.MM_LAB_MACHINE_AGITATOR, new WorldPoint(1393, 9329, 0)),
    RETORT(ObjectID.MM_LAB_MACHINE_RETORT, new WorldPoint(1397, 9325, 0)),
    CONVEYOR_BELT(ObjectID.MM_LAB_CONVEYOR, new WorldPoint(1394, 9331, 0)),
    HOPPER(ObjectID.MM_LAB_HOPPER, new WorldPoint(1394, 9322, 0)),
    DIGWEED_NORTH_EAST(ObjectID.MM_HERB_1, new WorldPoint(1399, 9331, 0)),
    DIGWEED_SOUTH_EAST(ObjectID.MM_HERB_2, new WorldPoint(1399, 9322, 0)),
    DIGWEED_SOUTH_WEST(ObjectID.MM_HERB_3, new WorldPoint(1389, 9322, 0)),
    DIGWEED_NORTH_WEST(ObjectID.MM_HERB_4, new WorldPoint(1389, 9331, 0));

    private final int objectId;
    private final WorldPoint coordinate;

    AlchemyObject(int objectId, WorldPoint coordinate) {
        this.objectId = objectId;
        this.coordinate = coordinate;
    }

    public int objectId() {
        return objectId;
    }

    public WorldPoint coordinate() {
        return coordinate;
    }
}