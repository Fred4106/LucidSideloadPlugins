package com.fredplugins.superClickHelper

import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.TextExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.WidgetExtensions.*
import com.fredplugins.common.utils.ShimUtils.Logging
import com.fredplugins.common.utils.WorldPointUtils
import com.fredplugins.superClickHelper.HallowedSepulchreData.SEPULCHRE_FLOORS
import com.fredplugins.superClickHelper.HallowedSepulchreData.SEPULCHRE_NPCS
import com.fredplugins.superClickHelper.HallowedSepulchreData.SEPULCHRE_VARBITS
import com.google.inject.Inject
import com.google.inject.Singleton
import com.lucidplugins.api.utils.GameObjectUtils
import ethanApiPlugin.collections.query.TileObjectQuery
import net.runelite.api.Client
import net.runelite.api.DynamicObject
import net.runelite.api.GameObject
import net.runelite.api.GameState
import net.runelite.api.Item
import net.runelite.api.ItemComposition
import net.runelite.api.ItemContainer
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.NPC
import net.runelite.api.coords.Angle
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.GameObjectDespawned
import net.runelite.api.events.GameObjectSpawned
import net.runelite.api.events.GameTick
import net.runelite.api.events.ItemContainerChanged
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.MenuOpened
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.PostMenuSort
import net.runelite.api.events.VarbitChanged
import net.runelite.api.gameval.InterfaceID
import net.runelite.api.gameval.InventoryID
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.NpcID
import net.runelite.api.gameval.ObjectID
import net.runelite.api.gameval.VarbitID
import net.runelite.api.widgets.Widget
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
import net.runelite.client.events.ExternalPluginsChanged
import net.runelite.client.events.PluginChanged
import net.runelite.client.plugins.PluginManager
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.OverlayPosition
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil
import net.runelite.client.util.Text as TextUtil

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.Random
import scala.util.Try
import scala.util.chaining.*

