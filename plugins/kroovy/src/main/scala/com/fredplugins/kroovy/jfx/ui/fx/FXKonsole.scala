package com.fredplugins.kroovy.jfx.ui.fx

import com.fredplugins.common.utils.ShimUtils

import java.io.{PrintStream, PrintWriter, StringWriter}
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.{Menu, MenuBar, SeparatorMenuItem, SplitPane}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.BorderPane
import net.runelite.api.widgets.WidgetInfo
import net.runelite.api.{EnumComposition, ItemID, MenuAction, MenuEntry, NPC, NpcID, ObjectID, Player, PlayerComposition, Projectile}
import net.runelite.client.config.ConfigManager
import com.fredplugins.kroovy.KroovyPlugin
import com.fredplugins.kroovy.jfx.{KFXSceneProvider, KResource, MenuCheckBox}
import com.fredplugins.kroovy.jfx.ui.fx.GroovyCodeArea
import com.fredplugins.kroovy.jfx.utils.StreamCapturer
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea

import scala.collection.mutable

class FXKonsole(val plugin: KroovyPlugin) extends KFXSceneProvider with ShimUtils.Logging {
	implicit class CaseInsensitiveRegex(sc: StringContext) {
		def ci = ( "(?i)" + sc.parts.mkString ).r
	}
	val itemIdMap: mutable.Map[Int, String] = mutable.Map.empty
	classOf[ItemID].getDeclaredFields.foreach(field => itemIdMap.put(field.getInt(null), s"ItemID.${field.getName}"))
	val npcIdMap: mutable.Map[Int, String] = mutable.Map.empty
	classOf[NpcID].getDeclaredFields.foreach(field => npcIdMap.put(field.getInt(null), s"NpcID.${field.getName}"))
	val objectIdMap: mutable.Map[Int, String] = mutable.Map.empty
	classOf[ObjectID].getDeclaredFields.foreach(field =>
		objectIdMap.put(field.getInt(null), s"ObjectID.${field.getName}")
	)
	val itemOpcodes: Seq[Int] = Seq(
		MenuAction.ITEM_USE_ON_GROUND_ITEM.getId,
		MenuAction.WIDGET_TARGET_ON_GROUND_ITEM.getId,
		MenuAction.GROUND_ITEM_FIRST_OPTION.getId,
		MenuAction.GROUND_ITEM_SECOND_OPTION.getId,
		MenuAction.GROUND_ITEM_THIRD_OPTION.getId,
		MenuAction.GROUND_ITEM_FOURTH_OPTION.getId,
		MenuAction.GROUND_ITEM_FIFTH_OPTION.getId,
		MenuAction.ITEM_USE_ON_ITEM.getId,
		MenuAction.ITEM_FIRST_OPTION.getId,
		MenuAction.ITEM_SECOND_OPTION.getId,
		MenuAction.ITEM_THIRD_OPTION.getId,
		MenuAction.ITEM_FOURTH_OPTION.getId,
		MenuAction.ITEM_FIFTH_OPTION.getId,
		MenuAction.ITEM_USE.getId
	)
	val widgetOpcodes: Seq[Int] = Seq(
		MenuAction.WIDGET_TYPE_1.getId,
		MenuAction.WIDGET_TARGET.getId,
		MenuAction.WIDGET_CLOSE.getId,
		MenuAction.WIDGET_TYPE_4.getId,
		MenuAction.WIDGET_TYPE_5.getId,
		MenuAction.WIDGET_CONTINUE.getId,
		MenuAction.ITEM_USE_ON_ITEM.getId,
		MenuAction.WIDGET_FIRST_OPTION.getId,
		MenuAction.WIDGET_SECOND_OPTION.getId,
		MenuAction.WIDGET_THIRD_OPTION.getId,
		MenuAction.WIDGET_FOURTH_OPTION.getId,
		MenuAction.WIDGET_FIFTH_OPTION.getId,
		MenuAction.CC_OP.getId,
		MenuAction.CC_OP_LOW_PRIORITY.getId
	)

	var previousCommands: mutable.Buffer[String] = mutable.ListBuffer(null)
	var previousCommandIndex = 0;

	val configManager: ConfigManager = plugin.configManager

	val borderPane = new BorderPane()
	val _scene = new Scene(borderPane, 800, 600)
	_scene.getStylesheets.add(classOf[KroovyPlugin].getResource("konsole.css").toExternalForm)

	val output = new CodeArea()
	output.setEditable(false)
	output.getStylesheets.add(classOf[KroovyPlugin].getResource("konsole.css").toExternalForm)
	val outputScrollPane = new VirtualizedScrollPane[CodeArea](output)
	val input = new GroovyCodeArea("")
	input.getStylesheets.add(KResource.string("GroovyKeywords.css"))
	val inputScrollPane = new VirtualizedScrollPane[GroovyCodeArea](input)

