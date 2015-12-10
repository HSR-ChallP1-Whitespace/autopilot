package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ChangePowerMessage;
import ch.hsr.whitespace.javapilot.akka.messages.InitializePositionDetection;
import ch.hsr.whitespace.javapilot.akka.messages.RestartWithTrackRecognitionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackRecognitionFinished;
import ch.hsr.whitespace.javapilot.config.PilotProperties;
import ch.hsr.whitespace.javapilot.model.Power;

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
	private ActorRef drivingCoordinatorActor;
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
		} else if (message instanceof ChangePowerMessage) {
			setCurrentPower(((ChangePowerMessage) message).getNewPower());
		} else if (message instanceof RestartWithTrackRecognitionMessage) {
			restart();
		} else {
			unhandled(message);
		}
	}

	private void restart() {
		LOGGER.warn("Restart whole track recognition :-/");
		trackRecognitionFinished = false;
		currentPower = new Power(properties.getInitialPower());
		restartDrivingActors();
	}

	private void forwardMessagesToChildren(Object message) {
		dataAnalyzerActor.forward(message, getContext());
		dataSerializerActor.forward(message, getContext());
		directionActor.forward(message, getContext());
		if (!isTrackRecognitionFinished())
			trackRecognizerActor.forward(message, getContext());
		if (isTrackRecognitionFinished())
			drivingCoordinatorActor.forward(message, getContext());
	}

	private boolean isTrackRecognitionFinished() {
		return trackRecognitionFinished;
	}

	private void handleTrackRecognitionFinished(TrackRecognitionFinished message) {
		trackRecognitionFinished = true;
		getContext().stop(trackRecognizerActor);
		drivingCoordinatorActor.tell(new InitializePositionDetection(message.getTrackParts()), getSelf());
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

	private void restartDrivingActors() {
		getContext().stop(trackRecognizerActor);
		getContext().stop(drivingCoordinatorActor);
		startDrivingActors();
	}

	private void startDrivingActors() {
		this.trackRecognizerActor = getContext().actorOf(Props.create(TrackRecognizerActor.class));
		this.drivingCoordinatorActor = getContext().actorOf(Props.create(DrivingCoordinatorActor.class, properties.getInitialPower()));
	}

	private void initChildActors() {
		this.dataAnalyzerActor = getContext().actorOf(Props.create(DataAnalyzerActor.class));
		this.dataSerializerActor = getContext().actorOf(Props.create(DataSerializerActor.class));
		this.directionActor = getContext().actorOf(Props.create(DirectionChangeRecognizerActor.class));
		startDrivingActors();
	}
}
