/***************************************************************************
 *                    Copyright © 2003-2022 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.PassiveNPC;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;

/**
 * An entity that can engage in combat.
 */
public abstract class CombatEntity extends GuidedEntity {
	private static final Logger logger = Logger.getLogger(CombatEntity.class);
	public static final String RPCLASS_NAME = "combat_entity";

	/**
	 * Player(s) taking damage receive more XP.
	 */
	private static final int GUARANTEED_ATK_XP_TURNS = 12;

	/**
	 * Maps each enemy which has recently damaged this entity to the turn when
	 * the last damage has occurred.
	 */
	protected final Map<CombatEntity, Integer> enemiesThatGiveFightXP;

	/**
	 * Default constructor.
	 */
	public CombatEntity() {
		super();
		enemiesThatGiveFightXP = new WeakHashMap<>();
	}

	/**
	 * Copy constructor.
	 *
	 * @param object
	 *     The entity to be copied.
	 */
	public CombatEntity(final RPObject object) {
		super(object);
		enemiesThatGiveFightXP = new WeakHashMap<>();
	}

	/**
	 * Generates the RPClass & specifies attributes.
	 */
	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(RPCLASS_NAME);
			rpclass.isA("active_entity");

			// hp/mana/etc.
			rpclass.addAttribute("base_hp", Type.SHORT);
			rpclass.addAttribute("hp", Type.SHORT);
			rpclass.addAttribute("base_mana", Type.INT);
			rpclass.addAttribute("mana", Type.INT);

			rpclass.addAttribute("base_capacity", Type.FLOAT, (byte) (Definition.PRIVATE | Definition.VOLATILE));
			rpclass.addAttribute("capacity", Type.FLOAT, (byte) (Definition.PRIVATE | Definition.VOLATILE));

			// stats
			rpclass.addAttribute("level", Type.SHORT);
			rpclass.addAttribute("xp", Type.INT);
			rpclass.addAttribute("atk", Type.SHORT);
			rpclass.addAttribute("atk_xp", Type.INT, Definition.PRIVATE);
			// TODO: volatile flag should be removed when ready for main server
			rpclass.addAttribute("ratk", Type.SHORT, (byte) (Definition.PRIVATE | Definition.VOLATILE));
			rpclass.addAttribute("ratk_xp", Type.INT, (byte) (Definition.PRIVATE | Definition.VOLATILE));
			rpclass.addAttribute("def", Type.SHORT);
			rpclass.addAttribute("def_xp", Type.INT, Definition.PRIVATE);
			rpclass.addAttribute("mining", Type.SHORT, Definition.PRIVATE);
			rpclass.addAttribute("mining_xp", Type.INT, Definition.PRIVATE);

			rpclass.addAttribute("atk_item", Type.INT,
					(byte) (Definition.PRIVATE | Definition.VOLATILE));
			rpclass.addAttribute("ratk_item", Type.INT,
					(byte) (Definition.PRIVATE | Definition.VOLATILE));
			rpclass.addAttribute("def_item", Type.INT,
					(byte) (Definition.PRIVATE | Definition.VOLATILE));
			rpclass.addAttribute("heal", Type.INT, Definition.VOLATILE);
			// TODO: check that the binary representation of old saved players
			//       is compatible when this is changed into a list.
			rpclass.addAttribute("target", Type.INT, Definition.VOLATILE);

			rpclass.addAttribute("crit", Type.FLAG);

			// Status effects
			rpclass.addAttribute("bleeding", Type.SHORT, Definition.VOLATILE);
			rpclass.addAttribute("choking", Type.SHORT, Definition.VOLATILE);
			rpclass.addAttribute("status_confuse", Type.SHORT, Definition.VOLATILE);
			rpclass.addAttribute("eating", Type.SHORT, Definition.VOLATILE);
			rpclass.addAttribute("poisoned", Type.SHORT, Definition.VOLATILE);
			rpclass.addAttribute("status_shock", Type.SHORT, Definition.VOLATILE);
			rpclass.addAttribute("status_zombie", Type.SHORT, Definition.VOLATILE);
			rpclass.addAttribute("status_heavy", Type.SHORT, Definition.VOLATILE);

			// status effects resistances
			rpclass.addAttribute("resist_bleeding", Type.FLOAT, Definition.VOLATILE);
			rpclass.addAttribute("resist_confused", Type.FLOAT, Definition.VOLATILE);
			rpclass.addAttribute("resist_drunk", Type.FLOAT, Definition.VOLATILE);
			rpclass.addAttribute("resist_heavy", Type.FLOAT, Definition.VOLATILE);
			rpclass.addAttribute("resist_poisoned", Type.FLOAT, Definition.VOLATILE);
			rpclass.addAttribute("resist_shocked", Type.FLOAT, Definition.VOLATILE);
			rpclass.addAttribute("resist_zombie", Type.FLOAT, Definition.VOLATILE);

			// events
			rpclass.addRPEvent("attack", Definition.VOLATILE);

			// DO NOT USE, the following are obsolete
			rpclass.addAttribute("risk", Type.BYTE, Definition.VOLATILE);
			rpclass.addAttribute("damage", Type.INT, Definition.VOLATILE);
		} catch (final Exception e) {
				logger.error("cannot generate RPClass", e);
		}
	}

	/**
	 * Checks if this entity should get ATK XP.
	 *
	 * @param defender
	 *     The target of this entity.
	 */
	public boolean getsAtkXpFrom(final CombatEntity defender) {
		if (!(this instanceof Player)) {
			return false;
		}

		// PvP is handled with the traditional rules
		if (defender instanceof Player) {
			return recentlyDamagedBy(defender);
		}
		// PvE with fogs with the traditional rules
		if (defender instanceof Creature) {
			if (((Creature) defender).isImmortal()) {
				return recentlyDamagedBy(defender);
			}
		}

		return !(defender instanceof PassiveNPC);
	}

	/**
	 * Checks if this entity should get DEF XP.
	 *
	 * @param attacker
	 *     The entity targeting this one.
	 * @param damaged
	 *     Whether or not damage was inflicted.
	 */
	public boolean getsDefXpFrom(final CombatEntity attacker, final boolean damaged) {
		if (!(this instanceof Player)) {
			return false;
		}

		// immediate damage guarantees XP
		if (damaged) {
			return true;
		}

		return recentlyDamagedBy(attacker);
	}

	/**
	 * Checks if entity qualifies to receive combat XP.
	 *
	 * @param opponent
	 *     Opposing entity.
	 * @return
	 *     <code>true</code> if damage occurred within a specified
	 *     number of turns.
	 */
	public boolean recentlyDamagedBy(final CombatEntity opponent) {
		final Integer turnWhenLastDamaged = enemiesThatGiveFightXP.get(opponent);
		if (turnWhenLastDamaged == null) {
			return false;
		}

		final int currentTurn = SingletonRepository.getRuleProcessor().getTurn();
		if (currentTurn - turnWhenLastDamaged > GUARANTEED_ATK_XP_TURNS) {
			enemiesThatGiveFightXP.remove(opponent);
			return false;
		}

		return true;
	}
}