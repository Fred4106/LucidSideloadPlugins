package packets;

import packetUtils.PacketDef;
import packetUtils.PacketReflection;
import net.runelite.api.coords.WorldPoint;

public class MovementPackets {
    public static void queueMovement(int worldPointX, int worldPointY, boolean ctrlDown) {
        int ctrl = ctrlDown ? 2 : 0;
        PacketReflection.sendPacket(PacketDef.getMoveGameClick(), worldPointX, worldPointY, ctrl, 5);
    }

    public static void queueMovement(WorldPoint location) {
        queueMovement(location.getX(), location.getY(), false);
    }
}
