/***************************************************************************
 *                 (C) Copyright 2003-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.blacksmith;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * The cooking girl.
 * Makes dwojaki for players.
 * Buys kisc winogron.
 * 
 * @author Legolas
 */
public class DwojakiElwirkaNPC implements ZoneConfigurator  {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Elwirka") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(2,13));
				nodes.add(new Node(4,13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Zajmuję się kuchnią kowala Andrzeja. Poproś mnie #ugotuj dwojaki a przygotuję dla ciebie gorące danie z gruli!");
				addHelp("Możesz mi pomóc i przegonić szczury, które biegają dookoła domu. Nasz kot gdzieś się zapodział.");
				addReply("ziemniaki", "Wieśniacy trudnią się zbieraniem ziemniaków, ale nie sądzę, że ci je dadzą z własnej woli.");
				addReply("kiełbasa wiejska", "Z tego co wiem kiełbasa wiejska z tutejszych świń jest najlepsza na skwarki.");
				addReply(Arrays.asList("onion", "cebula"), "Cebulę poznasz po zielonych cieńkich listkach wystających z grządki.");
				addReply(Arrays.asList("butter", "masło"), "Bez omasty nie zrobię dobrych dwojaków.");
				addReply(Arrays.asList("milk", "mleko"), "Trochę mleka dodam i będzie przepyszna potrawa. Idź wydoić jakąś krowę.");
				addReply(Arrays.asList("dwojak", "dwojaki"),
						"Dwojaki to gliniane naczynie, które po brzegi wypełnię kucianymi grulami. Jeśli chcesz powiedz mi po prostu #'ugotuj 1 dwojaki'.");
				addOffer("Cóż. Jeśli możesz mi zaoferować kiść winogron chętnie odkupię je od ciebie. Powiedz mi wtedy #'sprzedam kiść winogron'.");
				addGoodbye();

				// Elwirka makes dwojaki if you bring her ziemniaki, kiełbasa wiejska, onion, butter and milk.
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("ziemniaki", 3);
				requiredResources.put("kiełbasa wiejska", 3);
				requiredResources.put("cebula", 1);
				requiredResources.put("osełka masła", 1);
				requiredResources.put("mleko", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour(
						"elwirka_make_dwojaki", Arrays.asList("make", "ugotuj"), "dwojaki",
						requiredResources, 5 * 60);

				new ProducerAdder().addProducer(this, behaviour,
						"Witojcie! Co was tu sprowadza? Pewnie moje legendarne #dwojaki.");

			}
		};

		npc.setDescription("Oto Elwirka, która umie przygotować pyszne potrawy z gruli i lubi winogrona.");
		npc.setEntityClass("confectionerapplepienpc");
		npc.setGender("F");
		npc.setPosition(2, 13);
		zone.add(npc);
	}
}