	val menuBar = new MenuBar
	val loggingMenu = new Menu("Logging")
	val showTimestamps: MenuCheckBox = MenuCheckBox("Show Timestamps", "konsoleshowtimestamps", configManager, loggingMenu, selectedByDefault = true)
	val alwaysOnTop: MenuCheckBox = MenuCheckBox("Always On Top", "konsolealwaysontop", configManager, loggingMenu,
		selectedByDefault = true)
	alwaysOnTop.onEvent = () => this.onFXThreadLaterThunk()
	loggingMenu.getItems.add(new SeparatorMenuItem)
	val systemMenu = new Menu("System")
	val showSystemOut: MenuCheckBox = MenuCheckBox("Show System.out", "konsoleshowsystemout", configManager, systemMenu)
	val showSystemErr: MenuCheckBox = MenuCheckBox("Show System.err", "konsolesyhowsystemerr", configManager, systemMenu)
	loggingMenu.getItems.add(systemMenu)

	loggingMenu.getItems.add(new SeparatorMenuItem)
	val objectFormatMenu = new Menu("Object Formatting")

	//<editor-fold desc="playerFormatMenu">

	val playerFormatMenu = new Menu("Player")
	val playerActionFrame: MenuCheckBox = MenuCheckBox("actionFrame (int)", "playeractionframe", configManager, playerFormatMenu)
	val playerAnimation: MenuCheckBox = MenuCheckBox("animation (int)", "playeranimation", configManager, playerFormatMenu)
	val playerAnimationIdleTicks: MenuCheckBox = MenuCheckBox("animationIdleTicks (int)", "playeranimationidleticks", configManager, playerFormatMenu)
	val playerCombatLevel: MenuCheckBox = MenuCheckBox("combatLevel (int)", "playercombatlevel", configManager, playerFormatMenu)
	val playerCurrentOrientation: MenuCheckBox = MenuCheckBox("currentOrientation (int)", "playercurrentorientation", configManager, playerFormatMenu)
	val playerHealthRatio: MenuCheckBox = MenuCheckBox("healthRatio (int)", "playerhealthratio", configManager, playerFormatMenu)
	val playerHealthScale: MenuCheckBox = MenuCheckBox("healthScale (int)", "playername", configManager, playerFormatMenu)
	val playerIdlePoseAnimation: MenuCheckBox = MenuCheckBox("idlePoseAnimation (int)", "playeridleposeanimation", configManager, playerFormatMenu)
	val playerInteracting: MenuCheckBox = MenuCheckBox("interacting (Actor)", "playerinteracting", configManager, playerFormatMenu)
	val playerLastAnimation: MenuCheckBox = MenuCheckBox("lastAnimation (int)", "playerlastanimation", configManager, playerFormatMenu)
	val playerLastInteracting: MenuCheckBox = MenuCheckBox("lastInteracting (Actor)", "playerlastinteracting", configManager, playerFormatMenu)
	val playerLastLocation: MenuCheckBox = MenuCheckBox("lastLocation (WorldPoint)", "playerlastlocation", configManager, playerFormatMenu)
	val playerLocalLocation: MenuCheckBox = MenuCheckBox("localLocation (LocalPoint)", "playerlocallocation", configManager, playerFormatMenu)
	val playerMovementIdleTicks: MenuCheckBox = MenuCheckBox("movementIdleTicks (int)", "playermovementidleticks", configManager, playerFormatMenu)
	val playerIsMoving: MenuCheckBox = MenuCheckBox("isMoving (boolean)", "playermoving", configManager, playerFormatMenu)
	val playerName: MenuCheckBox = MenuCheckBox("name (String)", "playername", configManager, playerFormatMenu, selectedByDefault = true)
	val playerOrientation: MenuCheckBox = MenuCheckBox("orientation (int)", "playerorientation", configManager, playerFormatMenu)
	val playerOverheadText: MenuCheckBox = MenuCheckBox("overheadText (String)", "playeroverheadtext", configManager, playerFormatMenu)
	val playerPoseAnimation: MenuCheckBox = MenuCheckBox("poseAnimation (int)", "playerposeanimation", configManager, playerFormatMenu)
	val playerSpotAnimation: MenuCheckBox = MenuCheckBox("spotAnimation (int)", "playerspotanimation", configManager, playerFormatMenu)
	val playerWorldArea: MenuCheckBox = MenuCheckBox("worldArea (WorldArea)", "playerworldarea", configManager, playerFormatMenu)
	val playerWorldLocation: MenuCheckBox = MenuCheckBox("worldLocation (WorldPoint)", "playerworldlocation", configManager, playerFormatMenu)
	playerFormatMenu.getItems.add(new SeparatorMenuItem)
	val playerActions: MenuCheckBox = MenuCheckBox("actions (String[])", "playeractions", configManager, playerFormatMenu)
	val playerIsFriend: MenuCheckBox = MenuCheckBox("isFriend (boolean)", "playerisfriend", configManager, playerFormatMenu)
	val playerIsFriendsChatMember: MenuCheckBox =
		MenuCheckBox("isFriendsChatMember (boolean)", "playerisfriendschatmember", configManager, playerFormatMenu)
	val playerOverheadIcon: MenuCheckBox = MenuCheckBox("overheadIcon (HeadIcon)", "playeroverheadicon", configManager, playerFormatMenu)
	val playerAppearance: MenuCheckBox = MenuCheckBox("playerAppearance (PlayerAppearance)", "playerplayerappearance", configManager, playerFormatMenu)
	val playerId: MenuCheckBox = MenuCheckBox("playerId (int)", "playerid", configManager, playerFormatMenu, selectedByDefault = true)
	val playerSkullIcon: MenuCheckBox = MenuCheckBox("skullIcon (SkullIcon)", "playerskullicon", configManager, playerFormatMenu)
	val playerTeam: MenuCheckBox = MenuCheckBox("team (int)", "playerteam", configManager, playerFormatMenu)
	objectFormatMenu.getItems.add(playerFormatMenu)

