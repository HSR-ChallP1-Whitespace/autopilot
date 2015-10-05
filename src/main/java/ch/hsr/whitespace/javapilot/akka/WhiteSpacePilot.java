package ch.hsr.whitespace.javapilot.akka;

import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Main Pilot-Actor
 * 
 */
public class WhiteSpacePilot extends UntypedActor {

	private int power = 100;
	private ActorRef pilot;

	private ActorRef dataAnalyzerActor;

	public WhiteSpacePilot(ActorRef pilot) {
		this.pilot = pilot;
	}

	public static Props props(ActorRef pilot) {
		return Props.create(WhiteSpacePilot.class, () -> new WhiteSpacePilot(pilot));
	}

	@Override
	public void onReceive(Object message) throws Exception {
		dataAnalyzerActor.forward(message, getContext());
		if (message instanceof SensorEvent) {
			handleSensorEvent((SensorEvent) message);
		} else {
			unhandled(message);
		}
	}

	private void handleSensorEvent(SensorEvent event) {
		// at the moment we simply drive with constant power (while track
		// recognition)
		pilot.tell(new PowerAction(power), getSelf());
	}

	@Override
	public void preStart() {
		initChildActors();
	}

	private void initChildActors() {
		this.dataAnalyzerActor = getContext().actorOf(Props.create(DataAnalyzerActor.class));
	}
}
