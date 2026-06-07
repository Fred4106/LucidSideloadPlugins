package com.fredplugins.pvmDebugger

import com.fredplugins.common.extensions.LocationExtensions.given
import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.DarkSquallIds.AgileWarriorNpcId
import com.fredplugins.pvmDebugger.DarkSquallIds.DarkSquallNpc
import com.fredplugins.pvmDebugger.DarkSquallIds.DarkSquallNpcId
import com.fredplugins.pvmDebugger.DarkSquallIds.StrongWarriorNpcId
import com.fredplugins.pvmDebugger.WarriorType.spell
import com.google.inject.Inject
import ethanApiPlugin.lucidplugins.api.utils.InteractionUtils
import ethanApiPlugin.collections.ETileItem
import ethanApiPlugin.collections.Inventory
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.Player
import net.runelite.api.Projectile
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.GameTick
import net.runelite.api.events.ItemDespawned
import net.runelite.api.events.ItemSpawned
import net.runelite.api.events.MenuOptionClicked
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.OverheadTextChanged
import net.runelite.api.events.ProjectileMoved
import net.runelite.api.gameval.InterfaceID
import net.runelite.api.gameval.InterfaceID.MagicSpellbook
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

enum Mode {
	case Standard
	case SummonWarrior
	case ThrowExplosive
	case SpellSurge
}
object Mode {
	def apply(s: String): Mode = {
		Option(s).collect {
			case "Fighting you is beneath me" => SummonWarrior
			case "Magicus ignitus!" => ThrowExplosive
			case "Witness true sorcery!" => SpellSurge
		}.getOrElse(Standard)
	}
}
enum WarriorType(npc: NPC) {
	case Agilty(npc: NPC) extends WarriorType(npc)
	case Heavy(npc: NPC) extends WarriorType(npc)
	def getNpc: NPC = npc
}
object WarriorType {
	def unapply(s: NPC): Option[WarriorType] = {
		Option(s.getId).collect {
			case `StrongWarriorNpcId` => Heavy(s)
			case `AgileWarriorNpcId` => Agilty(s)
		}
	}

	def spell(s: WarriorType): Int = s match {
		case WarriorType.Agilty(_) => InterfaceID.MagicSpellbook.ENTANGLE
		case WarriorType.Heavy(_) => InterfaceID.MagicSpellbook.ENFEEBLE
	}
}
object DarkSquallIds {
	val DarkSquallNpcId    = 13565
	val AgileWarriorNpcId  = 13483
	val StrongWarriorNpcId = 13484

	object DarkSquallNpc {
		def unapply(npc: NPC): Option[NPC] = {
			Option(npc).filter(_.getId == DarkSquallNpcId)
		}
	}


}
case class InternalState(mode: Mode, warriorNpc: Option[WarriorType] = Option.empty, explosive: Option[ETileItem] = Option.empty,
												 projectiles: List[Projectile] = List.empty) {
	def updateMode(nM: Mode): InternalState = this.copy(mode = nM)
	def withWarrior(nm: WarriorType): InternalState = this.copy(warriorNpc = Option(nm))
	def clearWarrior: InternalState = this.copy(warriorNpc = None)
	def withExplosive(e: ETileItem): InternalState  = this.copy(explosive = Option(e))
	def cleanExplosive: InternalState = this.copy(explosive = None)
	def cleanupProjectiles: InternalState = this.copy(projectiles = List.empty[Projectile])
	def addProjectile(p:  Projectile): InternalState = {
		if(!projectiles.contains(p)) {
			this.copy(projectiles = this.projectiles :+ p)
		}
		this
	}
	def removeProjectile(p: Projectile): InternalState = {
		if(projectiles.contains(p)) {
			this.copy(projectiles = this.projectiles.filter(_ != p))
		} else {
			this
		}
	}
}

class DarkSquallHelper (val client: Client) extends ShimUtils.Logging("DEBUG") {
	given Client = client
	var squallNpc: NPC = null
	var lastTickState: InternalState = InternalState(Mode.Standard)
	var curTickState: InternalState = InternalState(Mode.Standard)
	def reset(): Unit = {
		this.squallNpc = null
		this.lastTickState = InternalState(Mode.Standard)
		this.curTickState = InternalState(Mode.Standard)
	}
	@Subscribe
	def onOverheadChanged(event: OverheadTextChanged): Unit = {
		Option(event.getActor). collect {
			case npc: NPC if squallNpc == npc => curTickState.updateMode(Mode(event.getOverheadText))
		}.foreach(curTickState = _)
	}

	@Subscribe
	def onNpcSpawned(event: NpcSpawned): Unit = {
		Option(event.getNpc).collect{
			case DarkSquallNpc(nm) => {
				this.squallNpc = nm
				log.debug(s"Spawned Dark squall ${squallNpc.getIndex -> squallNpc.getId}")
			}
			case WarriorType(nm) => {
				val old = curTickState
				val news  = this.curTickState.withWarrior(nm)
				log.debug(s"Spawned Warrior ${nm}\noldState = ${old}\nnewState = ${news}")
				curTickState = news
			}
		}
	}

	@Subscribe
	def onMenuEntryClicked(event: MenuOptionClicked): Unit = {
		if(squallNpc != null) {
			log.debug(s"clicked: ${event}")
		}
	}

	@Subscribe
	def onNpcDespawned(event:NpcDespawned): Unit = {
		if(squallNpc == event.getNpc) {
			squallNpc = null
		}
		if(curTickState.warriorNpc.exists(_.getNpc == event.getNpc)) {
			curTickState = curTickState.clearWarrior
		}
	}

	@Subscribe
	def onItemSpawned(event: ItemSpawned): Unit = {
		Option(event.getItem).map(t=>t.getId -> t).collect {
			case (29573, t) => ETileItem(event.getTile.getWorldLocation, t)
		}.foreach(u => curTickState = this.curTickState.withExplosive(u))
	}

	@Subscribe
	def onItemDespawned(event: ItemDespawned): Unit = {
		if(event.getItem.getId==29573) {
			curTickState = curTickState.cleanExplosive
		}
	}
	@Subscribe
	private def onProjectileMoved(event: ProjectileMoved): Unit = {
		val projectile   = event.getProjectile
		val projectileId = projectile.getId
		if(projectile.getSourcePoint != null) {
			curTickState = curTickState.addProjectile(projectile)
		}
	}

	@Subscribe
	def onGameTick(gt: GameTick): Unit = {
		curTickState = curTickState.cleanupProjectiles

		if(curTickState.mode == Mode.SummonWarrior && curTickState.warriorNpc.isDefined) {
			curTickState.warriorNpc.foreach(warrior => {
				InteractionUtils.useWidgetOnNPC(client.getWidget(spell(warrior)), warrior.getNpc)
			})
		}
		else if (curTickState.mode == Mode.ThrowExplosive && curTickState.explosive.isDefined) {
			curTickState.explosive.foreach(eitem => {
				val spellWidget = client.getWidget(InterfaceID.MagicSpellbook.TELEGRAB)
				InteractionUtils.useWidgetOnTileItem(spellWidget, eitem)
			})
		} else {
			val inInventoryExplosive = Inventory.search().withId(29573).first().toScala
			inInventoryExplosive.foreach(w => {
				val spellW = MagicSpellbook.HIGH_ALCHEMY.pipe(client.getWidget(_))
				InteractionUtils.useWidgetOnWidget(spellW, w)
			})
		}
		lastTickState = curTickState
	}
}
