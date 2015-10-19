package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.BrakeMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChanged;
import ch.hsr.whitespace.javapilot.akka.messages.TrackRecognitionFinished;
import ch.hsr.whitespace.javapilot.config.PilotProperties;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;
import scala.concurrent.duration.Duration;

/**
 * Main Pilot-Actor
 * 
 */
public class WhiteSpacePilot extends UntypedActor {

	private static final double ACCELERATION_DURATION_FACTOR = 0.5;

	private final Logger LOGGER = LoggerFactory.getLogger(WhiteSpacePilot.class);

	private PilotProperties properties;

	private ActorRef pilot;
	private ActorRef dataAnalyzerActor;
	private ActorRef trackRecognizerActor;
	private ActorRef positionCalculatorActor;

	private List<TrackPart> trackParts;

	private int currentPower;

	public WhiteSpacePilot(ActorRef pilot, PilotProperties properties) {
		this.pilot = pilot;
		this.properties = properties;
		this.currentPower = properties.getInitialPower();
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
		} else if (message instanceof DirectionChanged) {
			handleDirectionChanged((DirectionChanged) message);
		} else if (message instanceof BrakeMessage) {
			handleBrake((BrakeMessage) message);
		} else {
			unhandled(message);
		}
	}

	private void handleBrake(BrakeMessage message) {
		LOGGER.info("Brake: " + message.getReducedPower());
		setCurrentPower(message.getReducedPower());
	}

	private void handleDirectionChanged(DirectionChanged message) {
		if (message.getTrackPart().getDirection() == Direction.STRAIGHT) {
			message.getTrackPart().accelerate(20);
			scheduleBrake((long) (message.getTrackPart().getDuration() * ACCELERATION_DURATION_FACTOR), message.getNextTrackPart().getCurrentPower());
		}
		setCurrentPower(message.getTrackPart().getCurrentPower());
	}

	private void scheduleBrake(long time, int power) {
		getContext().system().scheduler().scheduleOnce(Duration.create(time, TimeUnit.MILLISECONDS), new Runnable() {
			@Override
			public void run() {
				getSelf().tell(new BrakeMessage(power), ActorRef.noSender());
			}
		}, getContext().dispatcher());
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
		this.trackParts = new ArrayList<TrackPart>(((TrackRecognitionFinished) message).getTrackParts());
		initializeTrackPartPower();
		trackRecognizerActor.tell(PoisonPill.getInstance(), getSelf());
		positionCalculatorActor.tell(message, getSelf());
	}

	private void initializeTrackPartPower() {
		for (TrackPart part : trackParts) {
			part.setCurrentPower(properties.getInitialPower());
		}
	}

	private void handleSensorEvent(SensorEvent event) {
		pilot.tell(new PowerAction(currentPower), getSelf());
	}

	private void setCurrentPower(int power) {
		currentPower = power;
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
}
