package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ChainTrackPartActorsMessage;
import ch.hsr.whitespace.javapilot.akka.messages.ChangePowerMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.LostPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.PrintTrackPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupFactorFromNextPartMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupFinishedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackPartEnteredMessage;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class TrackPartDrivingActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(TrackPartDrivingActor.class);

	private ActorRef pilot;
	private TrackPart trackPart;
	private ActorRef previousTrackPartActor;
	private ActorRef nextTrackPartActor;
	private Power initialPower;
	private Power currentPower;
	private Power currentBrakeDownPower;

	private boolean iAmDriving = false;
	private boolean iAmSpeedingUp = false;
	private boolean drivingWithConstantPower = true;
	private boolean hasPenalty = false;
	private boolean lastTurnWasTooFast = false;

	private long lastDuration;
	private long trackPartEntryTime = 0;
	private long timeUntilBrake;

	private long initialDuration = 0;
	private double speedupFactor = 0.0;
	private double timeUtilBrakeDownFactor = 0.1;

	public static Props props(ActorRef pilot, TrackPart trackPart, int currentPower) {
		return Props.create(TrackPartDrivingActor.class, () -> new TrackPartDrivingActor(pilot, trackPart, currentPower));
	}

	public TrackPartDrivingActor(ActorRef pilot, TrackPart trackPart, int currentPower) {
		this.pilot = pilot;
		this.trackPart = trackPart;
		this.initialPower = new Power(currentPower);
		this.currentPower = new Power(currentPower);
		this.currentBrakeDownPower = new Power(currentPower);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof TrackPartEnteredMessage) {
			enterTrackPart((TrackPartEnteredMessage) message);
		} else if (message instanceof DirectionChangedMessage && iAmDriving) {
			leaveTrackPart((DirectionChangedMessage) message);
		} else if (message instanceof ChainTrackPartActorsMessage) {
			this.previousTrackPartActor = ((ChainTrackPartActorsMessage) message).getPreviousTrackPartActorRef();
			this.nextTrackPartActor = ((ChainTrackPartActorsMessage) message).getNextTrackPartActorRef();
		} else if (message instanceof SpeedupMessage) {
			startSpeedingUp((SpeedupMessage) message);
		} else if (message instanceof SpeedupFactorFromNextPartMessage) {
			handleSpeedupFactor((SpeedupFactorFromNextPartMessage) message);
		} else if (message instanceof PenaltyMessage && iAmDriving) {
			handlePenalty((PenaltyMessage) message);
		}
	}

	private void startSpeedingUp(SpeedupMessage message) {
		this.iAmSpeedingUp = message.isSpeedup();
		if (iAmSpeedingUp) {
			drivingWithConstantPower = false;
			LOGGER.info("Driver#" + trackPart.getId() + " is speeding up now ...");
		}
	}

	private void handlePenalty(PenaltyMessage message) {
		LOGGER.warn((char) 27 + "[31m" + "Driver #" + trackPart.getId() + ": got PENALTY" + (char) 27 + "[0m");
		hasPenalty = true;
		speedupFactor = (message.getActualSpeed() / message.getSpeedLimit()) - 1.0;
		handleTooHighSpeed();
	}

	private void handleTooHighSpeed() {
		lastTurnWasTooFast = true;
		if (!iAmSpeedingUp) {
			LOGGER.info("Driver#" + trackPart.getId() + " Last time until brake: " + timeUntilBrake);
			timeUntilBrake = timeUntilBrake + (int) (timeUntilBrake * -speedupFactor);
			LOGGER.info("Driver#" + trackPart.getId() + " New time until brake: " + timeUntilBrake + " (speedup-factor: " + speedupFactor + ")");
		}
	}

	private void handleSpeedupFactor(SpeedupFactorFromNextPartMessage message) {
		if (initialDuration == 0)
			initialDuration = message.getLastDuration();
		if (!hasPenalty) {
			speedupFactor = calcSpeedupFactor(message.getCurrentDuration());
		}
		if (iAmSpeedingUp)
			LOGGER.info("Driver#" + trackPart.getId() + " speedup: " + speedupFactor);
		if (speedupFactor >= 0.1 && !drivingWithConstantPower) {
			handleTooHighSpeed();
		}
	}

	private double calcSpeedupFactor(long currentDuration) {
		return (100 - ((100.0 / initialDuration) * currentDuration)) / 100.0;
	}

	private void evaluateAndSetNewPower() {
		if (iAmSpeedingUp) {
			Power maxPower = new Power(Power.MAX_POWER);
			LOGGER.info("Was last round too fast?: " + lastTurnWasTooFast);
			if (!lastTurnWasTooFast) {
				timeUtilBrakeDownFactor = timeUtilBrakeDownFactor + 0.1;
			} else if (canWeReduceBrakeDownPower()) {
				int brakeDownValue = (int) (Math.max(currentBrakeDownPower.getValue() * 0.1, 10));
				LOGGER.info("BRAKE DOWN VALUE: " + brakeDownValue);
				currentBrakeDownPower = new Power(currentBrakeDownPower.getValue() - brakeDownValue);
			} else {
				timeUtilBrakeDownFactor = timeUtilBrakeDownFactor - 0.1;
				stopSpeedup();
			}
			calculateTimeUntilBrake(currentPower.calcDiffFactor(maxPower));
			calculateBrakeDownPower();
			currentPower = maxPower;
		}
		scheduleBrake(timeUntilBrake, currentBrakeDownPower);
		setPower(currentPower);
		lastTurnWasTooFast = false;
	}

	private boolean canWeReduceBrakeDownPower() {
		return currentBrakeDownPower.getValue() > Power.MIN_POWER;
	}

	private void calculateBrakeDownPower() {
		currentBrakeDownPower = new Power(currentBrakeDownPower.getValue() + (int) (currentBrakeDownPower.getValue() * -speedupFactor));
		if (currentBrakeDownPower.getValue() < 25)
			currentBrakeDownPower = new Power(0);
		LOGGER.info("Calculated brake down power: " + currentBrakeDownPower.getValue());
	}

	private void calculateTimeUntilBrake(double powerDiffFactor) {
		timeUntilBrake = (long) (trackPart.getDuration() * timeUtilBrakeDownFactor * powerDiffFactor);
		LOGGER.info("Calculated time until brake: " + timeUntilBrake);
	}

	private void stopSpeedup() {
		if (iAmSpeedingUp) {
			getContext().parent().tell(new SpeedupFinishedMessage(trackPart), getSelf());
			LOGGER.info("Driver#" + trackPart.getId() + " stopped speeding up ...");
		}
		iAmSpeedingUp = false;
	}

	private void scheduleBrake(long timeUntilBrake, Power brakeDownPower) {
		if (brakeDownPower.getValue() == currentPower.getValue())
			return;
		pilot.tell(new ChangePowerMessage(brakeDownPower, timeUntilBrake), getSelf());
	}

	private void resetPower() {
		Power power = new Power(initialPower.getValue());
		LOGGER.info("Reset-Power: " + power.getValue());
		setPower(power);
	}

	private void enterTrackPart(TrackPartEnteredMessage message) {
		// LOGGER.info("DRIVER#" + trackPart.getId() + "(" + getSelf() + ") GOT
		// ENTERED MESSAGE WITH DIRECTION '" + message.getTrackPartDirection() +
		// "' FROM SENDER '" + getSender()
		// + "'");
		if (!isValidDirection(message)) {
			handleLostPosition(message);
			return;
		}
		iAmDriving = true;
		hasPenalty = false;
		trackPartEntryTime = message.getTimestamp();
		tellParentToPrintPosition();
		if (!message.isPositionCorrectionMessage())
			evaluateAndSetNewPower();
	}

	private void handleLostPosition(TrackPartEnteredMessage message) {
		LOGGER.warn("Direction is not correct. Lost position! (Expected '" + trackPart.getDirection() + "' part now, but detected '" + message.getTrackPartDirection() + "'.)");
		resetPower();
		sendLostPositionMessage(message);
	}

	private void setPower(Power power) {
		LOGGER.info("Driver#" + trackPart.getId() + " SET POWER TO " + power.getValue());
		pilot.tell(new ChangePowerMessage(power), getSelf());
	}

	private void sendLostPositionMessage(TrackPartEnteredMessage message) {
		getContext().parent().tell(new LostPositionMessage(message.getTimestamp(), trackPart.getDirection(), message.getTrackPartDirection()), getSelf());
	}

	private boolean isValidDirection(TrackPartEnteredMessage message) {
		return message.getTrackPartDirection() == trackPart.getDirection();
	}

	private void leaveTrackPart(DirectionChangedMessage message) {
		// LOGGER.info(
		// "DRIVER#" + trackPart.getId() + "(" + getSelf() + ") GOT LEAVED
		// MESSAGE WITH NEW DIRECTION '" + message.getNewDirection() + "' FROM
		// SENDER '" + getSender() + "'");
		iAmDriving = false;
		notifyNextTrackPartActor(message);
		lastDuration = trackPart.getDuration();
		updateTrackPartTimestamps(message.getTimeStamp());
		tellSpeedupFactorToPreviousTrackPart(lastDuration, trackPart.getDuration());
	}

	private void tellSpeedupFactorToPreviousTrackPart(long lastDuration, long currentDuration) {
		previousTrackPartActor.tell(new SpeedupFactorFromNextPartMessage(lastDuration, currentDuration), getSelf());
	}

	private void tellParentToPrintPosition() {
		getContext().parent().tell(new PrintTrackPositionMessage(trackPart.getId()), getSelf());
	}

	private void notifyNextTrackPartActor(DirectionChangedMessage message) {
		nextTrackPartActor.tell(new TrackPartEnteredMessage(message.getTimeStamp(), message.getNewDirection()), getSelf());
	}

	private void updateTrackPartTimestamps(long currentTimeStamp) {
		if (trackPartEntryTime != 0) {
			trackPart.setStartTime(trackPartEntryTime);
			trackPart.setEndTime(currentTimeStamp);
		}
	}

}
