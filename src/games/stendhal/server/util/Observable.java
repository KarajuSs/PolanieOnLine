/***************************************************************************
 *                   Copyright (C) 2003-2022 - Arianne                     *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Wrapper for deprecated java.util.Observable class to prepare
 * for moving toward something else.
 *
 * Source: https://developer.classpath.org/doc/java/util/Observable-source.html
 */
public class Observable extends java.util.Observable {

	/** Tracks whether this object has changed. */
	private boolean changed = false;

	private LinkedHashSet<Observer> observers;

	public Observable() {
		observers = new LinkedHashSet<>();
	}

	/**
	 * mark Registrator object as changed.
	 * Function was moved from protected (in java.util.Observable)
	 * to public zone.
	 */
	public void setChanges() {
		setChanged();
	}

	public void addObserver(final Observer o) {
		if (o == null) {
			throw new NullPointerException("can't add null observer");
		}

		observers.add(o);
	}

	public void deleteObserver(final Observer o) {
		observers.remove(o);
	}

	@Override
	public void notifyObservers(final Object obj) {
		if (!hasChanged()) {
			return;
		}

		Set s;
		synchronized (this) {
			s = (Set) observers.clone();
		}
		int i = s.size();
		Iterator iter = s.iterator();
		while (--i >= 0) {
			((Observer) iter.next()).update(this, obj);
		}

		clearChanged();
	}
}