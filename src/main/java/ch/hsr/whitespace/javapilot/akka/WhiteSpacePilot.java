package ch.hsr.whitespace.javapilot.akka;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.config.PilotProperties;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

/**
 * Main Pilot-Actor
 * 
 */
public class WhiteSpacePilot extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(WhiteSpacePilot.class);

	private PilotProperties properties;

	private ActorRef pilot;
	private ActorRef dataAnalyzerActor;
	private ActorRef trackRecognizerActor;
	private ActorRef positionCalculatorActor;

	private List<TrackPart> trackParts;

	public WhiteSpacePilot(ActorRef pilot, PilotProperties properties) {
		this.pilot = pilot;
		this.properties = properties;
	}

	public static Props props(ActorRef pilot, PilotProperties properties) {
		return Props.create(WhiteSpacePilot.class, () -> new WhiteSpacePilot(pilot, properties));
	}

	@Override
	public void onReceive(Object message) throws Exception {
		forwardMessagesToChildren(message);
		if (message instanceof SensorEvent) {
			handleSensorEvent((SensorEvent) message);
		} else if (message instanceof TrackRecognitionFinished) {
			handleTrackRecognitionFinished(message);
		} else {
			unhandled(message);
		}
	}

	private void forwardMessagesToChildren(Object message) {
		dataAnalyzerActor.forward(message, getContext());
		if (!isTrackRecognitionFinished())
			trackRecognizerActor.forward(message, getContext());
		if (isTrackRecognitionFinished())
			positionCalculatorActor.forward(message, getContext());
	}

	private boolean isTrackRecognitionFinished() {
		return trackParts != null;
	}

	private void handleTrackRecognitionFinished(Object message) {
		this.trackParts = ((TrackRecognitionFinished) message).getTrackParts();
		trackRecognizerActor.tell(PoisonPill.getInstance(), getSelf());
		positionCalculatorActor.tell(message, getSelf());
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
		this.positionCalculatorActor = getContext().actorOf(Props.create(PositionCalculatorActor.class));
	}

	static public class TrackRecognitionFinished {

		public TrackRecognitionFinished(List<TrackPart> trackParts) {
			this.trackParts = trackParts;
		}

		private List<TrackPart> trackParts;

		public List<TrackPart> getTrackParts() {
			return trackParts;
		}
	}
}
