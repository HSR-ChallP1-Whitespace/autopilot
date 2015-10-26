package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChanged;
import ch.hsr.whitespace.javapilot.akka.messages.InitializePositionDetection;
import ch.hsr.whitespace.javapilot.akka.messages.PowerChangeMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackRecognitionFinished;
import ch.hsr.whitespace.javapilot.config.PilotProperties;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.driving.DrivingTrackPart;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;

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
	private ActorRef positionDetectorActor;
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
		} else if (message instanceof DirectionChanged) {
			handleDirectionChanged((DirectionChanged) message);
		} else {
			unhandled(message);
		}
	}

	private void handleDirectionChanged(DirectionChanged message) {
		Power increasedPower = calculateIncreasedPower(message);
		positionDetectorActor.tell(new PowerChangeMessage(message.getTrackPart().getId(), increasedPower), getSelf());
		setCurrentPower(increasedPower);
	}

	private Power calculateIncreasedPower(DirectionChanged message) {
		if (message.getTrackPart().getDirection() == Direction.STRAIGHT)
			return message.getTrackPart().getCurrentPower().increase(20);
		return currentPower;
	}

	private void forwardMessagesToChildren(Object message) {
		dataAnalyzerActor.forward(message, getContext());
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
		positionDetectorActor.tell(new InitializePositionDetection(initializeTrackParts4PositionDetector(message.getTrackParts())), getSelf());
	}

	private List<DrivingTrackPart> initializeTrackParts4PositionDetector(List<RecognitionTrackPart> recognizerParts) {
		List<DrivingTrackPart> parts4PositionDetector = new ArrayList<>();
		int idCounter = 1;
		for (RecognitionTrackPart part : recognizerParts) {
			DrivingTrackPart partCopy = new DrivingTrackPart(idCounter, part.getDirection());
			partCopy.setCurrentPower(new Power(properties.getInitialPower()));
			partCopy.setStartTime(part.getStartTime());
			partCopy.setEndTime(part.getEndTime());
			parts4PositionDetector.add(partCopy);
			idCounter++;
		}
		return parts4PositionDetector;
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
	}
}