object HallowedSepulchreData {
	private val HALLOWED_INVISIBLE_NPC = 38408
//	private val HALLOWED_FIRE_TRAP = 38409
//	private val HALLOWED_FIRE_TRAP_V2 = 38410
//	private val HALLOWED_FIRE_TRAP_V3 = 38411
//	private val HALLOWED_FIRE_TRAP_V4 = 38412
//	private val HALLOWED_FIRE_TRAP_V5 = 38413
//	private val HALLOWED_FIRE_TRAP_V6 = 38414
//	private val HALLOWED_FIRE_TRAP_V7 = 38415
//	private val HALLOWED_FIRE_TRAP_T2 = 38416
//	private val HALLOWED_FIRE_TRAP_T2_V2 = 38417
//	private val HALLOWED_FIRE_TRAP_T2_V3 = 38418
//	private val HALLOWED_FIRE_TRAP_T2_V4 = 38419
//	private val HALLOWED_FIRE_TRAP_T2_V5 = 38420
//	private val HALLOWED_FIRE_TRAP_T3 = 38421
//	private val HALLOWED_FIRE_TRAP_T3_V2 = 38422
//	private val HALLOWED_FIRE_TRAP_T3_V3 = 38423
//	private val HALLOWED_FIRE_TRAP_T3_V4 = 38424
//	private val HALLOWED_FIRE_TRAP_T3_V5 = 38425
	private val HALLOWED_FIRE_TRAP_ANIMATIONS = 38427
	private val HALLOWED_SWORD_TRAP = 38428
	private val HALLOWED_SWORD_TRAP_OWL = 38429
	private val HALLOWED_SWORD_TRAP_LION = 38430
	private val HALLOWED_SWORD_TRAP_UNICORN = 38431
	private val HALLOWED_SWORD_TRAP_T2 = 38432
	private val HALLOWED_SWORD_TRAP_T2_LION = 38433
	private val HALLOWED_SWORD_TRAP_T3 = 38434
	private val HALLOWED_SWORD_TRAP_T3_LION = 38435
	private val HALLOWED_SWORD_TRAP_THROWN = 38436
	private val HALLOWED_SWORD_TRAP_OWL_THROWN = 38437
	private val HALLOWED_SWORD_TRAP_LION_THROWN = 38438
	private val HALLOWED_SWORD_TRAP_UNICORN_THROWN = 38439
	private val HALLOWED_SWORD_TRAP_T2_THROWN = 38440
	private val HALLOWED_SWORD_TRAP_T2_LION_THROWN = 38441
	private val HALLOWED_SWORD_TRAP_T3_THROWN = 38442
	private val HALLOWED_SWORD_TRAP_T3_LION_THROWN = 38443
	private val HALLOWED_PROJECTILE_TRAP = 38444
	private val HALLOWED_PROJECTILE_TRAP_T2 = 38445
	private val HALLOWED_PROJECTILE_TRAP_T3 = 38446
	private val HALLOWED_TELEPORT_TRAP = 38447
	private val HALLOWED_TELEPORT_TRAP_FORWARD = 38448
	private val HALLOWED_LIGHTNING_TRAP_T2 = 38449
	private val HALLOWED_LIGHTNING_TRAP_T3 = 38450
	private val HALLOWED_EXIT = 38451
	private val HALLOWED_MINIGAME_START = 38452
	private val HALLOWED_PROGRESS_STAIRS = 38453
	private val HALLOWED_LOCKED_STAIRS = 38454
	private val HALLOWED_PATH_END_JUMPOVER_01_NORTH = 38455
	private val HALLOWED_PATH_END_JUMPOVER_01_EAST = 38456
	private val HALLOWED_PATH_END_JUMPOVER_01_SOUTH = 38457
	private val HALLOWED_PATH_END_JUMPOVER_01_WEST = 38458
	private val HALLOWED_PATH_END_JUMPOVER_02 = 38459
	private val HALLOWED_PATH_END_GATE = 38460
	private val HALLOWED_STAIRS_RETURN = 38461
	private val HALLOWED_FLOOR_1_NORTHPATH_DROP = 38462
	private val HALLOWED_FLOOR_1_EASTPATH_STAIRS = 38463
	private val HALLOWED_FLOOR_1_SOUTHPATH_STAIRS = 38464
	private val HALLOWED_FLOOR_1_WESTPATH_DROP = 38465
	private val HALLOWED_FLOOR_2_NORTHPATH_STAIRS = 38466
	private val HALLOWED_FLOOR_2_EASTPATH_STAIRS = 38467
	private val HALLOWED_FLOOR_2_SOUTHPATH_STAIRS = 38468
	private val HALLOWED_FLOOR_2_WESTPATH_DROP = 38469
	private val HALLOWED_FLOOR_2_STEPPINGSTONE = 38470
	private val HALLOWED_FLOOR_3_EASTPATH_DROP = 38471
	private val HALLOWED_FLOOR_3_WESTPATH_DROP = 38472
	private val HALLOWED_FLOOR_4_NORTHPATH_DROP = 38473
	private val HALLOWED_FLOOR_4_SOUTHPATH_STAIRS = 38474
	private val HALLOWED_FLOOR_5_DROP_1 = 38475
	private val HALLOWED_FLOOR_5_DROP_2 = 38476
	private val HALLOWED_FLOOR_5_STEPPINGSTONE = 38477
	private val HALLOWED_FLOOR5_EXIT = 38478
	private val HALLOWED_LOBBY_SKELETON = 38479
	private val HALLOWED_STEPS_UP = 38696
	private val HALLOWED_STEPS_DOWN = 38697
	private val HALLOWED_NOTICEBOARD = 38790
	private val HALLOWED_TREASURE_RANGED_HALLOWED = 38791
	private val HALLOWED_TREASURE_RANGED_MITHRIL = 38792
	private val HALLOWED_TREASURE_RANGED_PILLAR_READY = 38793
	private val HALLOWED_TREASURE_RANGED_PILLAR_INACTIVE = 38794
	private val HALLOWED_TREASURE_RANGED_PILLAR_LOWER = 38795
	private val HALLOWED_TREASURE_RANGED_PILLAR_TOP = 38796
	private val HALLOWED_TREASURE_PRAYER_INACTIVE = 38797
	private val HALLOWED_TREASURE_PRAYER_READY = 38798
	private val HALLOWED_TREASURE_PRAYER_FINAL = 38799
	private val HALLOWED_TREASURE_PRAYER_INACTIVE_MIRROR = 38800
	private val HALLOWED_TREASURE_PRAYER_READY_MIRROR = 38801
	private val HALLOWED_TREASURE_PRAYER_FINAL_MIRROR = 38802
	private val HALLOWED_TREASURE_PRAYER_BARRIER_INACTIVE = 38803
	private val HALLOWED_TREASURE_PRAYER_BARRIER_READY = 38804
	private val HALLOWED_TREASURE_PRAYER_BARRIER_FINAL = 38805
	private val HALLOWED_TREASURE_CONSTRUCTION_INACTIVE = 38806
	private val HALLOWED_TREASURE_CONSTRUCTION_READY = 38807
	private val HALLOWED_TREASURE_CONSTRUCTION_FINAL = 38808
	private val HALLOWED_TREASURE_CONSTRUCTION_POOL_INACTIVE = 38809
	private val HALLOWED_TREASURE_CONSTRUCTION_POOL_READY = 38810
	private val HALLOWED_TREASURE_CONSTRUCTION_POOL_FINAL = 38811
	private val HALLOWED_TREASURE_CONSTRUCTION_DECKING_INACTIVE = 38812
	private val HALLOWED_TREASURE_CONSTRUCTION_DECKING_READY = 38813
	private val HALLOWED_TREASURE_CONSTRUCTION_DECKING_FINAL = 38814
	private val HALLOWED_TREASURE_CONSTRUCTION_DECKING_POOL_INACTIVE = 38815
	private val HALLOWED_TREASURE_CONSTRUCTION_DECKING_POOL_READY = 38816
	private val HALLOWED_TREASURE_CONSTRUCTION_DECKING_POOL_FINAL = 38817
	private val HALLOWED_TREASURE_BRIDGE_BROKEN_END1 = 38818
	private val HALLOWED_TREASURE_BRIDGE_BROKEN_END2 = 38819
	private val HALLOWED_TREASURE_BRIDGE_BROKEN_END1_POOL = 38820
	private val HALLOWED_TREASURE_BRIDGE_BROKEN_END2_POOL = 38821
	private val HALLOWED_TREASURE_BRIDGE_PANEL_01 = 38822
	private val HALLOWED_TREASURE_BRIDGE_PANEL_02 = 38823
	private val HALLOWED_TREASURE_BRIDGE_PANEL_03 = 38824
	private val HALLOWED_TREASURE_BRIDGE_PANEL_EDGE_01 = 38825
	private val HALLOWED_TREASURE_BRIDGE_PANEL_EDGE_02 = 38826
	private val HALLOWED_TREASURE_MAGIC_INACTIVE = 38827
	private val HALLOWED_TREASURE_MAGIC_READY = 38828
	private val HALLOWED_TREASURE_MAGIC_FINAL = 38829
	private val HALLOWED_REWARD_COFFIN_UNLOOTED = 38830
	private val HALLOWED_REWARD_COFFIN_LOOTED = 38831
	private val HALLOWED_F5_GRAPPLE_COFFIN_UNLOOTED = 38832
	private val HALLOWED_F5_GRAPPLE_COFFIN_LOOTED = 38833
	private val HALLOWED_F5_PORTAL_COFFIN_UNLOOTED = 38834
	private val HALLOWED_F5_PORTAL_COFFIN_LOOTED = 38835
	private val HALLOWED_F5_BRIDGE_COFFIN_UNLOOTED = 38836
	private val HALLOWED_F5_BRIDGE_COFFIN_LOOTED = 38837
	private val HALLOWED_FINAL_CHEST_UNLOOTED = 38838
	private val HALLOWED_FINAL_CHEST_LOOTED = 38839
	private val HALLOWED_FINAL_CHEST_BARRIER_INACTIVE = 38840
	private val HALLOWED_FINAL_CHEST_BARRIER_ACTIVE = 38841
	private val HALLOWED_RANGED_GRAPPLE = 39523
	private val HALLOWED_RANGED_PILLAR_MULTI = 39524
	private val HALLOWED_PRAYER_MULTI = 39525
	private val HALLOWED_PRAYER_MULTI_MIRROR = 39526
	private val HALLOWED_CONSTRUCTION_MULTI = 39527
	private val HALLOWED_CONSTRUCTION_POOL_MULTI = 39528
	private val HALLOWED_CONSTRUCTION_MULTI_2 = 39529
	private val HALLOWED_CONSTRUCTION_POOL_MULTI_2 = 39530
	private val HALLOWED_CONSTRUCTION_PANEL_1_MULTI = 39531
	private val HALLOWED_CONSTRUCTION_PANEL_2_MULTI = 39532
	private val HALLOWED_MAGIC_MULTI = 39533
	private val HALLOWED_TREASURE_PRAYER_BARRIER_MULTI = 39534
	private val HALLOWED_F5_GRAPPLE_COFFIN = 39536
	private val HALLOWED_F5_PORTAL_COFFIN = 39537
	private val HALLOWED_F5_BRIDGE_COFFIN = 39538
	private val HALLOWED_FINAL_CHEST = 39539
	private val HALLOWED_FINAL_CHEST_BARRIER = 39540
	private val HALLOWED_REWARD_COFFIN_A = 39544
	private val HALLOWED_REWARD_COFFIN_B = 39545
	private val HALLOWED_STAIRS_FLOOR1 = 39622
	private val HALLOWED_STAIRS_FLOOR2 = 39623
	private val HALLOWED_STAIRS_FLOOR3 = 39624
	private val HALLOWED_STAIRS_FLOOR4 = 39625

