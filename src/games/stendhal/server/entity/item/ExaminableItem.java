/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
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

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.events.ExamineEvent;

public class ExaminableItem extends StackableItem {
	public ExaminableItem(ExaminableItem item) {
		super(item);
		put("menu", "Przyjrzyj się|use");
	}

	public ExaminableItem(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		put("menu", "Przyjrzyj się|use");
	}

	@Override
	public boolean onUsed(RPEntity user) {
		String[] itemdata = getItemData().split("\t");
		String image = itemdata[0];
		String title = itemdata[1];
		String caption = itemdata[2];

		user.addEvent(new ExamineEvent(image, title, caption));
		user.notifyWorldAboutChanges();
		return false;
	}
}