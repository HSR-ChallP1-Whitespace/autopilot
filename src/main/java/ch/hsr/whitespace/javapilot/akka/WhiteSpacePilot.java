package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.RaceStartMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.InitializePositionDetection;
import ch.hsr.whitespace.javapilot.akka.messages.PositionChangeMessage;
import ch.hsr.whitespace.javapilot.akka.messages.PowerChangeMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackRecognitionFinished;
import ch.hsr.whitespace.javapilot.config.PilotProperties;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.converter.TrackPartConverter;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.driving.DrivingTrackPart;

/**
 * Main Pilot-Actor
 * 
 */
public class WhiteSpacePilot extends UntypedActor {

	private static final long MIN_STRAIGHT_DURATION_FOR_SPEEDUP = 600;
	private static final int MAX_CURVE_POWER = 150;

	private final Logger LOGGER = LoggerFactory.getLogger(WhiteSpacePilot.class);

	private PilotProperties properties;

	private ActorRef pilot;
	private ActorRef dataAnalyzerActor;
	private ActorRef trackRecognizerActor;
	private ActorRef positionDetectorActor;
	private ActorRef dataSerializerActor;
	private ActorRef directionActor;
	private boolean trackRecognitionFinished = false;
	private Power currentPower;

	public WhiteSpacePilot(ActorRef pilot, PilotProperties properties) {
		this.pilot = pilot;
		this.properties = properties;
		this.currentPower = new Power(properties.getInitialPower());
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
			handleTrackRecognitionFinished((TrackRecognitionFinished) message);
		} else if (message instanceof PositionChangeMessage) {
			handleDirectionChanged((PositionChangeMessage) message);
		} else if (message instanceof RaceStartMessage) {
			handleRaceStart((RaceStartMessage) message);
		} else {
			unhandled(message);
		}
	}

	private void handleRaceStart(RaceStartMessage message) {
		Direction.initialize4TrackRecognition();
	}

	private void handleDirectionChanged(PositionChangeMessage message) {
		Power increasedPower = calculateIncreasedPower(message);
		positionDetectorActor.tell(new PowerChangeMessage(message.getTrackPart().getId(), increasedPower), getSelf());
		dataSerializerActor.tell(new PowerChangeMessage(message.getTrackPart().getId(), increasedPower), getSelf());
		setCurrentPower(increasedPower);
	}

	private Power calculateIncreasedPower(PositionChangeMessage message) {
		DrivingTrackPart trackPart = message.getTrackPart();
		if (trackPart.getDirection() == Direction.STRAIGHT) {
			if (!trackPart.hasPenalty() && trackPart.getDuration() > MIN_STRAIGHT_DURATION_FOR_SPEEDUP)
				return trackPart.getCurrentPower().increase(10);
			else
				return trackPart.getCurrentPower();
		} else {
			return new Power(Math.min(MAX_CURVE_POWER, currentPower.getValue()));
		}
	}

	private void forwardMessagesToChildren(Object message) {
		dataAnalyzerActor.forward(message, getContext());
		dataSerializerActor.forward(message, getContext());
		directionActor.forward(message, getContext());
		if (!isTrackRecognitionFinished())
			trackRecognizerActor.forward(message, getContext());
		if (isTrackRecognitionFinished())
			positionDetectorActor.forward(message, getContext());
	}

	private boolean isTrackRecognitionFinished() {
		return trackRecognitionFinished;
	}

	private void handleTrackRecognitionFinished(TrackRecognitionFinished message) {
		trackRecognitionFinished = true;
		trackRecognizerActor.tell(PoisonPill.getInstance(), getSelf());
		positionDetectorActor.tell(new InitializePositionDetection(TrackPartConverter.convertTrackParts(message.getTrackParts(), properties.getInitialPower())), getSelf());
		Direction.configure4Driving();
	}

	private void handleSensorEvent(SensorEvent event) {
		pilot.tell(new PowerAction(currentPower.getValue()), getSelf());
	}

	private void setCurrentPower(Power power) {
		currentPower = power;
	}

	@Override
	public void preStart() {
		initChildActors();
	}

	private void initChildActors() {
		this.dataAnalyzerActor = getContext().actorOf(Props.create(DataAnalyzerActor.class));
		this.trackRecognizerActor = getContext().actorOf(Props.create(TrackRecognizerActor.class));
		this.positionDetectorActor = getContext().actorOf(Props.create(PositionDetectorActor.class));
		this.dataSerializerActor = getContext().actorOf(Props.create(DataSerializerActor.class));
		this.directionActor = getContext().actorOf(Props.create(DirectionChangeRecognizerActor.class));
	}
}
