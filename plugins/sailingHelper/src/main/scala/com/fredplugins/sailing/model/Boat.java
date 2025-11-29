package com.fredplugins.sailing.model;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.WorldEntity;
import net.runelite.api.gameval.VarbitID;

public class Boat {

	private final int worldViewId;
	private final WorldEntity worldEntity;

	GameObject hull;
	GameObject sail;
	GameObject helm;
	GameObject salvagingHook;
	GameObject cargoHold;

	public Boat(int worldViewId, WorldEntity worldEntity) {
		this.worldViewId = worldViewId;
		this.worldEntity = worldEntity;
	}

	public int getMaxSpeed() {
		HullTier h = getHullTier();
		SizeClass sc = getSizeClass();
		if(h != null && sc != null) {
			return h.getMaxSpeed(sc);
		} else {
			return -1;
		}
	}

	// these are intentionally not cached in case the object is transformed without respawning
	// e.g. helms have a different idle vs in-use id
	public HullTier getHullTier() {
		return hull != null ? HullTier.fromGameObjectId(hull.getId()) : null;
	}

	public SailTier getSailTier() {
		return sail != null ? SailTier.fromGameObjectId(sail.getId()) : null;
	}

	public HelmTier getHelmTier() {
		return helm != null ? HelmTier.fromGameObjectId(helm.getId()) : null;
	}

	public SalvagingHookTier getSalvagingHookTier() {
		return salvagingHook != null ? SalvagingHookTier.fromGameObjectId(salvagingHook.getId()) : null;
	}

	public CargoHoldTier getCargoHoldTier() {
		return cargoHold != null ? CargoHoldTier.fromGameObjectId(cargoHold.getId()) : null;
	}

	public SizeClass getSizeClass() {
		return hull != null ? SizeClass.fromGameObjectId(hull.getId()) : null;
	}

	public int getCargoCapacity(boolean uim) {
		CargoHoldTier cargoHoldTier = getCargoHoldTier();
		if (cargoHoldTier == null) {
			return 0;
		}

		return cargoHoldTier.getCapacity(getSizeClass(), uim);
	}

	public int getCargoCapacity(Client client) {
		return getCargoCapacity(client.getVarbitValue(VarbitID.IRONMAN) == 2);
	}

	public String getDebugString() {
		return String.format(
			"Id: %d, Hull: %s, Sail: %s, Helm: %s, Hook: %s, Cargo: %s",
			worldViewId,
			getHullTier(),
			getSailTier(),
			getHelmTier(),
			getSalvagingHookTier(),
			getCargoHoldTier()
		);
	}

	public WorldEntity getWorldEntity() {
		return this.worldEntity;
	}

	public GameObject getHull() {
		return this.hull;
	}

	public GameObject getSail() {
		return this.sail;
	}

	public GameObject getHelm() {
		return this.helm;
	}

	public GameObject getSalvagingHook() {
		return this.salvagingHook;
	}

	public GameObject getCargoHold() {
		return this.cargoHold;
	}

	public void setHull(GameObject hull) {
		this.hull = hull;
	}

	public void setSail(GameObject sail) {
		this.sail = sail;
	}

	public void setHelm(GameObject helm) {
		this.helm = helm;
	}

	public void setSalvagingHook(GameObject salvagingHook) {
		this.salvagingHook = salvagingHook;
	}

	public void setCargoHold(GameObject cargoHold) {
		this.cargoHold = cargoHold;
	}

	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof Boat)) return false;
		final Boat other = (Boat) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getWorldViewId() != other.getWorldViewId()) return false;
		final Object this$worldEntity = this.getWorldEntity();
		final Object other$worldEntity = other.getWorldEntity();
		if (this$worldEntity == null ? other$worldEntity != null : !this$worldEntity.equals(other$worldEntity))
			return false;
		final Object this$hull = this.getHull();
		final Object other$hull = other.getHull();
		if (this$hull == null ? other$hull != null : !this$hull.equals(other$hull)) return false;
		final Object this$sail = this.getSail();
		final Object other$sail = other.getSail();
		if (this$sail == null ? other$sail != null : !this$sail.equals(other$sail)) return false;
		final Object this$helm = this.getHelm();
		final Object other$helm = other.getHelm();
		if (this$helm == null ? other$helm != null : !this$helm.equals(other$helm)) return false;
		final Object this$salvagingHook = this.getSalvagingHook();
		final Object other$salvagingHook = other.getSalvagingHook();
		if (this$salvagingHook == null ? other$salvagingHook != null : !this$salvagingHook.equals(other$salvagingHook))
			return false;
		final Object this$cargoHold = this.getCargoHold();
		final Object other$cargoHold = other.getCargoHold();
		if (this$cargoHold == null ? other$cargoHold != null : !this$cargoHold.equals(other$cargoHold)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof Boat;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getWorldViewId();
		final Object $worldEntity = this.getWorldEntity();
		result = result * PRIME + ($worldEntity == null ? 43 : $worldEntity.hashCode());
		final Object $hull = this.getHull();
		result = result * PRIME + ($hull == null ? 43 : $hull.hashCode());
		final Object $sail = this.getSail();
		result = result * PRIME + ($sail == null ? 43 : $sail.hashCode());
		final Object $helm = this.getHelm();
		result = result * PRIME + ($helm == null ? 43 : $helm.hashCode());
		final Object $salvagingHook = this.getSalvagingHook();
		result = result * PRIME + ($salvagingHook == null ? 43 : $salvagingHook.hashCode());
		final Object $cargoHold = this.getCargoHold();
		result = result * PRIME + ($cargoHold == null ? 43 : $cargoHold.hashCode());
		return result;
	}

	public String toString() {
		return "Boat(worldViewId=" + this.getWorldViewId() + ", worldEntity=" + this.getWorldEntity() + ", hull=" + this.getHull() + ", sail=" + this.getSail() + ", helm=" + this.getHelm() + ", salvagingHook=" + this.getSalvagingHook() + ", cargoHold=" + this.getCargoHold() + ")";
	}

	public int getWorldViewId() {
		return this.worldViewId;
	}
}