	val SEPULCHRE_FLOOR_1 = new WorldArea(2232, 5946, 82, 80, 2)
	val SEPULCHRE_FLOOR_2 = new WorldArea(2482, 5947, 93, 82, 2)
	val SEPULCHRE_FLOOR_3 = new WorldArea(2362, 5811, 83, 89, 2)
	val SEPULCHRE_FLOOR_4 = new WorldArea(2483, 5807, 90, 91, 2)
	val SEPULCHRE_FLOOR_5 = new WorldArea(2229, 5811, 87, 77, 2)
	inline def SEPULCHRE_FIRE_TRAP = List(
		38409, 38410, 38411,
		38412, 38413, 38414,
		38415, 38416, 38417,
		38418, 38419, 38420,
		38421, 38422, 38423,
		38424, 38425
	)
	inline def SEPULCHRE_FLOORS = List(
		SEPULCHRE_FLOOR_1, SEPULCHRE_FLOOR_2, SEPULCHRE_FLOOR_3, SEPULCHRE_FLOOR_4, SEPULCHRE_FLOOR_5
	)
	inline def SEPULCHRE_OBSTACLE_IDS = Set(
		// Stairs and Platforms (and one Gate)
		HALLOWED_PATH_END_GATE, HALLOWED_PATH_END_JUMPOVER_01_NORTH, HALLOWED_PATH_END_JUMPOVER_01_EAST, HALLOWED_PATH_END_JUMPOVER_01_SOUTH, HALLOWED_PATH_END_JUMPOVER_01_WEST, HALLOWED_PATH_END_JUMPOVER_02,
		HALLOWED_FLOOR_2_STEPPINGSTONE, HALLOWED_FLOOR_5_STEPPINGSTONE, HALLOWED_FLOOR_1_NORTHPATH_DROP, HALLOWED_FLOOR_1_EASTPATH_STAIRS, HALLOWED_FLOOR_1_SOUTHPATH_STAIRS, HALLOWED_FLOOR_1_WESTPATH_DROP,
		HALLOWED_FLOOR_2_NORTHPATH_STAIRS, HALLOWED_FLOOR_2_EASTPATH_STAIRS, HALLOWED_FLOOR_2_SOUTHPATH_STAIRS, HALLOWED_FLOOR_2_WESTPATH_DROP, HALLOWED_FLOOR_3_EASTPATH_DROP, HALLOWED_FLOOR_3_WESTPATH_DROP,
		HALLOWED_FLOOR_4_NORTHPATH_DROP, HALLOWED_FLOOR_4_SOUTHPATH_STAIRS, HALLOWED_FLOOR_5_DROP_1, HALLOWED_FLOOR_5_DROP_2
	)
	inline def SEPULCHRE_SKILL_OBSTACLE_IDS = Set(
		// Grapple, Portal, and Bridge skill obstacles
		// They are multilocs, thus we use the NullObjectID
		HALLOWED_RANGED_PILLAR_MULTI, HALLOWED_PRAYER_MULTI, HALLOWED_PRAYER_MULTI_MIRROR, HALLOWED_CONSTRUCTION_MULTI, HALLOWED_CONSTRUCTION_POOL_MULTI, HALLOWED_MAGIC_MULTI
	)