	//</editor-fold>

	//region npcFormatMenu

	val npcFormatMenu = new Menu("NPC")
	val npcActionFrame: MenuCheckBox = MenuCheckBox("actionFrame (int)", "konsolenpcactionframe", configManager, npcFormatMenu)
	val npcAnimation: MenuCheckBox = MenuCheckBox("animation (int)", "konsolenpcanimation", configManager, npcFormatMenu)
	val npcAnimationIdleTicks: MenuCheckBox = MenuCheckBox("animationIdleTicks (int)", "konsolenpcanimationidleticks",
		configManager, npcFormatMenu)
	val npcCombatLevel: MenuCheckBox = MenuCheckBox("combatLevel (int)", "konsolenpccombatlevel", configManager, npcFormatMenu)
	val npcCurrentOrientation: MenuCheckBox = MenuCheckBox("currentOrientation (int)", "konsolenpccurrentorientation",
		configManager, npcFormatMenu)
	val npcHealthRatio: MenuCheckBox = MenuCheckBox("healthRatio (int)", "konsolenpchealthratio", configManager, npcFormatMenu)
	val npcHealthScale: MenuCheckBox = MenuCheckBox("healthScale (int)", "konsolenpcname", configManager, npcFormatMenu)
	val npcIdlePoseAnimation: MenuCheckBox = MenuCheckBox("idlePoseAnimation (int)", "konsolenpcidleposeanimation", configManager, npcFormatMenu)
	val npcInteracting: MenuCheckBox = MenuCheckBox("interacting (Actor)", "konsolenpcinteracting", configManager, npcFormatMenu)
	val npcLastAnimation: MenuCheckBox = MenuCheckBox("lastAnimation (int)", "konsolenpclastanimation", configManager, npcFormatMenu)
	val npcLastInteracting: MenuCheckBox = MenuCheckBox("lastInteracting (Actor)", "konsolenpclastinteracting", configManager, npcFormatMenu)
	val npcLastLocation: MenuCheckBox = MenuCheckBox("lastLocation (WorldPoint)", "konsolenpclastlocation", configManager, npcFormatMenu)
	val npcLocalLocation: MenuCheckBox = MenuCheckBox("localLocation (LocalPoint)", "konsolenpclocallocation", configManager, npcFormatMenu)
	val npcMovementIdleTicks: MenuCheckBox = MenuCheckBox("movementIdleTicks (int)", "konsolenpcmovementidleticks", configManager, npcFormatMenu)
	val npcIsMoving: MenuCheckBox = MenuCheckBox("isMoving (boolean)", "konsolenpcmoving", configManager, npcFormatMenu)
	val npcName: MenuCheckBox = MenuCheckBox("name (String)", "konsolenpcname", configManager, npcFormatMenu, selectedByDefault = true)
	val npcOrientation: MenuCheckBox = MenuCheckBox("orientation (int)", "konsolenpcorientation", configManager, npcFormatMenu)
	val npcOverheadText: MenuCheckBox = MenuCheckBox("overheadText (String)", "konsolenpcoverheadtext", configManager, npcFormatMenu)
	val npcPoseAnimation: MenuCheckBox = MenuCheckBox("poseAnimation (int)", "konsolenpcposeanimation", configManager, npcFormatMenu)
	val npcSpotAnimation: MenuCheckBox = MenuCheckBox("spotAnimation (int)", "konsolenpcspotanimation", configManager, npcFormatMenu)
	val npcWorldArea: MenuCheckBox = MenuCheckBox("worldArea (WorldArea)", "konsolenpcworldarea", configManager, npcFormatMenu)
	val npcWorldLocation: MenuCheckBox = MenuCheckBox("worldLocation (WorldPoint)", "konsolenpcworldlocation", configManager, npcFormatMenu)
	npcFormatMenu.getItems.add(new SeparatorMenuItem)
	val npcId: MenuCheckBox = MenuCheckBox("id (int)", "konsolenpcid", configManager, npcFormatMenu, selectedByDefault = true)
	val npcIndex: MenuCheckBox = MenuCheckBox("index (int)", "konsolenpcindex", configManager, npcFormatMenu)
	val npcDefinition: MenuCheckBox = MenuCheckBox("definition (NPCDefinition)", "konsolenpcdefinition", configManager, npcFormatMenu)
	val npcTransformedDefinition: MenuCheckBox = MenuCheckBox("transformedDefinition (NPCDefinition)",
		"konsolenpctransformeddefinition", configManager, npcFormatMenu)
	val npcIsDdad: MenuCheckBox = MenuCheckBox("isDead (boolean)", "konsolenpcisdead", configManager, npcFormatMenu)
	objectFormatMenu.getItems.add(npcFormatMenu)
	//endregion

