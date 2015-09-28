package ch.hsr.whitespace.javapilot.services;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.RaceStartMessage;
import com.zuehlke.carrera.relayapi.messages.RaceStopMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import com.zuehlke.carrera.simulator.model.PilotInterface;

import akka.actor.ActorRef;

/**
 * Implementation to connect a RaceTrackSimulatorSystem to a Pilot System
 * locally
 */
public class RacetrackToPilotConnector implements PilotInterface, PilotCommandInterface {

	ActorRef pilotEntryPoint;

	public void registerPilot(ActorRef pilotEntryPoint) {
		this.pilotEntryPoint = pilotEntryPoint;
	}

	@Override
	public void send(SensorEvent message) {
		sendMessage(message);
	}

	@Override
	public void send(VelocityMessage message) {
		sendMessage(message);
	}

	@Override
	public void send(PenaltyMessage message) {
		sendMessage(message);
	}

	@Override
	public void send(RoundTimeMessage message) {
		sendMessage(message);
	}

	@Override
	public void ensureConnection(String url) {
		// don't need this in local mode
	}

	@Override
	public void send(RaceStartMessage message) {
		sendMessage(message);
	}

	@Override
	public void send(RaceStopMessage message) {
		sendMessage(message);
	}

	private void sendMessage(Object message) {
		// Messages may arrive during startup prior to entry point registration.
		// Just ignore them
		if (pilotEntryPoint != null) {
			pilotEntryPoint.tell(message, ActorRef.noSender());
		}
	}

}
