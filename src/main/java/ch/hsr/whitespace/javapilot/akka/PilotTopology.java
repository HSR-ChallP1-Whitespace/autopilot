package ch.hsr.whitespace.javapilot.akka;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * creates the complete topology and provides a Map of well-defined entry-points
 */
public class PilotTopology {

	public static final String SENSOR_ENTRYPOINT = "SENSOR_ENTRYPOINT";
	public static final String VELOCITY_ENTRYPOINT = "VELOCITY_ENTRYPOINT";
	public static final String PENALTY_ENTRYPOINT = "PENALTY_ENTRYPOINT";
	public static final String RACE_START_ENTRYPOINT = "RACE_START_ENTRYPOINT";

	private final ActorSystem system;
	private final ActorRef pilot;
	private final Map<String, ActorRef> entryPoints = new HashMap<>();

	public PilotTopology(ActorRef pilot, ActorSystem system) {
		this.pilot = pilot;
		this.system = system;
	}

	public Map<String, ActorRef> create() {
		ActorRef initialProcessor = system.actorOf(WhiteSpacePilot.props(pilot));

		entryPoints.put(PENALTY_ENTRYPOINT, initialProcessor);
		entryPoints.put(SENSOR_ENTRYPOINT, initialProcessor);
		entryPoints.put(VELOCITY_ENTRYPOINT, initialProcessor);
		entryPoints.put(RACE_START_ENTRYPOINT, initialProcessor);

		return entryPoints;
	}

}