	//region playerAppearanceFormatMenu

	val playerAppearanceFormatMenu = new Menu("PlayerAppearance")
	val playerAppearanceEquipmentIds: MenuCheckBox =
		MenuCheckBox("equipmentIds (int[])", "konsoleplayerappearanceequipmentids", configManager, playerAppearanceFormatMenu)
	val playerAppearanceEquippedItemIds: MenuCheckBox =
		MenuCheckBox("equippedItemIds (List<Integer>)", "konsoleplayerappearanceequippeditemids", configManager, playerAppearanceFormatMenu, selectedByDefault = true)
	val playerAppearanceIsFemale: MenuCheckBox = MenuCheckBox("isFemale (boolean)", "konsoleplayerappearanceisfemale", configManager, playerAppearanceFormatMenu)
	objectFormatMenu.getItems.add(playerAppearanceFormatMenu)
	//endregion

	val menuEntryFormatMenu = new Menu("MenuEntry")
	val menuEntryOption: MenuCheckBox = MenuCheckBox("option (string)", "konsolemenuentryoption", configManager, menuEntryFormatMenu)
	val menuEntryTarget: MenuCheckBox = MenuCheckBox("target (string)", "konsolemenuentryoption", configManager, menuEntryFormatMenu)
	val menuEntryOpcode: MenuCheckBox =
		MenuCheckBox("opcode (int)", "konsolemenuentryopcode", configManager, menuEntryFormatMenu, selectedByDefault = true)
	val menuEntryIdentifier: MenuCheckBox = MenuCheckBox("identifier (int)", "konsolemenuentryidentifier", configManager, menuEntryFormatMenu, selectedByDefault = true)
	val menuEntryParam0: MenuCheckBox = MenuCheckBox("param0 (int)", "konsolemenuentryparam0", configManager, menuEntryFormatMenu, selectedByDefault = true)
	val menuEntryParam1: MenuCheckBox = MenuCheckBox("param1 (int)", "konsolemenuentryparam1", configManager, menuEntryFormatMenu, selectedByDefault = true)
	val menuEntryForceLeftClick: MenuCheckBox =
		MenuCheckBox("forceLeftClick (boolean)", "konsolemenuentryforceleftclick", configManager, menuEntryFormatMenu)
	val menuEntryMenuOpcode: MenuCheckBox =
		MenuCheckBox("menuOpcode (MenuOpcode)", "konsolemenuentrymenuopcode", configManager, menuEntryFormatMenu)
	objectFormatMenu.getItems.add(menuEntryFormatMenu)


	val enumDefinitionFormatMenu = new Menu("EnumDefinition")
	val enumDefinitionIntVals: MenuCheckBox = MenuCheckBox("intVals (int[])", "konsoleenumdefinitionintvals", configManager,
		enumDefinitionFormatMenu)
	val enumDefinitionKeys: MenuCheckBox = MenuCheckBox("keys (int[])", "konsoleenumdefinitionkeys", configManager, enumDefinitionFormatMenu)
	val enumDefinitionStringVals: MenuCheckBox = MenuCheckBox("stringVals (String[])", "konsoleenumdefinitionstringvals",
		configManager, enumDefinitionFormatMenu)
	objectFormatMenu.getItems.add(enumDefinitionFormatMenu)

	val varbitDefinitionFormatMenu = new Menu("VarbitDefinition")
	val varbitDefinitionIndex: MenuCheckBox = MenuCheckBox("index (int)", "konsolevarbitdefinitionindex", configManager,
		varbitDefinitionFormatMenu)
	val varbitLeastSignificantBit: MenuCheckBox = MenuCheckBox("leastSignificantBit (int)",
		"konsolevarbitdefinitionleastsignificantbit",
		configManager,
		varbitDefinitionFormatMenu)
	val varbitMostSignificantBit: MenuCheckBox = MenuCheckBox("index (int)", "konsolevarbitdefinitionmostsignificantbit",
		configManager, varbitDefinitionFormatMenu)
	objectFormatMenu.getItems.add(varbitDefinitionFormatMenu)

	val projectileFormatMenu = new Menu("Projectile")