	inline def SEPULCHRE_NPCS = Set(
				NpcID.HALLOWED_PROJECTILE_NPC, NpcID.HALLOWED_PROJECTILE_NPC_T2, NpcID.HALLOWED_PROJECTILE_NPC_T3,  // arrows
		NpcID.HALLOWED_SWORD_NPC, NpcID.HALLOWED_SWORD_NPC_T2, NpcID.HALLOWED_SWORD_NPC_T3   // swords
	)
	inline def SEPULCHRE_VARBITS  = Map(
		VarbitID.HALLOWED_TOME -> "HALLOWED_TOME",
//		VarbitID.HALLOWED_TIME_REMAINING -> "HALLOWED_TIME_REMAINING",
//		VarbitID.HALLOWED_TIME_SPENT -> "HALLOWED_TIME_SPENT",
//		VarbitID.HALLOWED_CURRENT_FLOOR_TIME_SPENT -> "HALLOWED_CURRENT_FLOOR_TIME_SPENT",
		VarbitID.HALLOWED_CURRENT_FLOOR -> "HALLOWED_CURRENT_FLOOR",
		VarbitID.HALLOWED_ALTERNATE_SAFESPOT -> "HALLOWED_ALTERNATE_SAFESPOT",
//		VarbitID.HALLOWED_GHOST_OWLKNIGHT_FOUND -> "HALLOWED_GHOST_OWLKNIGHT_FOUND",
//		VarbitID.HALLOWED_GHOST_LIONKNIGHT_FOUND -> "HALLOWED_GHOST_LIONKNIGHT_FOUND",
//		VarbitID.HALLOWED_GHOST_WOLFKNIGHT_FOUND -> "HALLOWED_GHOST_WOLFKNIGHT_FOUND",
//		VarbitID.HALLOWED_GHOST_UNICORNKNIGHT_FOUND -> "HALLOWED_GHOST_UNICORNKNIGHT_FOUND",
//		VarbitID.HALLOWED_GHOST_ARCHPRIEST_FOUND -> "HALLOWED_GHOST_ARCHPRIEST_FOUND",
//		VarbitID.HALLOWED_GHOST_OWLKNIGHT_VISIBLE -> "HALLOWED_GHOST_OWLKNIGHT_VISIBLE",
//		VarbitID.HALLOWED_GHOST_LIONKNIGHT_VISIBLE -> "HALLOWED_GHOST_LIONKNIGHT_VISIBLE",
//		VarbitID.HALLOWED_GHOST_WOLFKNIGHT_VISIBLE -> "HALLOWED_GHOST_WOLFKNIGHT_VISIBLE",
//		VarbitID.HALLOWED_GHOST_UNICORNKNIGHT_VISIBLE -> "HALLOWED_GHOST_UNICORNKNIGHT_VISIBLE",
//		VarbitID.HALLOWED_GHOST_ARCHPRIEST_VISIBLE -> "HALLOWED_GHOST_ARCHPRIEST_VISIBLE",
		VarbitID.HALLOWED_TUTORIAL -> "HALLOWED_TUTORIAL",
		VarbitID.HALLOWED_ENTRY_CHECK -> "HALLOWED_ENTRY_CHECK",
		VarbitID.HALLOWED_BLOCK_CLIMBOVER -> "HALLOWED_BLOCK_CLIMBOVER",
		VarbitID.HALLOWED_ADVENTURER_MARKS_GIVEN -> "HALLOWED_ADVENTURER_MARKS_GIVEN",
		VarbitID.HALLOWED_SKILLPET_DARK_UNLOCKED -> "HALLOWED_SKILLPET_DARK_UNLOCKED",
//		VarbitID.HALLOWED_TOME_PAGE1 -> "HALLOWED_TOME_PAGE1",
//		VarbitID.HALLOWED_TOME_PAGE2 -> "HALLOWED_TOME_PAGE2",
//		VarbitID.HALLOWED_TOME_PAGE3 -> "HALLOWED_TOME_PAGE3",
//		VarbitID.HALLOWED_TOME_PAGE4 -> "HALLOWED_TOME_PAGE4",
//		VarbitID.HALLOWED_TOME_PAGE5 -> "HALLOWED_TOME_PAGE5",
		VarbitID.HALLOWED_FINAL_CHEST_COUNT -> "HALLOWED_FINAL_CHEST_COUNT",

		VarbitID.HALLOWED_RANGED_STATE -> "HALLOWED_RANGED_STATE",
		VarbitID.HALLOWED_PRAYER_STATE -> "HALLOWED_PRAYER_STATE",
		VarbitID.HALLOWED_CONSTRUCTION_STATE -> "HALLOWED_CONSTRUCTION_STATE",
		VarbitID.HALLOWED_MAGIC_STATE -> "HALLOWED_MAGIC_STATE",

		VarbitID.HALLOWED_GRAPPLE_TYPE -> "HALLOWED_GRAPPLE_TYPE",
		VarbitID.HALLOWED_PRAYER_ASHES_OFFERED -> "HALLOWED_PRAYER_ASHES_OFFERED",
		VarbitID.HALLOWED_MAGIC_SPELL_FAILED -> "HALLOWED_MAGIC_SPELL_FAILED",

		VarbitID.HALLOWED_CURRENT_FLOOR_LOOTED -> "HALLOWED_CURRENT_FLOOR_LOOTED",
		VarbitID.HALLOWED_CURRENT_FLOOR_LOOTED_B -> "HALLOWED_CURRENT_FLOOR_LOOTED_B",
		VarbitID.HALLOWED_FINAL_FLOOR_LOOTED -> "HALLOWED_FINAL_FLOOR_LOOTED",
		VarbitID.HALLOWED_FLOOR5_GRAPPLE_LOOTED -> "HALLOWED_FLOOR5_GRAPPLE_LOOTED",
		VarbitID.HALLOWED_FLOOR5_PORTAL_LOOTED -> "HALLOWED_FLOOR5_PORTAL_LOOTED",
		VarbitID.HALLOWED_FLOOR5_BRIDGE_LOOTED -> "HALLOWED_FLOOR5_BRIDGE_LOOTED",

		VarbitID.HALLOWED_ADVENTURER_FLOOR1_EAST -> "HALLOWED_ADVENTURER_FLOOR1_EAST",
		VarbitID.HALLOWED_ADVENTURER_FLOOR1_WEST -> "HALLOWED_ADVENTURER_FLOOR1_WEST",
		VarbitID.HALLOWED_ADVENTURER_FLOOR1_MELVIN -> "HALLOWED_ADVENTURER_FLOOR1_MELVIN",
		VarbitID.HALLOWED_ADVENTURER_FLOOR2_NORTH -> "HALLOWED_ADVENTURER_FLOOR2_NORTH",
		VarbitID.HALLOWED_ADVENTURER_FLOOR2_SOUTH -> "HALLOWED_ADVENTURER_FLOOR2_SOUTH",
		VarbitID.HALLOWED_ADVENTURER_FLOOR2_SOUTH -> "HALLOWED_ADVENTURER_FLOOR2_SOUTH",
		VarbitID.HALLOWED_ADVENTURER_FLOOR3_EAST -> "HALLOWED_ADVENTURER_FLOOR3_EAST",
		VarbitID.HALLOWED_ADVENTURER_FLOOR2_WEST -> "HALLOWED_ADVENTURER_FLOOR2_WEST",
		VarbitID.HALLOWED_ADVENTURER_FLOOR3_WEST -> "HALLOWED_ADVENTURER_FLOOR3_WEST",
		VarbitID.HALLOWED_ADVENTURER_FLOOR3_TREASURE -> "HALLOWED_ADVENTURER_FLOOR3_TREASURE",
		VarbitID.HALLOWED_TELEPORTS_RECEIVED -> "HALLOWED_TELEPORTS_RECEIVED",
		VarbitID.HALLOWED_NORMAL_CHEST_COUNT -> "HALLOWED_NORMAL_CHEST_COUNT",
//		VarbitID.HALLOWED_RANGED_XP_EARNED -> "HALLOWED_RANGED_XP_EARNED",
//		VarbitID.HALLOWED_MAGIC_XP_EARNED -> "HALLOWED_MAGIC_XP_EARNED",
//		VarbitID.HALLOWED_PRAYER_XP_EARNED -> "HALLOWED_PRAYER_XP_EARNED",
//		VarbitID.HALLOWED_CONSTRUCTION_XP_EARNED -> "HALLOWED_CONSTRUCTION_XP_EARNED",
		VarbitID.HALLOWED_STORAGE_TOKEN -> "HALLOWED_STORAGE_TOKEN",
		VarbitID.HALLOWED_STORAGE_GRAPPLE -> "HALLOWED_STORAGE_GRAPPLE",
		VarbitID.HALLOWED_STORAGE_FOCUS -> "HALLOWED_STORAGE_FOCUS",
		VarbitID.HALLOWED_STORAGE_SYMBOL -> "HALLOWED_STORAGE_SYMBOL",
		VarbitID.HALLOWED_STORAGE_HAMMER -> "HALLOWED_STORAGE_HAMMER",
		VarbitID.HALLOWED_STORAGE_RING -> "HALLOWED_STORAGE_RING",
		VarbitID.HALLOWED_SACK_REMINDER -> "HALLOWED_SACK_REMINDER",
		VarbitID.HALLOWED_PRIVATE_INSTANCES -> "HALLOWED_PRIVATE_INSTANCES",
		VarbitID.HALLOWED_PRIVATE_INSTANCES_PURCHASED -> "HALLOWED_PRIVATE_INSTANCES_PURCHASED",
//		VarbitID.HALLOWED_TIME_PB_ALL -> "HALLOWED_TIME_PB_ALL",
//		VarbitID.HALLOWED_TIME_PB_F1 -> "HALLOWED_TIME_PB_F1",
//		VarbitID.HALLOWED_TIME_PB_F2 -> "HALLOWED_TIME_PB_F2",
//		VarbitID.HALLOWED_TIME_PB_F3 -> "HALLOWED_TIME_PB_F3",
//		VarbitID.HALLOWED_TIME_PB_F4 -> "HALLOWED_TIME_PB_F4",
//		VarbitID.HALLOWED_TIME_PB_F5 -> "HALLOWED_TIME_PB_F5",
//
//		VarbitID.HALLOWED_PB_RESET_1 -> "HALLOWED_PB_RESET_1",
//		VarbitID.HALLOWED_PB_RESET_2 -> "HALLOWED_PB_RESET_2",
//		VarbitID.HALLOWED_PB_RESET_3 -> "HALLOWED_PB_RESET_3",
//		VarbitID.HALLOWED_PB_RESET_4 -> "HALLOWED_PB_RESET_4",
//		VarbitID.HALLOWED_PB_RESET_5 -> "HALLOWED_PB_RESET_5",
//		VarbitID.HALLOWED_PB_RESET_6 -> "HALLOWED_PB_RESET_6",
//		VarbitID.HALLOWED_PB_RESET_7 -> "HALLOWED_PB_RESET_7"
	)
}

