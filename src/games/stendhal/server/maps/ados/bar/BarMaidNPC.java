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
package games.stendhal.server.maps.ados.bar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds a Bar Maid NPC to buy food from players.
 *
 * @author kymara
 */
public class BarMaidNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Siandra") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 27));
				nodes.add(new Node(3, 27));
				nodes.add(new Node(3, 13));
				nodes.add(new Node(20, 13));
				nodes.add(new Node(20, 18));
				nodes.add(new Node(28, 18));
				nodes.add(new Node(28, 7));
				nodes.add(new Node(12, 7));
				nodes.add(new Node(12, 13));
				nodes.add(new Node(3, 13));
				nodes.add(new Node(3, 27));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam!");
				addJob("Jestem kelnerką. Ze względu na ciężkie czasy nie mamy wystarczająco dużo jedzenia aby nakarmić naszych klientów. Czy możesz nam coś #zaoferować? Cokolwiek?");
				addHelp("Byłabym wdzięczna gdybyś mógł coś #zaoferować aby uzupełnić nasze zapasy: mięso, szynka lub ser.");
				addQuest("#Zaoferowano nam już dość jedzenia, dziękuję za pomoc.");
 				addGoodbye("Do widzenia, do widzenia!");
			}
		};

		npc.setDescription("Oto piękna młoda barmanka, Siandra.");
		npc.setEntityClass("woman_004_npc");
		npc.setGender("F");
		npc.setPosition(8, 27);
		zone.add(npc);
	}
}
