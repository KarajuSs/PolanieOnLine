/***************************************************************************
 *                   (C) Copyright 2003-2022 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client;

import games.stendhal.client.gui.ScreenController;
import games.stendhal.client.gui.UserInterface;
import games.stendhal.client.sound.facade.SoundSystemFacade;
import marauroa.client.ClientFramework;

/**
 * Keeps instances of singletons that may depend on the context
 *
 * @author hendrik
 */
public class ClientSingletonRepository {
	private static ClientFramework clientFramework;
	private static UserInterface userInterface;

	/**
	 * Gets the ClientFramework
	 *
	 * @return ClientFramework
	 */
	public static ClientFramework getClientFramework() {
		return clientFramework;
	}


	/**
	 * Sets the ClientFramework
	 *
	 * @param clientFramework ClientFramework
	 */
	public static void setClientFramework(ClientFramework clientFramework) {
		ClientSingletonRepository.clientFramework = clientFramework;
	}


	/**
	 * Gets the user interface
	 *
	 * @return UserInterface
	 */
	public static UserInterface getUserInterface() {
		return userInterface;
	}

	/**
	 * Sets the user interface
	 *
	 * @param userInterface UserInterface
	 */
	public static void setUserInterface(UserInterface userInterface) {
		ClientSingletonRepository.userInterface = userInterface;
	}

	/**
	 * Gets the screen controller.
	 *
	 * @return
	 *     ScreenController instance.
	 */
	public static ScreenController getScreenController() {
		return ScreenController.get();
	}

	/**
	 * Gets the sound system
	 *
	 * @return SoundSystemFacade
	 */
	public static SoundSystemFacade getSound() {
		return userInterface.getSoundSystemFacade();
	}
}
