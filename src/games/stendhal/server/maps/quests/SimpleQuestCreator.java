/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;

public class SimpleQuestCreator {

	private static SimpleQuestCreator instance;

	public static SimpleQuestCreator getInstance() {
		if (instance == null) {
			instance = new SimpleQuestCreator();
		}

		return instance;
	}

	public SimpleQuest create(final String slotName, final String properName, final String npcName) {
		return new SimpleQuest(slotName, properName, npcName);
	}

	public class SimpleQuest extends AbstractQuest {

		private final String QUEST_SLOT;
		private final String name;
		private final SpeakerNPC npc;

		private String description;

		private int repeatDelay = -1;

		private String itemToCollect;
		private int quantityToCollect = 1;

		private Map<String, String> replies = new HashMap<String, String>() {{
			// default replies
			put("request", "Pomożesz mi?");
			put("reject", "Okej. Być może innym razem.");
			put("reward", "Dziękuję.");
		}};

		private int xpReward = 0;
		private double karmaReward = 0;
		private double karmaAcceptReward = 0;
		private double karmaRejectReward = 0;
		private final Map<String, Integer> itemReward = new HashMap<String, Integer>();
		//private final Map<String, Integer> statReward = new HashMap<String, Integer>();

		private String region;


		public SimpleQuest(final String slotName, final String properName, final String npcName) {
			QUEST_SLOT = slotName;
			name = properName;
			npc = SingletonRepository.getNPCList().get(npcName);
		}

		public void setDescription(final String descr) {
			description = descr;
		}

		/**
		 * Sets the quest's repeatable status & repeat delay.
		 *
		 * @param delay
		 * 		Number of minutes player must wait before repeating quest.
		 * 		`0` means immediately repeatable. `null` or less than `0`
		 * 		means not repeatable.
		 */
		public void setRepeatable(Integer delay) {
			if (delay == null) {
				delay = -1;
			}

			repeatDelay = delay;
		}

		public void setItemToCollect(final String itemName, final int quantity) {
			itemToCollect = itemName;
			quantityToCollect = quantity;
		}

		public void setXPReward(final int xp) {
			xpReward = xp;
		}

		public void setKarmaReward(final double karma) {
			karmaReward = karma;
		}

		public void setKarmaAcceptReward(final double karma) {
			karmaAcceptReward = karma;
		}

		public void setKarmaRejectReward(final double karma) {
			karmaRejectReward = karma;
		}

		public void addItemReward(final String itemName, final int quantity) {
			itemReward.put(itemName, quantity);
		}

		@SuppressWarnings("unused")
		public void addItemReward(final String itemName) {
			addItemReward(itemName, 1);
		}

		public void setReply(final String rType, final String reply) {
			replies.put(rType, reply);
		}

		private String getReply(final String rType) {
			String reply = replies.get(rType);

			if (rType.equals("accept")) {
				if (reply == null) {
					reply = "Potrzebuję Twojej pomocy, by zdobyć " + Integer.toString(quantityToCollect) + " " + itemToCollect + ".";
				} else {
					reply = reply + " Potrzebuję Twojej pomocy, by zdobyć " + Integer.toString(quantityToCollect) + " " + itemToCollect + ".";
				}
			}

			return reply;
		}

		public void setRequestReply(final String reply) {
			setReply("request", reply);
		}

		public void setAcceptReply(final String reply) {
			setReply("accept", reply);
		}

		public void setRejectReply(final String reply) {
			setReply("reject", reply);
		}

		public void setRewardReply(final String reply) {
			setReply("reward", reply);
		}

		public void setRegion(final String regionName) {
			region = regionName;
		}

		/**
		 * Retrives the number of times the player has completed the quest.
		 *
		 * @param player
		 * 		The Player to check.
		 * @return
		 * 		Number of times completed.
		 */
		private int getCompletedCount(final Player player) {
			final String state = player.getQuest(QUEST_SLOT, 0);

			if (state == null) {
				return 0;
			}

			int completedIndex = 2;
			if (state.equals("start") || state.equals("rejected")) {
				completedIndex = 1;
			}

			try {
				return Integer.parseInt(player.getQuest(QUEST_SLOT, completedIndex));
			} catch (NumberFormatException e) {
				return 0;
			}
		}

