/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.area.FertileGround;
import games.stendhal.server.entity.mapstuff.spawner.FlowerGrower;
import games.stendhal.server.entity.player.Player;

/**
 * A seed can be planted.
 * The plant action defines the behaviour (e.g. only plantable on fertile ground).
 * The itemdata stores what it will grow.
 */
public class Seed extends StackableItem {
	public Seed(final Seed item) {
		super(item);
	}

	/**
	 * Creates a new seed
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public Seed(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (!this.isContained()) {
			// the seed is on the ground, but not next to the player
			if (!this.nextTo(user)) {
				user.sendPrivateText("" + this.getName() + " jest zbyt daleko");
				return false;
			}

			final StendhalRPZone userZone = user.getZone();
			final int pos_x = this.getX();
			final int pos_y = this.getY();

			boolean fertile = false;
			for (final Entity ent: userZone.getEntitiesAt(pos_x, pos_y)) {
				if (ent instanceof FertileGround) {
					// check for fertile ground
					fertile = true;
				} else if (ent instanceof FlowerGrower) {
					// check if we are overwriting another flower grower so seeds are not wasted & don't
					// allow infinite sowing in one spot
					user.sendPrivateText("W tym miejscu już coś rośnie.");
					return false;
				}
			}
			if (!fertile) {
				// don't waste seeds on infertile ground
				user.sendPrivateText("Ziemia jest nieurodzajna.");
				return false;
			}

			// the itemdata of the seed stores what it should grow
			final String itemdata = this.getItemData();
			FlowerGrower flowerGrower;
			// choose the default flower grower if there is none set
			if (itemdata == null) {
				flowerGrower = new FlowerGrower();
			} else {
				flowerGrower = new FlowerGrower(itemdata);
			}
			userZone.add(flowerGrower);
			// add the FlowerGrower where the seed was on the ground
			flowerGrower.setPosition(pos_x, pos_y);
			// The first stage of growth happens almost immediately
			TurnNotifier.get().notifyInTurns(3, flowerGrower);
			// remove the seed now that it is planted
			this.removeOne();
			if (user instanceof Player) {
				// XXX: should this increment only after flower grower has fully ripened?
				((Player) user).incSownForItem(flowerGrower.getItemName(), 1);
			}
			return true;
		}
		// the seed was 'contained' in a slot and so it cannot be planted
		user.sendPrivateText("Musisz zasadzić " + this.getName() + " w ziemi, aby wyrosło głuptasie!");
		return false;
	}

	@Override
	public String describe() {
		String seed_desc = getDescription();
		final String flower_name = getItemData();
		if (flower_name != null) {
			seed_desc = seed_desc.replaceFirst(flower_name, Grammar.variation(flower_name));
		}
		return seed_desc;
	}

	/**
	 * This is overridden so that sprite image can be updated whenever "itemdata" attribute is set.
	 */
	@Override
	public void put(final String attribute, final String value) {
		super.put(attribute, value);

		if (!"itemdata".equals(attribute)) {
			return;
		}

		// "seed" or "bulb"
		final String seedClass = getName();
		if ("bulwa".equals(seedClass)) {
			// zantedeschia image is default for bulb
			if (!"bielikrasa".equals(value)) {
				final String zantedeschia = value.replaceFirst("bielikrasa", "zantedeschia");
				super.put("subclass", "bulb_" + zantedeschia.replace(" ", "_"));
			}
		} else {
			// daisies image is default for seed
			if (!"stokrotki".equals(value)) {
				final String daisies = value.replaceFirst("stokrotki", "daisies");
				super.put("subclass", "seed_" + daisies.replace(" ", "_"));
			}
		}
	}
}
