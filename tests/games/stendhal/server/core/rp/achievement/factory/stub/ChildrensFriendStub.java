/***************************************************************************
 *                    Copyright © 2020-2023 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.factory.stub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static utilities.PlayerTestHelper.equipWithItem;
import static utilities.PlayerTestHelper.equipWithStackableItem;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;

public class ChildrensFriendStub {
	private static final NPCList npcs = NPCList.get();

	public static void doQuestSusi(final Player player) {
		final String questSlot = "susi";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC susi = npcs.get("Susi");
		assertNotNull(susi);

		final Engine en = susi.getEngine();

		en.step(player, "hi");
		en.step(player, "quest");
		en.step(player, "friend");
		en.step(player, "Kółko jest okrągłe,");
		en.step(player, "nie ma końca.");
		en.step(player, "To jak długie,");
		en.step(player, "Będę twoim przyjacielem.");
		en.step(player, "bye");

		// note: quest slot is set to year of completion
		assertTrue(player.hasQuest(questSlot));
	}

	public static void doQuestTad(final Player player) {
		final String questSlot = "introduce_players";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC tad = npcs.get("Tad");
		final SpeakerNPC ilisa = npcs.get("Ilisa");
		assertNotNull(tad);
		assertNotNull(ilisa);

		Engine en = tad.getEngine();

		en.step(player, "hi");
		en.step(player, "quest");
		en.step(player, "yes");
		en.step(player, "bye");

		equipWithItem(player, "butelka");

		en.step(player, "hi");
		en.step(player, "bye");

		en = ilisa.getEngine();

		en.step(player, "hi");
		en.step(player, "yes");
		en.step(player, "bye");

		equipWithItem(player, "arandula");

		en.step(player, "hi");
		en.step(player, "bye");

		en = tad.getEngine();

		en.step(player, "hi");
		en.step(player, "bye");

		assertEquals("done", player.getQuest(questSlot, 0));
	}

	public static void doQuestPlink(final Player player) {
		final String questSlot = "plinks_toy";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC plink = npcs.get("Plink");
		assertNotNull(plink);

		final Engine en = plink.getEngine();

		en.step(player, "hi");
		en.step(player, "yes");

		// don't need to say "bye"
		assertEquals(ConversationStates.IDLE, en.getCurrentState());

		equipWithItem(player, "pluszowy miś");

		en.step(player, "hi");
		en.step(player, "bye");

		assertEquals("done", player.getQuest(questSlot, 0));
	}

	public static void doQuestAnna(final Player player) {
		final String questSlot = "toys_collector";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC anna = npcs.get("Anna");
		assertNotNull(anna);

		final Engine en = anna.getEngine();

		en.step(player, "hi");
		en.step(player, "toys");
		en.step(player, "yes");

		// don't need to say "bye"
		assertEquals(ConversationStates.IDLE, en.getCurrentState());

		en.step(player, "hi");
		en.step(player, "yes");

		for (final String toy: Arrays.asList("pluszowy miś", "kości do gry", "koszula")) {
			equipWithItem(player, toy);
			en.step(player, toy);
		}

		en.step(player, "bye");

		assertEquals("done", player.getQuest(questSlot, 0));
	}

	public static void doQuestSally(final Player player) {
		final String questSlot = "campfire";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC sally = npcs.get("Sally");
		assertNotNull(sally);

		final Engine en = sally.getEngine();

		en.step(player, "hi");
		en.step(player, "quest");
		en.step(player, "yes");
		en.step(player, "bye");

		equipWithStackableItem(player, "polano", 10);

		en.step(player, "hi");
		en.step(player, "yes");
		en.step(player, "bye");

		assertNotNull(player.getQuest(questSlot));
		assertNotEquals("start", player.getQuest(questSlot, 0));
	}

	public static void doQuestAnnie(final Player player) {
		final String questSlot = "icecream_for_annie";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC annie = npcs.get("Annie Jones");
		final SpeakerNPC mrsjones = npcs.get("Mrs. Jones");
		assertNotNull(annie);
		assertNotNull(mrsjones);

		Engine en = annie.getEngine();

		en.step(player, "hi");
		en.step(player, "quest");
		en.step(player, "yes");
		en.step(player, "bye");

		en = mrsjones.getEngine();

		en.step(player, "hi");
		en.step(player, "bye");

		en = annie.getEngine();

		equipWithItem(player, "lody");

		en.step(player, "hi");
		en.step(player, "yes");
		en.step(player, "bye");

		assertEquals("eating", player.getQuest(questSlot, 0));
	}

	public static void doQuestElisabeth(final Player player) {
		final String questSlot = "chocolate_for_elisabeth";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC elisabeth = npcs.get("Elisabeth");
		final SpeakerNPC carey = npcs.get("Carey");
		assertNotNull(elisabeth);
		assertNotNull(carey);

		Engine en = elisabeth.getEngine();

		en.step(player, "hi");
		en.step(player, "quest");
		en.step(player, "yes");
		en.step(player, "bye");

		en = carey.getEngine();

		en.step(player, "hi");
		en.step(player, "bye");

		equipWithItem(player, "tabliczka czekolady");

		en = elisabeth.getEngine();

		en.step(player, "hi");
		en.step(player, "yes");
		en.step(player, "bye");

		assertEquals("eating", player.getQuest(questSlot, 0));
	}

	public static void doQuestJef(final Player player) {
		final String questSlot = "find_jefs_mom";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC jef = npcs.get("Jef");
		final SpeakerNPC amber = npcs.get("Amber");
		assertNotNull(jef);
		assertNotNull(amber);

		Engine en = jef.getEngine();

		en.step(player, "hi");
		en.step(player, "quest");
		en.step(player, "yes");
		en.step(player, "bye");

		en = amber.getEngine();

		en.step(player, "hi");
		en.step(player, "Jef");

		// don't need to say "bye"
		assertEquals(ConversationStates.IDLE, en.getCurrentState());

		en = jef.getEngine();

		en.step(player, "hi");
		en.step(player, "fine");
		en.step(player, "bye");

		assertEquals("done", player.getQuest(questSlot, 0));
	}

	public static void doQuestHughie(final Player player) {
		final String questSlot = "fishsoup_for_hughie";
		assertNull(player.getQuest(questSlot));

		//final SpeakerNPC hughie = npcs.get("Hughie");
		final SpeakerNPC anastasia = npcs.get("Anastasia");
		//assertNotNull(hughie);
		assertNotNull(anastasia);

		final Engine en = anastasia.getEngine();

		en.step(player, "hi");
		en.step(player, "quest");
		en.step(player, "yes");
		en.step(player, "bye");

		equipWithItem(player, "zupa rybna");

		en.step(player, "hi");
		en.step(player, "yes");
		en.step(player, "bye");

		assertNotNull(player.getQuest(questSlot));
		assertNotEquals("start", player.getQuest(questSlot, 0));
	}

	public static void doQuestFinn(final Player player) {
		final String questSlot = "coded_message";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC finn = npcs.get("Finn Farmer");
		final SpeakerNPC george = npcs.get("George");
		assertNotNull(finn);
		assertNotNull(george);

		Engine en = finn.getEngine();

		en.step(player, "hi");
		en.step(player, "quest");
		en.step(player, "yes");

		String message = getReply(finn);

		en.step(player, "bye");

		en = george.getEngine();

		en.step(player, "hi");
		en.step(player, message);

		message = getReply(george);

		// don't need to say "bye"
		assertEquals(ConversationStates.IDLE, en.getCurrentState());

		en = finn.getEngine();

		en.step(player, "hi");
		en.step(player, message);
		en.step(player, "bye");

		assertEquals("done", player.getQuest(questSlot, 0));
	}

	public static void doQuestMarianne(final Player player) {
		final String questSlot = "eggs_for_marianne";
		assertNull(player.getQuest(questSlot));

		final SpeakerNPC marianne = npcs.get("Marianne");
		assertNotNull(marianne);

		final Engine en = marianne.getEngine();

		en.step(player, "hi");
		en.step(player, "quest");
		en.step(player, "yes");
		en.step(player, "bye");

		equipWithStackableItem(player, "jajo", 12);

		en.step(player, "hi");
		en.step(player, "yes");
		en.step(player, "bye");

		assertNotNull(player.getQuest(questSlot));
		assertNotEquals("start", player.getQuest(questSlot, 0));
	}
}