class HallowedSepulchreHelper(plugin: SuperClickerPlugin) extends MonitorService(plugin, "DEBUG") {
//	given
//	given ModelOutlineRenderer = plugin.getInjector.getInstance(classOf[ModelOutlineRenderer])

	val npcs: mutable.Set[NPC] = mutable.Set.empty[NPC]
	val fireStatues: mutable.Set[GameObject] = mutable.Set.empty[GameObject]
	var timeRemaining: Int = -1

	var currentFloor: Int = -1
	override protected def startService(): Unit = {
		npcs.clear()
		fireStatues.clear()
		currentFloor = -1
		timeRemaining = -1
		client.getTopLevelWorldView.npcs().asScala.toList.filter(n => SEPULCHRE_NPCS.contains(n.getId))
			.foreach(npcs.add)
		GameObjectUtils.search().filter(to => to.isInstanceOf[GameObject] && filterFireStatues(to.asInstanceOf[GameObject]).isDefined).result.asScala.toList
			.flatMap(to => Option(to).collect{
				case go: GameObject => go
			})
			.foreach(fireStatues.add)
	}
	override protected def stopService(): Unit = {
		npcs.clear()
		fireStatues.clear()
		currentFloor = -1
		timeRemaining = -1
		overlayManager.remove(HallowedSepulchreOverlay)
		overlayManager.remove(HallowedSepulchrePanel)
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(client.getGameState == GameState.LOGGED_IN) {
			val curFloor =
				client.getLocalPlayer.getWorldLocation
					.pipe(wp => WorldPointUtils.toTemplate(wp))//if(client.getLocalPlayer.getWorldView.isInstance) WorldPointUtils.fromInstance(wp)(using client) else wp)
					.pipe(wp => SEPULCHRE_FLOORS.indexWhere(_.contains2D(wp)))
			if(curFloor != currentFloor) {
				log.debug(s"Changed floor from ${currentFloor} to ${curFloor}")
				if(curFloor == -1) {
					overlayManager.remove(HallowedSepulchreOverlay)
					overlayManager.remove(HallowedSepulchrePanel)
				} else {
					overlayManager.add(HallowedSepulchreOverlay)
					if(config.isHallowedSepulchrePanel) {
						overlayManager.add(HallowedSepulchrePanel)
					}
				}
				currentFloor = curFloor
			}
		}
	}

