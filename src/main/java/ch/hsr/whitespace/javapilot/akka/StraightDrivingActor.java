package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import ch.hsr.whitespace.javapilot.akka.messages.ChangePowerMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupFactorFromNextPartMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupFinishedMessage;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class StraightDrivingActor extends AbstractTrackPartDrivingActor {

	private final Logger LOGGER = LoggerFactory.getLogger(StraightDrivingActor.class);

	private Power currentBrakeDownPower;
	private boolean lastTurnWasTooFast = false;
	private long timeUntilBrake;
	private long initialDuration = 0;
	private double speedupFactor = 0.0;
	private double timeUtilBrakeDownFactor = 0.1;

	public static Props props(ActorRef pilot, TrackPart trackPart, int currentPower) {
		return Props.create(StraightDrivingActor.class, () -> new StraightDrivingActor(pilot, trackPart, currentPower));
	}

	public StraightDrivingActor(ActorRef pilot, TrackPart trackPart, int currentPower) {
		super(pilot, trackPart, currentPower);
		this.currentBrakeDownPower = new Power(currentPower);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		super.onReceive(message);
		if (message instanceof SpeedupFactorFromNextPartMessage) {
			handleSpeedupFactor((SpeedupFactorFromNextPartMessage) message);
		} else if (message instanceof PenaltyMessage && iAmDriving) {
			handlePenalty((PenaltyMessage) message);
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

	@Override
	protected void evaluateAndSetNewPower() {
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

}
