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
package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.StatLevelComparisonCondition;


public class ExperienceRATKAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.EXPERIENCE_RATK;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final List<Achievement> achievements = new LinkedList<>();

		String level = "25";
		achievements.add(createAchievement(
				"ratk.level." + level, "Początkujący Dystansowiec", "Osiągnął " + level + " poziom STR",
				Achievement.EASY_BASE_SCORE, true,
				createComparison(level)));

		level = "50";
		achievements.add(createAchievement(
				"ratk.level." + level, "Strzelec", "Osiągnął " + level + " poziom STR",
				Achievement.EASY_BASE_SCORE, true,
				createComparison(level)));

		level = "75";
		achievements.add(createAchievement(
				"ratk.level." + level, "Zaawansowany Strzelec", "Osiągnął " + level + " poziom STR",
				Achievement.MEDIUM_BASE_SCORE, true,
				createComparison(level)));

		level = "100";
		achievements.add(createAchievement(
				"ratk.level." + level, "Ekspert Strzelnictwa", "Osiągnął " + level + " poziom STR",
				Achievement.MEDIUM_BASE_SCORE, true,
				createComparison(level)));

		level = "150";
		achievements.add(createAchievement(
				"ratk.level." + level, "Mistrz Strzelnictwa", "Osiągnął " + level + " poziom STR",
				Achievement.HARD_BASE_SCORE, true,
				createComparison(level)));

		return achievements;
	}

	private StatLevelComparisonCondition createComparison(final String level) {
		return new StatLevelComparisonCondition("ratk", ">=", Integer.parseInt(level));
	}
}