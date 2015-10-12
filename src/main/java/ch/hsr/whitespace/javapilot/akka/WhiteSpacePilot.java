package ch.hsr.whitespace.javapilot.akka;

import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.config.PilotProperties;

/**
 * Main Pilot-Actor
 * 
 */
public class WhiteSpacePilot extends UntypedActor {

	private PilotProperties properties;

	private ActorRef pilot;

	private ActorRef dataAnalyzerActor;
	private ActorRef trackRecognizerActor;

	public WhiteSpacePilot(ActorRef pilot, PilotProperties properties) {
		this.pilot = pilot;
		this.properties = properties;
	}

	public static Props props(ActorRef pilot, PilotProperties properties) {
		return Props.create(WhiteSpacePilot.class, () -> new WhiteSpacePilot(pilot, properties));
	}

	@Override
	public void onReceive(Object message) throws Exception {
		dataAnalyzerActor.forward(message, getContext());
		trackRecognizerActor.forward(message, getContext());
		if (message instanceof SensorEvent) {
			handleSensorEvent((SensorEvent) message);
		} else {
			unhandled(message);
		}
	}

	private void handleSensorEvent(SensorEvent event) {
		// at the moment we simply drive with constant power (while track
		// recognition)
		pilot.tell(new PowerAction(properties.getInitialPower()), getSelf());
	}

	@Override
	public void preStart() {
		initChildActors();
	}

	private void initChildActors() {
		this.dataAnalyzerActor = getContext().actorOf(Props.create(DataAnalyzerActor.class));
		this.trackRecognizerActor = getContext().actorOf(Props.create(TrackRecognizerActor.class));
	}
}