	val projectileEndCycle: MenuCheckBox = MenuCheckBox("endCycle (int)", "konsoleprojectileendcycle", configManager, projectileFormatMenu)
	val projectileEndHeight: MenuCheckBox = MenuCheckBox("endHeight (int)", "konsoleprojectileendheight", configManager, projectileFormatMenu)
	val projectileFloor: MenuCheckBox = MenuCheckBox("floor (int)", "konsoleprojectilefloor", configManager, projectileFormatMenu)
	val projectileHeight: MenuCheckBox = MenuCheckBox("height (int)", "konsoleprojectileheight", configManager, projectileFormatMenu)
	val projectileId: MenuCheckBox = MenuCheckBox("id (int)", "konsoleprojectileid", configManager, projectileFormatMenu, selectedByDefault = true)
	val projectileInteracting: MenuCheckBox = MenuCheckBox("interacting (Actor)", "konsoleprojectileinteracting", configManager, projectileFormatMenu, selectedByDefault = true)
	val projectileRemainingCycles: MenuCheckBox = MenuCheckBox("remainingCycles (int)", "konsoleprojectileremainingcycles", configManager, projectileFormatMenu)
	val projectileScalar: MenuCheckBox = MenuCheckBox("scalar (double)", "konsoleprojectilescalar", configManager, projectileFormatMenu)
	val projectileSlope: MenuCheckBox = MenuCheckBox("slope (int)", "konsoleprojectileslope", configManager, projectileFormatMenu)
	val projectileSpawnLocation: MenuCheckBox = MenuCheckBox("spawnLocation (WorldPoint)", "konsoleprojectilespawnlocation", configManager, projectileFormatMenu)
	val projectileStartHeight: MenuCheckBox = MenuCheckBox("startHeight (int)", "konsoleprojectilestartheight", configManager, projectileFormatMenu)
	val projectileStartMovementCycle: MenuCheckBox = MenuCheckBox("startMovementCycle (int)", "konsoleprojectilestartmovementcycle",	configManager, projectileFormatMenu)
	val projectileVelocityX: MenuCheckBox = MenuCheckBox("velocityX (double)", "konsoleprojectilevelocityx", configManager, projectileFormatMenu)
	val projectileVelocityY: MenuCheckBox = MenuCheckBox("velocityY (double)", "konsoleprojectilevelocityy", configManager, projectileFormatMenu)
	val projectileVelocityZ: MenuCheckBox = MenuCheckBox("velocityZ (double)", "konsoleprojectilevelocityz", configManager, projectileFormatMenu)
	val projectileX1: MenuCheckBox = MenuCheckBox("x1 (int)", "konsoleprojectilex1", configManager, projectileFormatMenu)
	val projectileX: MenuCheckBox = MenuCheckBox("x (double)", "konsoleprojectilex", configManager, projectileFormatMenu)
	val projectileY1: MenuCheckBox = MenuCheckBox("y1 (int)", "konsoleprojectiley1", configManager, projectileFormatMenu)
	val projectileY: MenuCheckBox = MenuCheckBox("y (double)", "konsoleprojectiley", configManager, projectileFormatMenu)
	val projectileZ: MenuCheckBox = MenuCheckBox("z (double)", "konsoleprojectilez", configManager, projectileFormatMenu)

	objectFormatMenu.getItems.add(projectileFormatMenu)

	val tileObjectFormatMenu = new Menu("TileObject")
	objectFormatMenu.getItems.add(tileObjectFormatMenu)
	val decorativeObjectFormatMenu = new Menu("DecorativeObject")
	objectFormatMenu.getItems.add(decorativeObjectFormatMenu)
	val gameObjectObjectFormatMenu = new Menu("GameObject")
	objectFormatMenu.getItems.add(gameObjectObjectFormatMenu)
	val graphicsObjectFormatMenu = new Menu("GraphicsObject")
	objectFormatMenu.getItems.add(graphicsObjectFormatMenu)
	val groundObjectFormatMenu = new Menu("GroundObject")
	objectFormatMenu.getItems.add(groundObjectFormatMenu)
	val tileFormatMenu = new Menu("Tile")
	objectFormatMenu.getItems.add(tileFormatMenu)
	val tileItemFormatMenu = new Menu("TileItem")
	objectFormatMenu.getItems.add(tileItemFormatMenu)
	val tileItemPileFormatMenu = new Menu("TileItemPile")
	objectFormatMenu.getItems.add(tileItemPileFormatMenu)
	val wallObjectFormatMenu = new Menu("WallObject")
	objectFormatMenu.getItems.add(wallObjectFormatMenu)
	val itemDefinitionFormatMenu = new Menu("ItemDefinition")
	objectFormatMenu.getItems.add(itemDefinitionFormatMenu)
	val npcDefinitionFormatMenu = new Menu("NPCDefinition")
	objectFormatMenu.getItems.add(npcDefinitionFormatMenu)
	val objectDefinitionFormatMenu = new Menu("ObjectDefinition")
	objectFormatMenu.getItems.add(objectDefinitionFormatMenu)

	loggingMenu.getItems.add(objectFormatMenu)
	loggingMenu.getItems.add(new SeparatorMenuItem)

	val logEventsMenu = new Menu("Events")


	menuBar.getMenus.add(loggingMenu)
	borderPane.setTop(menuBar)

	val capturedSysOut: StreamCapturer = new StreamCapturer(System.out, (s: String) => {
		showSystemOut.doIfSelected(() => onFXThreadLaterThunk(() => output.append(s, "sysout")))
	})
	System.setOut(new PrintStream(capturedSysOut))
	val capturedSysErr: StreamCapturer = new StreamCapturer(System.err, (s: String) => {
		showSystemErr.doIfSelected(() => onFXThreadLaterThunk(() => output.append(s, "err")))
	})
	System.setErr(new PrintStream(capturedSysErr))
	//val customAppender

