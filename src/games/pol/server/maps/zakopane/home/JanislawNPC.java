package games.pol.server.maps.zakopane.home;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class JanislawNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Janisław") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj młody podróżniku, potrzebujesz #pomocy?");
				addJob("Jeszczę parę lat wstecz byłem jednym ze sławnych myśliwych w całej kranie, polowałem na potwory. Teraz to już jestem za stary na takie podróże.");
				addHelp("Póki jesteś jeszcze młody, mogę Tobie zlecać #zadania na upolowanie potwora.");
				addGoodbye();
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		npc.setEntityClass("npcstarybaca");
		npc.setDirection(Direction.RIGHT);
		npc.setPosition(2, 14);
		zone.add(npc);
	}
}