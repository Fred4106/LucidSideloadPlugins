package com.fredplugins.common;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import java.util.function.Predicate;

public class WorldAreaUtil {
    /**
     * The plane the area is on.
     */
    @Getter
    private final WorldArea wrapped;

    public WorldAreaUtil(WorldArea wrapped)
    {
        this.wrapped = wrapped;
    }

    public static Point getComparisonPoint(WorldArea self, WorldArea other)
    {
        int x, y;
        if (other.getX() <= self.getX())
        {
            x = self.getX();
        }
        else x = Math.min(other.getX(), self.getX() + self.getWidth() - 1);
        if (other.getY() <= self.getY())
        {
            y = self.getY();
        }
        else y = Math.min(other.getY(), self.getY() + self.getHeight() - 1);

        return new Point(x, y);
    }

    public static Point getAxisDistances(WorldArea self, WorldArea other)
    {
        Point p1 = getComparisonPoint(self, other);
        Point p2 = getComparisonPoint(other, self);
        return new Point(Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getY() - p2.getY()));
    }

    /**
     * Calculates the next area that will be occupied if this area attempts
     * to move toward it by using the normal NPC travelling pattern.
     *
     * @param client the client to calculate with
     * @param target the target area
     * @param stopAtMeleeDistance whether to stop at melee distance to the target
     * @return the next occupied area
     */
    public WorldArea calculateNextTravellingPoint(Client client, WorldArea target,
                                                  boolean stopAtMeleeDistance)
    {
        return calculateNextTravellingPoint(client, target, stopAtMeleeDistance, x -> true);
    }

    /**
     * Calculates the next area that will be occupied if this area attempts
     * to move toward it by using the normal NPC travelling pattern.
     *
     * @param client the client to calculate with
     * @param target the target area
     * @param stopAtMeleeDistance whether to stop at melee distance to the target
     * @param extraCondition an additional condition to perform when checking valid tiles,
     * 	                     such as performing a check for un-passable actors
     * @return the next occupied area
     */
    public WorldArea calculateNextTravellingPoint(Client client, WorldArea target,
                                                  boolean stopAtMeleeDistance, Predicate<? super WorldPoint> extraCondition)
    {
        if (wrapped.getPlane() != target.getPlane())
        {
            return null;
        }

        if (wrapped.intersectsWith(target))
        {
            if (stopAtMeleeDistance)
            {
                // Movement is unpredictable when the NPC and actor stand on top of each other
                return null;
            }
            else
            {
                return wrapped;
            }
        }

        int dx = target.getX() - wrapped.getX();
        int dy = target.getY() - wrapped.getY();

        Point axisDistances = getAxisDistances(wrapped, target);
        String wrappedMsg = String.format("wrapped = (%d,%d,%d)(%d, %d)", wrapped.getX(), wrapped.getY(), wrapped.getPlane(), wrapped.getWidth(), wrapped.getHeight());
        String targetMsg = String.format("target = (%d,%d,%d)(%d, %d)", target.getX(), target.getY(), target.getPlane(), target.getWidth(), target.getHeight());
        String axisDistancesMsg = String.format("dist = (%d, %d)", axisDistances.getX(), axisDistances.getY());
        String finalMsg = String.format("%s, %s, %s", wrappedMsg, targetMsg, axisDistancesMsg);
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "WorldAreaUtil", finalMsg, "WorldAreaUtils");
        if (stopAtMeleeDistance && axisDistances.getX() + axisDistances.getY() == 1)
        {
            // NPC is in melee distance of target, so no movement is done
            return wrapped;
        }

        LocalPoint lp = LocalPoint.fromWorld(client, wrapped.getX(), wrapped.getY());
        if (lp == null ||
                lp.getSceneX() + dx < 0 || lp.getSceneX() + dy >= Constants.SCENE_SIZE ||
                lp.getSceneY() + dx < 0 || lp.getSceneY() + dy >= Constants.SCENE_SIZE)
        {
            // NPC is travelling out of the scene, so collision data isn't available
            return null;
        }

        int dxSig = Integer.signum(dx);
        int dySig = Integer.signum(dy);
        if (stopAtMeleeDistance && axisDistances.getX() == 1 && axisDistances.getY() == 1)
        {
            // When it needs to stop at melee distance, it will only attempt
            // to travel along the x axis when it is standing diagonally
            // from the target
            if (wrapped.canTravelInDirection(client.getTopLevelWorldView(), dxSig, 0, extraCondition))
            {
                return new WorldArea(wrapped.getX() + dxSig, wrapped.getY(), wrapped.getWidth(), wrapped.getHeight(), wrapped.getPlane());
            }
        }
        else
        {
            if (wrapped.canTravelInDirection(client.getTopLevelWorldView(), dxSig, dySig, extraCondition))
            {
                return new WorldArea(wrapped.getX() + dxSig, wrapped.getY() + dySig, wrapped.getWidth(), wrapped.getHeight(), wrapped.getPlane());
            }
            else if (dx != 0 && wrapped.canTravelInDirection(client.getTopLevelWorldView(), dxSig, 0, extraCondition))
            {
                return new WorldArea(wrapped.getX() + dxSig, wrapped.getY(),wrapped.getWidth(), wrapped.getHeight(), wrapped.getPlane());
            }
            else if (dy != 0 && Math.max(Math.abs(dx), Math.abs(dy)) > 1 &&
                    wrapped.canTravelInDirection(client.getTopLevelWorldView(), 0, dy, extraCondition))
            {
                // Note that NPCs don't attempts to travel along the y-axis
                // if the target is <= 1 tile distance away
                return new WorldArea(wrapped.getX(),wrapped.getY() + dySig, wrapped.getWidth(), wrapped.getHeight(), wrapped.getPlane());
            }
        }

        // The NPC is stuck
        return wrapped;
    }
}