	input.addEventHandler(KeyEvent.KEY_PRESSED, (e: KeyEvent) => {
		e match {
			case enter if (enter.getCode == KeyCode.ENTER && !enter.isShiftDown) =>
				e.consume()
				val trimmed = input.getText().trim
				trimmed match {
					case ci"clear" =>
						previousCommands = mutable.ListBuffer(null)
						previousCommandIndex = 0
						output.clear()
						input.clear()
					case s if s != "" =>
						showTimestamps.doIfSelected(() => {
							val timeString = LocalTime.now.truncatedTo(ChronoUnit.MILLIS).toString
							output.append("[" + timeString + "] ", "timestamp")
						})
						output.append(trimmed + "\n", "input")
						var result: Object = null
						try {
							result = formatObject(plugin.kManager.global.runtime(input.getText))
							if (result != null) output.append(formatObject(result) + "\n", "output")
						}
						catch {
							case e: Exception =>
								val stackTraceWriter = new StringWriter
								e.printStackTrace(new PrintWriter(stackTraceWriter))
								output.append(stackTraceWriter.toString, "err")
						}
						previousCommands.+=(trimmed)
						previousCommandIndex = previousCommands.indices.last
						input.clear()
					case _ =>
				}
			case pgUp if (pgUp.getCode == KeyCode.PAGE_UP) =>
				pgUp.consume()
				val trimmed = input.getText().trim
				if (trimmed == "") previousCommandIndex = previousCommands.indices.last
				val command = Option(previousCommands(previousCommandIndex))
				command.foreach(c => {
					input.clear()
					input.appendText(c)
					if (previousCommandIndex > 0) previousCommandIndex -= 1
				})
			case pgDown if (pgDown.getCode == KeyCode.PAGE_DOWN) =>
				pgDown.consume()
				if (previousCommandIndex <  previousCommands.indices.last - 1) {
					previousCommandIndex += 1
					input.clear()
					input.appendText(previousCommands(previousCommandIndex + 1))
				}
			case _ =>
		}
	})

	val splitPane = new SplitPane(outputScrollPane, inputScrollPane)
	splitPane.setOrientation(Orientation.VERTICAL)
	splitPane.setDividerPositions(0.80)

	borderPane.setCenter(splitPane)

	override def scene: () => Scene = () => _scene

	def formatObject(obj: Object): String = {
		obj match {
			case null => null
			case player: Player => formatPlayer(player)
			case npc: NPC => formatNpc(npc)
			case playerAppearance: PlayerComposition => formatPlayerAppearance(playerAppearance)
			case menuEntry: MenuEntry => formatMenuEntry(menuEntry)
			case enumDefinition: EnumComposition => formatEnumDefinition(enumDefinition)
			case projectile: Projectile => formatProjectile(projectile)
			case _ => obj.toString
		}
	}

	def formatPlayer(player: Player): String = {
		val params = mutable.ListBuffer.empty[String]
//		playerActionFrame.doIfSelected(() => params.+=(s"actionFrame = ${player.getActionFrame}"))
		playerAnimation.doIfSelected(() => params.+=(s"animation = ${player.getAnimation}"))
//		playerAnimationIdleTicks.doIfSelected(() => params.+=(s"animationIdleTicks = ${player.getAnimationIdleTicks}"))
		playerCombatLevel.doIfSelected(() => params.+=(s"combatLevel = ${player.getCombatLevel}"))
		playerCurrentOrientation.doIfSelected(() => params.+=(s"currentOrientation = ${player.getCurrentOrientation}"))
		playerHealthRatio.doIfSelected(() => params.+=(s"healthRatio = ${player.getHealthRatio}"))
		playerHealthScale.doIfSelected(() => params.+=(s"healthScale = ${player.getHealthScale}"))
		playerIdlePoseAnimation.doIfSelected(() => params.+=(s"idlePoseAnimation = ${player.getIdlePoseAnimation}"))
		playerInteracting.doIfSelected(() => params.+=(s"interacting = ${formatObject(player.getInteracting)}"))
//		playerLastAnimation.doIfSelected(() => params.+=(s"lastAnimation = ${player.getLastAnimation}"))
//		playerLastInteracting.doIfSelected(() => params.+=(s"lastInteracting = ${formatObject(player.getLastInteracting)}"))
//		playerLastLocation.doIfSelected(() => params.+=(s"lastLocation = ${player.getLastLocation}"))
		playerLocalLocation.doIfSelected(() => params.+=(s"localLocation = ${player.getLocalLocation}"))
//		playerMovementIdleTicks.doIfSelected(() => params.+=(s"movementIdleTicks = ${player.getMovementIdleTicks}"))
//		playerIsMoving.doIfSelected(() => params.+=(s"isMoving = ${player.isMoving}"))
		playerName.doIfSelected(() => params.+=(s"""name = \"${player.getName}\""""))
		playerOrientation.doIfSelected(() => params.+=(s"orientation = ${player.getOrientation}"))
		playerOverheadText.doIfSelected(() => params.+=(s"""overheadText = \"${player.getOverheadText}\""""))
		playerPoseAnimation.doIfSelected(() => params.+=(s"poseAnimation = ${player.getPoseAnimation}"))
		playerSpotAnimation.doIfSelected(() => params.+=(s"spotAnimation = ${player.getGraphic}"))
		playerWorldArea.doIfSelected(() => params.+=(s"worldArea = ${player.getWorldArea}"))
		playerWorldLocation.doIfSelected(() => params.+=(s"worldLocation = ${player.getWorldLocation}"))
//		playerActions.doIfSelected(() => params.+=(s"actions = ${player.getActions.mkString("[", ", ", "]")}"))
		playerIsFriend.doIfSelected(() => params.+=(s"isFriend = ${player.isFriend}"))
		playerIsFriendsChatMember.doIfSelected(() => params.+=(s"isFriendsChatMember = ${player.isFriendsChatMember}"))
		playerOverheadIcon.doIfSelected(() => params.+=(s"overheadIcon = ${player.getOverheadIcon}"))
		playerAppearance.doIfSelected(() => params.+=(s"playerAppearance = ${formatObject(player.getPlayerComposition)}"))
//		playerId.doIfSelected(() => params.+=(s"playerId = ${player.getPlayerId}"))
		playerSkullIcon.doIfSelected(() => params.+=(s"skullIcon = ${player.getSkullIcon}"))
		playerTeam.doIfSelected(() => params.+=(s"team = ${player.getTeam}"))
		if (params.isEmpty) player.toString
		params.mkString("Player(", ", ", ")")
	}

