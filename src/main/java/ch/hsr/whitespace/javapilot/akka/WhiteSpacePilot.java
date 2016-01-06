package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.RaceStartMessage;
import com.zuehlke.carrera.relayapi.messages.RaceStopMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ChangePowerMessage;
import ch.hsr.whitespace.javapilot.akka.messages.CheckedPatternsMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.InitializePositionDetection;
import ch.hsr.whitespace.javapilot.akka.messages.LostPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.RestartWithTrackRecognitionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackRecognitionFinished;
import ch.hsr.whitespace.javapilot.algorithms.MovingAverages;
import ch.hsr.whitespace.javapilot.config.PilotProperties;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.util.MessageUtil;
import scala.concurrent.duration.Duration;

/**
 * Main Pilot-Actor
 * 
 */
public class WhiteSpacePilot extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(WhiteSpacePilot.class);

	private static final int POWERUP_STEPS_IF_STARTING_PROBLEMS = 2;
	private static final int ADDITIONAL_POWER_INCREASE_AFTER_STARTING_PROBLEMS = 10;

	private ActorRef pilot;
	private ActorRef dataAnalyzerActor;
	private ActorRef trackRecognizerActor;
	private ActorRef drivingCoordinatorActor;
	private ActorRef dataSerializerActor;
	private ActorRef directionActor;
	private boolean trackRecognitionFinished = false;
	private Power currentPower;
	private Power initialPower;
	private boolean carStarted = false;
	private boolean hadStartingProblems = false;
	private List<String> alreadyCheckedPatterns;
	private Cancellable cancelledPowerChange;
	private MovingAverages gyrzValues;

	public WhiteSpacePilot(ActorRef pilot, PilotProperties properties) {
		this.pilot = pilot;
		this.initialPower = new Power(properties.getInitialPower());
		this.currentPower = new Power(properties.getInitialPower());
		this.alreadyCheckedPatterns = new ArrayList<>();
		this.gyrzValues = new MovingAverages();
	}

	public static Props props(ActorRef pilot, PilotProperties properties) {
		return Props.create(WhiteSpacePilot.class, () -> new WhiteSpacePilot(pilot, properties));
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (carStarted)
			forwardMessagesToChildren(message);
		if (message instanceof SensorEvent) {
			handleSensorEvent((SensorEvent) message);
		} else if (message instanceof TrackRecognitionFinished) {
			handleTrackRecognitionFinished((TrackRecognitionFinished) message);
		} else if (message instanceof ChangePowerMessage) {
			changePower((ChangePowerMessage) message);
		} else if (message instanceof RestartWithTrackRecognitionMessage) {
			restart();
		} else if (message instanceof CheckedPatternsMessage) {
			handleAlreadyCheckedPatterns((CheckedPatternsMessage) message);
		} else if (message instanceof LostPositionMessage) {
			handleLostPosition();
		} else {
			unhandled(message);
		}
	}

	private void handleLostPosition() {
		if (cancelledPowerChange != null)
			cancelledPowerChange.cancel();
	}

	private void handleAlreadyCheckedPatterns(CheckedPatternsMessage message) {
		this.alreadyCheckedPatterns.addAll(message.getCheckedPatterns());
	}

	private void restart() {
		LOGGER.warn("Restart whole track recognition :-/");
		restartDrivingActors();
		trackRecognitionFinished = false;
		setCurrentPower(initialPower);
	}

	private void forwardMessagesToChildren(Object message) {
		if (!MessageUtil.isMessageForwardNeeded(message, new Class[] { SensorEvent.class, VelocityMessage.class, DirectionChangedMessage.class, PenaltyMessage.class,
				RoundTimeMessage.class, RaceStartMessage.class, RaceStopMessage.class }))
			return;

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
		gyrzValues.shift(event.getG()[2]);
		pilot.tell(new PowerAction(currentPower.getValue()), getSelf());

		if (gyrzValues.isHistoryInitialized())
			checkIfCarIsStanding();
	}

	private void checkIfCarIsStanding() {
		if (gyrzValues.isCarStanding()) {
			handleCarStanding();
		} else if (!carStarted) {
			carStarted = true;
			increasePowerIfWeHadStartingProblems();
			handleRaceStarted();
		}
	}

	private void increasePowerIfWeHadStartingProblems() {
		if (hadStartingProblems) {
			initialPower = new Power(initialPower.getValue() + ADDITIONAL_POWER_INCREASE_AFTER_STARTING_PROBLEMS);
			setCurrentPower(initialPower);
		}
	}

	private void handleRaceStarted() {
		initChildActors();
	}

	private void handleCarStanding() {
		// did car already drived once? If not, we had starting problems
		// (initial-power too low)
		if (!carStarted) {
			hadStartingProblems = true;
			initialPower = new Power(initialPower.getValue() + POWERUP_STEPS_IF_STARTING_PROBLEMS);
		}
		setCurrentPower(initialPower);
	}

	private void changePower(ChangePowerMessage message) {
		if (message.getDelayInMillis() > 0) {
			this.cancelledPowerChange = getContext().system().scheduler().scheduleOnce(Duration.create(message.getDelayInMillis(), TimeUnit.MILLISECONDS), new Runnable() {
				@Override
				public void run() {
					getSelf().tell(new ChangePowerMessage(message.getNewPower()), getSelf());
				}
			}, getContext().dispatcher());
		} else {
			setCurrentPower(message.getNewPower());
		}
	}

	private void setCurrentPower(Power newPower) {
		currentPower = new Power(newPower);
		LOGGER.info("Set current power: " + currentPower.getValue());
	}

	private void restartDrivingActors() {
		if (!trackRecognitionFinished)
			getContext().stop(trackRecognizerActor);
		getContext().stop(drivingCoordinatorActor);
		startDrivingActors();
	}

	private void startDrivingActors() {
		this.trackRecognizerActor = getContext().actorOf(Props.create(TrackRecognizerActor.class, getSelf(), new ArrayList<String>(alreadyCheckedPatterns)));
		this.drivingCoordinatorActor = getContext().actorOf(Props.create(DrivingCoordinatorActor.class, getSelf(), initialPower.getValue()));
	}

	private void initChildActors() {
		this.dataAnalyzerActor = getContext().actorOf(Props.create(DataAnalyzerActor.class));
		this.dataSerializerActor = getContext().actorOf(Props.create(DataSerializerActor.class));
		this.directionActor = getContext().actorOf(Props.create(DirectionChangeRecognizerActor.class));
		startDrivingActors();
	}
}
