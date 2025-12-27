package com.fred4106.improvedCharges.items.jewelry;

import net.runelite.api.gameval.VarbitID;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItemWithStatus;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.ids.ItemId;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class J_EscapeCrystal extends ChargedItemWithStatus {
    private Instant instantToTeleport = Instant.now();
    private boolean alertedAboutActivation = false;
    private boolean inGauntletWithEscapeCrystal = false;

    public J_EscapeCrystal(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.ESCAPE_CRYSTAL, ItemId.ESCAPE_CRYSTAL, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.ESCAPE_CRYSTAL).quantityCharges().hideOverlay(),
        };

        this.triggers = new TriggerBase[]{
            // Activate / Deactivate.
            new OnVarbitChanged(VarbitID.TELEPORT_CRYSTAL_AFK_MODE).varbitValueConsumer(value -> {
                if (value == 1) {
                    activate();
                } else {
                    deactivate();
                }
            }),

            // Inactivity timer.
            new OnVarbitChanged(VarbitID.TELEPORT_CRYSTAL_AFK_DELAY).varbitValueConsumer((value) -> {
                provider.configManager.setConfiguration(Constants.GROUP, com.fred4106.improvedCharges.Constants.ESCAPE_CRYSTAL_INACTIVITY_PERIOD, value);
            }),

            // Keyboard or mouse actions.
            new OnUserAction().consumer(() -> {
                resetIdleTimer();
            }),

            // Enter Gauntlet detection.
            new OnMenuOptionClicked("Enter", "Enter-corrupted").onMenuTarget("The Gauntlet").consumer(() -> {
                if (provider.store.inventoryContainsItem(ItemId.ESCAPE_CRYSTAL)) {
                    inGauntletWithEscapeCrystal = true;
                } else if (provider.store.equipmentContainsItem(ItemId.ESCAPE_CRYSTAL)) {
                    provider.notifier.notify("Escape crystal disabled, because it was not in the inventory!");
                    inGauntletWithEscapeCrystal = false;
                } else {
                    inGauntletWithEscapeCrystal = false;
                }
            }),

            // Leave Gauntlet detection.
            new OnChatMessage("(You leave the Gauntlet.|Your reward awaits you in the nearby chest.)").consumer(() -> {
                inGauntletWithEscapeCrystal = false;
            }),
        };
    }

    private void resetIdleTimer() {
        instantToTeleport = Instant.now().plusMillis(provider.config.getEscapeCrystalInactivityPeriod() * 600L + 600L);

        provider.store.addConsumerToNextTickQueue(() -> {
            alertedAboutActivation = false;
        });
    }

    private long getTimeRemainingUntilActivation() {
        switch (provider.config.getEscapeCrystalTimeRemainingUnit()) {
            case SECONDS:
                return Math.max(0, (Duration.between(Instant.now(), instantToTeleport).toMillis()) / 1000);
            case TICKS:
            default:
                return Math.max(0, (Duration.between(Instant.now(), instantToTeleport).toMillis()) / 600);
        }
    }

    private boolean isAboutToActivate() {
        return provider.config.getEscapeCrystalTimeRemainingWarning() > 0 && isActivated() && provider.store.isLockedInCombat() && getTimeRemainingUntilActivation() <= provider.config.getEscapeCrystalTimeRemainingWarning();
    }

    @Override
    public boolean inInventory() {
        return super.inInventory() || inGauntletWithEscapeCrystal;
    }

    @Override
    public Color getTextColor(int itemId) {
        return getTotalTextColor();
    }

    @Override
    public Color getTotalTextColor() {
        return isAboutToActivate() ? Color.YELLOW : isActivated() ? provider.config.getColorActivated() : provider.config.getColorEmpty();
    }

    @Override
    public String getChargesString(final int itemId) {
        return getTotalChargesString();
    }

    @Override
    public String getTotalChargesString() {
        if (
            provider.config.getEscapeCrystalStatus() == com.fred4106.improvedCharges.Constants.ItemActivity.DEACTIVATED
            || !inInventoryOrEquipment()
        ) {
            return FredsItemChargesPlugin.getChargesMinified(ChargeId.UNLIMITED);
        }

        if (provider.config.getEscapeCrystalInactivityPeriod() == ChargeId.UNKNOWN) {
            return FredsItemChargesPlugin.getChargesMinified(ChargeId.UNKNOWN);
        }

        final long timeRemainingUntilActivation = getTimeRemainingUntilActivation();
        if (!alertedAboutActivation && isAboutToActivate()) {
            alertedAboutActivation = true;
            provider.notifier.notify("Escape crystal is activating in " + timeRemainingUntilActivation + (provider.config.getEscapeCrystalTimeRemainingUnit() == com.fred4106.improvedCharges.Constants.EscapeCrystalTimeRemainingUnit.SECONDS ? " seconds." : " ticks."));
        }

        switch (provider.config.getEscapeCrystalTimeRemainingUnit()) {
            case SECONDS:
                return timeRemainingUntilActivation / 60 + ":" + String.format("%02d", timeRemainingUntilActivation % 60);
            case TICKS:
            default:
                return String.valueOf(timeRemainingUntilActivation);
        }
    }
}