	def formatNpc(npc: NPC): String = {
		val params = mutable.ListBuffer.empty[String]
//		npcActionFrame.doIfSelected(() => params.+=(s"actionFrame = ${npc.getActionFrame}"))
		npcAnimation.doIfSelected(() => params.+=(s"animation = ${npc.getAnimation}"))
//		npcAnimationIdleTicks.doIfSelected(() => params.+=(s"animationIdleTicks = ${npc.getAnimationIdleTicks}"))
		npcCombatLevel.doIfSelected(() => params.+=(s"combatLevel = ${npc.getCombatLevel}"))
		npcCurrentOrientation.doIfSelected(() => params.+=(s"currentOrientation = ${npc.getCurrentOrientation}"))
		npcHealthRatio.doIfSelected(() => params.+=(s"healthRatio = ${npc.getHealthRatio}"))
		npcHealthScale.doIfSelected(() => params.+=(s"healthScale = ${npc.getHealthScale}"))
		npcIdlePoseAnimation.doIfSelected(() => params.+=(s"idlePoseAnimation = ${npc.getIdlePoseAnimation}"))
		npcInteracting.doIfSelected(() => params.+=(s"interacting = ${formatObject(npc.getInteracting)}"))
//		npcLastAnimation.doIfSelected(() => params.+=(s"lastAnimation = ${npc.getLastAnimation}"))
//		npcLastInteracting.doIfSelected(() => params.+=(s"lastInteracting = ${formatObject(npc.getLastInteracting)}"))
//		npcLastLocation.doIfSelected(() => params.+=(s"lastLocation = ${npc.getLastLocation}"))
		npcLocalLocation.doIfSelected(() => params.+=(s"localLocation = ${npc.getLocalLocation}"))
//		npcMovementIdleTicks.doIfSelected(() => params.+=(s"movementIdleTicks = ${npc.getMovementIdleTicks}"))
//		npcIsMoving.doIfSelected(() => params.+=(s"isMoving = ${npc.isMoving}"))
		npcName.doIfSelected(() => params.+=(s"""name = \"${npc.getName}\""""))
		npcOrientation.doIfSelected(() => params.+=(s"orientation = ${npc.getOrientation}"))
		npcOverheadText.doIfSelected(() => params.+=(s"""overheadText = \"${npc.getOverheadText}\""""))
		npcPoseAnimation.doIfSelected(() => params.+=(s"poseAnimation = ${npc.getPoseAnimation}"))
		npcSpotAnimation.doIfSelected(() => params.+=(s"spotAnimation = ${npc.getGraphic}"))
		npcWorldArea.doIfSelected(() => params.+=(s"worldArea = ${npc.getWorldArea}"))
		npcWorldLocation.doIfSelected(() => params.+=(s"worldLocation = ${npc.getWorldLocation}"))
		npcId.doIfSelected(() => params.+=(s"id = ${npcIdMap.getOrElse(npc.getId, npc.getId.toString)}"))
		npcIndex.doIfSelected(() => params.+=(s"index = ${npc.getIndex}"))
		npcDefinition.doIfSelected(() => params.+=(s"definition = ${formatObject(npc.getComposition)}"))
		npcTransformedDefinition.doIfSelected(() =>
			params.+=(s"transformedDefinition = ${formatObject(npc.getTransformedComposition)}"))
		npcIsDdad.doIfSelected(() => params.+=(s"isDead = ${npc.isDead}"))
		if (params.isEmpty) npc.toString
		params.mkString("NPC(", ", ", ")")
	}

	def formatPlayerAppearance(playerAppearance: PlayerComposition): String = {
		val params = mutable.ListBuffer.empty[String]
		playerAppearanceEquipmentIds.doIfSelected(() =>
			params.+=(s"equipmentIds = ${playerAppearance.getEquipmentIds.mkString("[", ", ", "]")}")
		)
		playerAppearanceEquippedItemIds.doIfSelected(() => {
			val itemIdStrings = playerAppearance.getEquipmentIds.map(id => itemIdMap.getOrElse(id, id.toString))
			params.+=(s"equippedItemIds = ${itemIdStrings.mkString(", ")}")
		})
		playerAppearanceIsFemale.doIfSelected(() => params.+=(s"isFemale = ${playerAppearance.isFemale}"))
		if (params.isEmpty) playerAppearance.toString
		params.mkString("PlayerAppearance(", ", ", ")")
	}

