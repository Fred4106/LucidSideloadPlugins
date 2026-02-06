package packets;

import packetUtils.PacketDef;
import packetUtils.PacketReflection;

public class SailingPackets {
	public static void setDirection(int direction) {
		PacketReflection.sendPacket(PacketDef.getSetHeading(), direction);
	}
}