	private def filterFireStatues(go: GameObject): Option[GameObject] = {
		val fireStatueRange = (38409 to 38425)
		Option(go).filter(_.getId.pipe(fireStatueRange.contains))
	}

	@Subscribe
	def onGameObjectSpawned(event: GameObjectSpawned): Unit = {
		filterFireStatues(event.getGameObject)
			.foreach(fireStatues.add)
//		val newGameObjects = for {
//			obj <-
//			wp = WorldPointUtils.toTemplate(obj.getWorldLocation)(using client)
//			floor = SEPULCHRE_FLOORS.indexWhere(_.contains(wp)) if floor != -1
//		} yield (floor, wp, obj.getId)
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if (e.getGroup == SuperClickHelperConfig.GroupName) {
			e.getKey match {
				case "hallowedSepulchrePanel" => {
					if(config.isHallowedSepulchrePanel && currentFloor > -1) overlayManager.add(HallowedSepulchrePanel)
					else overlayManager.remove(HallowedSepulchrePanel)
				}
				case _ => 
			}
		}
	}

	@Subscribe
	def onGameObjectDespawned(event: GameObjectDespawned): Unit = {
		fireStatues.remove(event.getGameObject)
	}

	@Subscribe
	def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
		val npc = npcSpawned.getNpc
		if (SEPULCHRE_NPCS.contains(npc.getId)) npcs.add(npc)
	}

	@Subscribe
	def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
		val npc = npcDespawned.getNpc
		npcs.remove(npc)
	}
	@Subscribe
	def onVarbitChanged(event: VarbitChanged): Unit = {
		SEPULCHRE_VARBITS.find(_._1 == event.getVarbitId).map(x => (x._1, x._2, event.getValue))
			.foreach((id, name, value) => {
				log.debug(s"Varbit ${name}(${id}) = ${value}")
			})
		if(event.getVarbitId == VarbitID.HALLOWED_TIME_REMAINING) {
			timeRemaining = event.getValue
		}
	}

	private object HallowedSepulchreOverlay extends Overlay(plugin) {
		import com.fredplugins.common.overlays.{renderGameObjectOverlay, renderActorOverlay}

		setPosition(OverlayPosition.DYNAMIC)
		setLayer(OverlayLayer.ABOVE_WIDGETS)
		setPriority(Overlay.PRIORITY_HIGHEST)

		override def render(graphics: Graphics2D): Dimension = {
			given Graphics2D = graphics
			npcs.toList
				.foreach(n => {
					renderActorOverlay(n, s"${Option(n.getTransformedComposition).getOrElse(n.getComposition).pipe(c => s"${c.getId}: ${c.getName}")}")(2, 1, Color.RED, false)
//					Option(n.getCanvasTilePoly)
//						.foreach(OverlayUtil.renderPolygon(graphics, _, new Color(0, 255, 255)))

//					outlineRenderer.drawOutline(n, 3, Color.RED, 2)
				})

			fireStatues.toList
				.foreach(go => {
					val animStrOpt = go.animationOpt.map(anima => s"Animation(id=${anima.getId}, duration=${anima.getDuration}, numFrames=${anima.getNumFrames}, step=${anima.getFrameStep})")
					renderGameObjectOverlay(go, s"${go.getId}${animStrOpt.map(x => x.prependedAll(": ")).getOrElse("")}")(2, 1, animStrOpt.fold(Color.GREEN)(_ => Color.ORANGE), false)
				})
			null
		}
	}
	private object HallowedSepulchrePanel extends OverlayPanel(plugin) {
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT)
		setLayer(OverlayLayer.ABOVE_WIDGETS)
		type LineData = (String, String) | String
		private def lines: Seq[LineData] = {
			val floorLine: LineData = "Floor" -> s"${currentFloor}"
			val tickRemainingLine: LineData = "Remaining" -> s"${timeRemaining}"
			val npcLines  : Seq[LineData] = npcs.toList.map[LineData] {
				case (n) => (s"${n.getIndex}", s"${n.getId} ${n.getWorldArea.pipe(a=>(a.getX, a.getY, a.getWidth, a.getHeight))}")
			}.prepended("Projectiles")
			val statueLines   : Seq[LineData] = fireStatues.toList.map[LineData] {
				case (o) => s"${o.getId}@${o.templateLocation}" -> o.animationOpt.map(anima => s"Animation(id=${anima.getId}, duration=${anima.getDuration}, numFrames=${anima.getNumFrames}, step=${anima.getFrameStep})").getOrElse("None")
				//			case (processType, brew) =>
				//			case (, idx) => s"Order ${idx + 1}" -> s"${pt} ${br}"
			}.filter{
				case (_: String, s: String) => !(s.isEmpty || s.contentEquals("None"))
				case _ => true
			}.prepended("FireStatues")
			List[LineData | Seq[LineData]](floorLine, tickRemainingLine, npcLines, statueLines).flatMap {
				case a: LineData => Seq(a)
				case b: Seq[LineData] => b
			}
		}

		override def render(graphics: Graphics2D): Dimension = {
			//		if (plugin.inLab) {
			List[LayoutableRenderableEntity | Seq[LayoutableRenderableEntity]](
				TitleComponent.builder.text(s"${name}Panel").color(Color.GREEN).build,
				lines.map {
					case (left, right) => LineComponent.builder.left(left).right(right).build
					case line: String => TitleComponent.builder.text(line).build
				}
			).flatMap {
				case x: LayoutableRenderableEntity => List(x)
				case x: Seq[LayoutableRenderableEntity] => x
			}.foreach(u => panelComponent.getChildren.add(u))
			super.render(graphics)
		}
	}
}
