package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.common.Rand;
import games.stendhal.server.entity.player.Player;

public class SourceEntity extends PlayerActivityEntity {
	/**
	 * The chance that prospecting is successful.
	 */
	private static final double FINDING_PROBABILITY = 0.02;

	/**
	 * The equipment needed.
	 */
	public static final String[] NEEDED_PICKS = { "zardzewiały kilof", "kilof",
			"kilof stalowy", "kilof złoty", "kilof obsydianowy" };

	/**
	 * Calculates the probability that the given player finds stone. This is
	 * based on the player's mining skills, however even players with no skills
	 * at all have a 5% probability of success.
	 *
	 * @param player
	 *            The player,
	 *
	 * @return The probability of success.
	 */
	private double getSuccessProbability(final Player player) {
		double probability = FINDING_PROBABILITY;

		final String skill = player.getSkill("mining");
		if (skill != null) {
			probability = probability * (player.getMining()/5);
		}

		return probability + player.useKarma(0.02);
	}

	@Override
	protected int getDuration(final Player player) {
		if (player.isEquipped(NEEDED_PICKS[4])) {
			return 6 + Rand.rand(4);
		} else if (player.isEquipped(NEEDED_PICKS[3])) {
			return 12 + Rand.rand(4);
		} else if (player.isEquipped(NEEDED_PICKS[2])) {
			return 16 + Rand.rand(4);
		} else if (player.isEquipped(NEEDED_PICKS[1])) {
			return 20 + Rand.rand(4);
		}

		return 30 + Rand.rand(4);
	}

	@Override
	protected boolean isPrepared(Player player) {
		for (final String itemName : NEEDED_PICKS) {
			if (player.isEquipped(itemName)) {
				return true;
			}
		}

		player.sendPrivateText("Potrzebujesz kilofa do wydobywania kamieni.");
		return false;
	}

	/**
	 * Decides if the activity was successful.
	 *
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isSuccessful(final Player player) {
		final int random = Rand.roll1D100();
		return (random <= (getSuccessProbability(player) * 100));
	}

	@Override
	protected void onFinished(final Player player, final boolean successful) {
		// Do nothing
	}
	@Override
	protected void onStarted(Player player) {
		// Do nothing
	}
}