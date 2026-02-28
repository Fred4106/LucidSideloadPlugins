package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import net.runelite.api.Skill;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnXpDrop;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class W_BryophytasStaff extends ChargedItem {
    public W_BryophytasStaff(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.BRYOPHYTAS_STAFF, ItemId.BRYOPHYTAS_STAFF, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BRYOPHYTAS_STAFF_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.BRYOPHYTAS_STAFF)
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("The nature staff has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Save a nature rune.
            new OnChatMessage("Your staff saved you a nature rune.").increaseCharges(1),

            // Charge.
            new OnChatMessage("Your Bryophyta's staff now has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Auto-charge.
            new OnChatMessage("The banker charges your Bryophyta's staff using (?<naturerune>.+)x Nature rune.").matcherConsumer(m -> {
                final int natureRunes = Integer.parseInt(m.group("naturerune"));
                increaseCharges(natureRunes);
            }),

            // Regular spellbook.
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Cast").onMenuTarget(
                "Bones to Bananas",
                "Low Level Alchemy",
                "Superheat Item",
                "High Level Alchemy"
            ).decreaseCharges(1),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Make sets").onMenuTarget(
                "Emerald bolts (e)"
            ).decreaseCharges(1),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Cast").onMenuTarget(
                "Bind",
                "Bones to Peaches"
            ).decreaseCharges(2),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Cast").onMenuTarget(
                "Snare"
            ).decreaseCharges(3),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Cast").onMenuTarget(
                "Entangle"
            ).decreaseCharges(4),


            // Arcuus spellbook.
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Cast").onMenuTarget(
                "Dark Lure",
                "Harmony Island Teleport"
            ).decreaseCharges(1),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Cast").onMenuTarget(
                "Degrime",
                "Ward of Arceuus"
            ).decreaseCharges(2),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Reanimate").onMenuTarget(
                "Basic Reanimation"
            ).decreaseCharges(2),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Reanimate").onMenuTarget(
                "Adept Reanimation",
                "Expert Reanimation"
            ).decreaseCharges(3),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Reanimate").onMenuTarget(
                "Master Reanimation"
            ).decreaseCharges(4),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Resurrect").onMenuTarget(
                "Resurrect Crops"
            ).decreaseCharges(12),

            // Lunar spellbook.
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Cast").onMenuTarget(
                "Tan Leather",
                "Plank Make",
                "Energy Transfer"
            ).decreaseCharges(1),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Cast").onMenuTarget(
                "Spin Flax",
                "Fertile Soil"
            ).decreaseCharges(2),
            new OnXpDrop(Skill.MAGIC).isEquipped().onMenuOption("Cast").onMenuTarget(
                "Geomancy"
            ).decreaseCharges(3)
        ));
    }
}