	def formatMenuEntry(entry: MenuEntry): String = {
		val params = mutable.ListBuffer.empty[String]
		menuEntryOption.doIfSelected(() => params.+=(s"""option = \"${entry.getOption}\""""))
		menuEntryTarget.doIfSelected(() => params.+=(s"""target = \"${entry.getTarget}\""""))
		menuEntryOpcode.doIfSelected(() => params.+=(s"""opcode = \"${getOpcodeString(entry)}\""""))
		menuEntryIdentifier.doIfSelected(() => params.+=(s"""identifier = \"${getIdentifierString(entry)}\""""))
		menuEntryParam0.doIfSelected(() => params.+=(s"param0 = ${entry.getParam0}"))
		menuEntryParam1.doIfSelected(() => params.+=(s"""param1 = \"${getParam1String(entry)}\""""))
		params.mkString("MenuEntry(", ", ", ")")
	}

	def getOpcodeString(entry: MenuEntry): String = {
		if (entry.getType == MenuAction.UNKNOWN) entry.toString
		else s"MenuOpcode.${entry.getType.name()}.id"
	}

	def getIdentifierString(entry: MenuEntry): String = {
		if (itemOpcodes.contains(entry.getType.getId)) itemIdMap.getOrElse(entry.getType.getId, entry.getType.toString)
		entry.getType.toString
	}

	def getParam1String(entry: MenuEntry): String = {
		if (widgetOpcodes.contains(entry.getType.getId)) {
			val widgetInfo = WidgetInfo.values.toStream.find(_.getId == entry.getParam1).orNull
			if (widgetInfo == null) return entry.getParam1.toString
			s"WidgetInfo.${widgetInfo.name()}.id"
		}
		else entry.getParam1.toString
	}

	def formatEnumDefinition(enumDefinition: EnumComposition): String = {
		val params = mutable.ListBuffer.empty[String]
		enumDefinitionIntVals.doIfSelected(() => params.+=(s"""intVals = ${enumDefinition.getIntVals.mkString("Array(", ", ", ")")}"""))
		enumDefinitionKeys.doIfSelected(() => params.+=(s"""keys = ${enumDefinition.getKeys.mkString("Array(", ", ", ")")}"""))
		enumDefinitionStringVals.doIfSelected(() => params.+=(s"""stringVals = ${enumDefinition.getStringVals
		  .toString}"""))
		params.mkString("EnumDefinition(", ", ", ")")
	}

	def formatProjectile(projectile: Projectile): String = {
		val params = mutable.ListBuffer.empty[String]
		projectileEndCycle.doIfSelected(() => params.+=(s"endCycle = ${projectile.getEndCycle}"))
		projectileEndHeight.doIfSelected(() => params.+=(s"endHeight = ${projectile.getEndHeight}"))
		projectileFloor.doIfSelected(() => params.+=(s"floor = ${projectile.getFloor}"))
		projectileHeight.doIfSelected(() => params.+=(s"height = ${projectile.getHeight}"))
		projectileId.doIfSelected(() => params.+=(s"id = ${projectile.getId}"))
		projectileInteracting.doIfSelected(() => params.+=(s"interacting = ${formatObject(projectile.getInteracting)}"))
		projectileRemainingCycles.doIfSelected(() => params.+=(s"remainingCycles = ${projectile.getRemainingCycles}"))
		projectileScalar.doIfSelected(() => params.+=(s"scalar = ${projectile.getScalar}"))
		projectileSlope.doIfSelected(() => params.+=(s"slope = ${projectile.getSlope}"))
		//projectileSpawnLocation.doIfSelected(() => params.+=(s"spawnLocation = ${projectile.get}"))
		projectileStartHeight.doIfSelected(() => params.+=(s"startHeight = ${projectile.getStartHeight}"))
		projectileStartMovementCycle.doIfSelected(() => params.+=(s"startMovementCycle = ${projectile.getStartCycle}"))
		projectileVelocityX.doIfSelected(() => params.+=(s"velocityX = ${projectile.getVelocityX}"))
		projectileVelocityY.doIfSelected(() => params.+=(s"velocityY = ${projectile.getVelocityY}"))
		projectileVelocityZ.doIfSelected(() => params.+=(s"velocityZ = ${projectile.getVelocityZ}"))
		projectileX.doIfSelected(() => params.+=(s"x = ${projectile.getX}"))
		projectileX1.doIfSelected(() => params.+=(s"x1 = ${projectile.getX1}"))//
		projectileY.doIfSelected(() => params.+=(s"y = ${projectile.getY}"))
		projectileY1.doIfSelected(() => params.+=(s"y1 = ${projectile.getY1}"))
		projectileZ.doIfSelected(() => params.+=(s"z = ${projectile.getZ}"))
		if (params.isEmpty) projectile.toString
		params.mkString("Projectile(", ", ", ")")
	}
}