		/**
		 * Action to execute when player starts quest.
		 *
		 * @return
		 * 		`ChatAction`
		 */
		private ChatAction startAction() {
			return new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.addKarma(karmaAcceptReward);
					player.setQuest(QUEST_SLOT, "start;" + Integer.toString(getCompletedCount(player)));
				}
			};
		}

		/**
		 * Action to execute when player rejects quest.
		 *
		 * @return
		 * 		`ChatAction`
		 */
		private ChatAction rejectAction() {
			return new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.addKarma(karmaRejectReward);
					player.setQuest(QUEST_SLOT, "rejected;" + Integer.toString(getCompletedCount(player)));
				}
			};
		}

		/**
		 * Action to execute when player completes quest
		 *
		 * @return
		 * 		`ChatAction`
		 */
		private ChatAction completeAction() {
			return new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser npc) {
					// drop collected items
					player.drop(itemToCollect, quantityToCollect);

					final StringBuilder sb = new StringBuilder();
					sb.append(getReply("reward"));

					final int rewardCount = itemReward.size();

					if (rewardCount > 0) {
						sb.append(" W nagrodę otrzymasz ode mnie ");

						int idx = 0;
						for (final String itemName: itemReward.keySet()) {
							final int quantity = itemReward.get(itemName);

							if (idx == rewardCount - 1) {
								sb.append("oraz ");
							}

							sb.append(Integer.toString(quantity) + " " + Grammar.plnoun(quantity, itemName));

							if (idx < rewardCount - 1) {
								if (rewardCount == 2) {
									sb.append(" ");
								} else {
									sb.append(", ");
								}
							}

							idx++;
						}

						sb.append(".");
					}

					npc.say(sb.toString());

					// reward player
					player.addXP(xpReward);
					player.addKarma(karmaReward);

					for (final String itemName: itemReward.keySet()) {
						final EntityManager em = SingletonRepository.getEntityManager();
						final Item item = em.getItem(itemName);
						final int quantity = itemReward.get(itemName);

						if (item instanceof StackableItem) {
							((StackableItem) item).setQuantity(quantity);
						}

						if (item != null) {
							player.equipOrPutOnGround(item);
						}
					}

					player.setQuest(QUEST_SLOT, "done;" + System.currentTimeMillis() + ";" + Integer.toString(getCompletedCount(player) + 1));
				}
			};
		}

		/**
		 * This must be called in order for the quest to be added to game.
		 */
		public void register() {
			StendhalQuestSystem.get().loadQuest(this);
		}

		@Override
		public void addToWorld() {
			fillQuestInfo(name, description, isRepeatable());

			final ChatCondition canStartCondition = new ChatCondition() {
				@Override
				public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
					if (player.getQuest(QUEST_SLOT) == null) {
						return true;
					}

					if (isRepeatable() && player.getQuest(QUEST_SLOT, 0).equals("done")) {
						return new TimePassedCondition(QUEST_SLOT, 1, repeatDelay).fire(player, sentence, npc);
					}

					return false;
				}
			};

			final ChatCondition questRepeatableCondition = new ChatCondition() {
				@Override
				public boolean fire(Player player, Sentence sentence, Entity npc) {
					return isRepeatable();
				}
			};


			npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				canStartCondition,
				ConversationStates.QUEST_OFFERED,
				getReply("request"),
				null);

			npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
					questRepeatableCondition,
					new QuestInStateCondition(QUEST_SLOT, 0, "done")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, repeatDelay, "Jeżeli chcesz ponownie mi pomóc to przyjdź za "));

			npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
					new NotCondition(questRepeatableCondition),
					new QuestInStateCondition(QUEST_SLOT, 0, "done")),
				ConversationStates.ATTENDING,
				"Dzięki, ale nie potrzebuję więcej już pomocy.",
				null);

			npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Znowu? Musisz zdobyć dla mnie " + Integer.toString(quantityToCollect) + " " + itemToCollect + ".",
				null);

			npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				getReply("accept"),
				startAction());

			npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				getReply("reject"),
				rejectAction());

			npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(
					new QuestActiveCondition(QUEST_SLOT),
					new PlayerHasItemWithHimCondition(itemToCollect, quantityToCollect)),
				ConversationStates.ATTENDING,
				null,
				completeAction());

			npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(
					new QuestActiveCondition(QUEST_SLOT),
					new NotCondition(new PlayerHasItemWithHimCondition(itemToCollect, quantityToCollect))),
				ConversationStates.ATTENDING,
				"Gdzie to masz? Miałeś przynieść dla mnie " + Integer.toString(quantityToCollect) + " " + itemToCollect + ".",
				null);
		}

		@Override
		public String getSlotName() {
			return QUEST_SLOT;
		}

		@Override
		public String getName() {
			final StringBuilder sb = new StringBuilder();
			boolean titleCase = true;

			for (char c: name.toCharArray()) {
				if (Character.isSpaceChar(c)) {
					titleCase = true;
				} else if (titleCase) {
					c = Character.toTitleCase(c);
					titleCase = false;
				}

				sb.append(c);
			}

			return sb.toString().replace(" ", "");
		}

		@Override
		public String getNPCName() {
			if (npc == null) {
				return null;
			}

			return npc.getName();
		}

		@Override
		public String getRegion() {
			return region;
		}

		@Override
		public boolean isRepeatable(final Player player) {
			if (!isRepeatable()) {
				return false;
			}

			return new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
					new TimePassedCondition(QUEST_SLOT, 1, repeatDelay)).fire(player, null, null);
		}

		/**
		 * Checks if this quest has be set for repetition.
		 *
		 * @return
		 * 		<code>true</code> if players are allowed to do this quest more than once.
		 */
		private boolean isRepeatable() {
			return repeatDelay >= 0;
		}

		@Override
		public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();

			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}

			final String[] questState = player.getQuest(QUEST_SLOT).split(";");

			if (questState[0].equals("start")) {
				res.add(getNPCName() + " poprosił mnie, abym zdobył " + Integer.toString(quantityToCollect) + " " + itemToCollect + ".");
				if (player.isEquipped(itemToCollect, quantityToCollect)) {
					res.add("Zdobyłem już to, o co mnie poproszono dla " + getNPCName() + ".");
				} else {
					res.add("Nie znalazłem jeszcze tego, czego potrzebuje.");
				}
			} else if (questState[0].equals("done")) {
				res.add("Znalazłem " + Integer.toString(quantityToCollect) + " " + itemToCollect + " dla " + getNPCName() + ".");

				if (isRepeatable()) {
					final int completions = getCompletedCount(player);
					String plural = "raz";
					if (completions != 1) {
						plural += "y";
					}

					res.add("Ukończyłem zadanie " + Integer.toString(completions) + " " + plural + ".");
				}
			}

			return res;
		}
	}
}