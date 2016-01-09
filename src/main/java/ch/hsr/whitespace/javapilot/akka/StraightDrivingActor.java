package ch.hsr.whitespace.javapilot.akka;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import ch.hsr.whitespace.javapilot.akka.messages.ChainTrackPartActorsMessage;
import ch.hsr.whitespace.javapilot.akka.messages.ChangePowerMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DurationFromNextPartMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupFinishedMessage;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class StraightDrivingActor extends AbstractTrackPartDrivingActor {

	private final Logger LOGGER = LoggerFactory.getLogger(StraightDrivingActor.class);

	private static final int MINIMAL_BRAKE_DOWN_POWER = 30;
	private static final double SPEEDUP_DURATION_INCREASE_STEPS = 0.05;
	private static final double SPEEDUP_DURATION_DECREASE_STEPS = 0.1;

	private static final double DURATION_INCREASE_STEPS = 0.01;

	private Power currentBrakeDownPower;
	private long timeUntilBrake;
	private Map<Integer, PenaltyMessage> penalties;
	private Map<Integer, DurationFromNextPartMessage> nextTrackPartDurations;
	private double timeUntilBrakeDownFactor = 0.1;
	private boolean speedupPhaseFinished = false;

	public static Props props(ActorRef pilot, TrackPart trackPart, int currentPower) {
		return Props.create(StraightDrivingActor.class, () -> new StraightDrivingActor(pilot, trackPart, currentPower));
	}

	public StraightDrivingActor(ActorRef pilot, TrackPart trackPart, int currentPower) {
		super(pilot, trackPart, currentPower);
		this.currentBrakeDownPower = new Power(currentPower);
		this.penalties = new HashMap<>();
		this.nextTrackPartDurations = new HashMap<>();
	}

	@Override
	public void onReceive(Object message) throws Exception {
		super.onReceive(message);
		if (message instanceof DurationFromNextPartMessage) {
			handleSpeedupFactor((DurationFromNextPartMessage) message);
		} else if (message instanceof PenaltyMessage && iAmDriving) {
			handlePenalty((PenaltyMessage) message);
		} else if (message instanceof ChainTrackPartActorsMessage) {
			this.nextTrackPartDurations.put(0, new DurationFromNextPartMessage(((ChainTrackPartActorsMessage) message).getNextTrackPart().getDuration()));
		}
	}

	private void handlePenalty(PenaltyMessage message) {
		LOGGER.warn((char) 27 + "[31m" + "Driver #" + trackPart.getId() + ": got PENALTY" + (char) 27 + "[0m");
		penalties.put(this.roundCounter, message);
	}

	private void handleSpeedupFactor(DurationFromNextPartMessage message) {
		nextTrackPartDurations.put(roundCounter, message);
	}

	private double calcSpeedupFactorByTrackDurations() {
		if (!nextTrackPartDurations.containsKey(0)) {
			LOGGER.error("Driver#" + trackPart.getId() + " The initial track-part-duration does not exist!");
			return 0.0;
		}
		if (!nextTrackPartDurations.containsKey(roundCounter - 1)) {
			LOGGER.error("Driver#" + trackPart.getId() + " The track-part-duration for the last round does not exist!");
			return 0.0;
		}
		DurationFromNextPartMessage initialDuration = nextTrackPartDurations.get(0);
		DurationFromNextPartMessage calculationDuration = nextTrackPartDurations.get(roundCounter - 1);
		double result = (100 - ((100.0 / initialDuration.getDuration()) * calculationDuration.getDuration())) / 100.0;
		LOGGER.info("Driver#" + trackPart.getId() + " CALCULATE SPEEDUP-FACTOR = " + result + " based on TRACK-PART-DURATIONS (initial-duration=" + initialDuration.getDuration()
				+ ", last-duration=" + calculationDuration.getDuration() + ")");
		return result;
	}

	private double calcSpeedupFactorByPenalty() {
		if (!penalties.containsKey(roundCounter - 1)) {
			LOGGER.error("There is no penalty-message!");
			return 0.0;
		}
		PenaltyMessage calculationPenalty = penalties.get(roundCounter - 1);
		int penaltyAmount = getAmountOfPenaltiesLastRounds();
		double result = ((100 - ((100.0 / calculationPenalty.getActualSpeed()) * calculationPenalty.getSpeedLimit())) / 100.0) * penaltyAmount;
		LOGGER.info("Driver#" + trackPart.getId() + " CALCULATE SPEEDUP-FACTOR = " + result + " based on PENALTY (max-allowed-speed=" + calculationPenalty.getSpeedLimit()
				+ ", our-speed=" + calculationPenalty.getActualSpeed() + ", amount-penalties=" + penaltyAmount + ")");
		return result;
	}

	private int getAmountOfPenaltiesLastRounds() {
		if (!penalties.containsKey(roundCounter - 1))
			return 0;
		int amount = 1;
		while (penalties.containsKey(roundCounter - amount))
			amount++;
		return amount;
	}

	@Override
	protected void evaluateAndSetNewPower() {
		if (iAmSpeedingUp) {
			setMaxPower();
			if (wereWeTooFastLastRound() && canWeReduceBrakeDownPower()) {
				reduceBrakeDownPower();
			} else if (wereWeTooFastLastRound()) {
				decreaseTimeUntilBrake();
				stopSpeedup();
			} else {
				increaseTimeUntilBrake();
			}
		} else if (speedupPhaseFinished) {
			if (wereWeTooFastLastRound()) {
				decreaseTimeUntilBrake();
			} else if (wereWeTooSlowLastRound()) {
				increaseTimeUntilBrake();
			}
		}
		setPower(currentPower);
		scheduleBrake(timeUntilBrake, currentBrakeDownPower);
	}

	private void setMaxPower() {
		if (currentPower.getValue() < Power.MAX_POWER) {
			currentPower = new Power(Power.MAX_POWER);
		}
	}

	private boolean wereWeTooFastLastRound() {
		return didWeHadPenaltyLastRound() || calcSpeedupFactorByTrackDurations() > 0.0;
	}

	private boolean wereWeTooSlowLastRound() {
		return calcSpeedupFactorByTrackDurations() < 0.1;
	}

	private boolean canWeReduceBrakeDownPower() {
		return currentBrakeDownPower.getValue() > MINIMAL_BRAKE_DOWN_POWER;
	}

	private void reduceBrakeDownPower() {
		double factor = 1.0;
		if (didWeHadPenaltyLastRound()) {
			factor = calcSpeedupFactorByPenalty();
		} else {
			factor = calcSpeedupFactorByTrackDurations();
		}
		int calculatedBrakeDownPower = currentBrakeDownPower.getValue() + (int) (currentBrakeDownPower.getValue() * -factor);
		currentBrakeDownPower = new Power(Math.max(calculatedBrakeDownPower, MINIMAL_BRAKE_DOWN_POWER));
		LOGGER.info("Driver#" + trackPart.getId() + " calculated brake-down-power=" + currentBrakeDownPower + " (based on factor=" + factor + ")");
	}

	private void increaseTimeUntilBrake() {
		if (iAmSpeedingUp)
			timeUntilBrakeDownFactor = timeUntilBrakeDownFactor + SPEEDUP_DURATION_INCREASE_STEPS;
		else
			timeUntilBrakeDownFactor = timeUntilBrakeDownFactor + DURATION_INCREASE_STEPS;
		calculateTimeUntilBrake();
	}

	private void decreaseTimeUntilBrake() {
		timeUntilBrakeDownFactor = timeUntilBrakeDownFactor - SPEEDUP_DURATION_DECREASE_STEPS;
		calculateTimeUntilBrake();
	}

	private void calculateTimeUntilBrake() {
		timeUntilBrake = (long) (trackPart.getDuration() * timeUntilBrakeDownFactor);
		LOGGER.info("Driver#" + trackPart.getId() + " Calculated time until brake: " + timeUntilBrake + " (based on duration=" + trackPart.getDuration() + ", timeUntilBrakeFactor="
				+ timeUntilBrakeDownFactor + ")");
	}

	private boolean didWeHadPenaltyLastRound() {
		return this.penalties.containsKey(this.roundCounter - 1);
	}

	private void stopSpeedup() {
		if (iAmSpeedingUp) {
			getContext().parent().tell(new SpeedupFinishedMessage(trackPart), getSelf());
			LOGGER.info("Driver#" + trackPart.getId() + " stopped speeding up ...");
			speedupPhaseFinished = true;
		}
		iAmSpeedingUp = false;
	}

	private void scheduleBrake(long timeUntilBrake, Power brakeDownPower) {
		if (brakeDownPower.getValue() == currentPower.getValue())
			return;
		pilot.tell(new ChangePowerMessage(brakeDownPower, timeUntilBrake), getSelf());
	}

}
