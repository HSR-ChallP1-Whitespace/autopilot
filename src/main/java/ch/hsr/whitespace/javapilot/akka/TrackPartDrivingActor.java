package ch.hsr.whitespace.javapilot.akka;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.BrakeDownMessage;
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
import scala.concurrent.duration.Duration;

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
	private long trackPartEntryTime = 0;
	private long timeUntilBrake;

	private long initialDuration = 0;
	private double speedupFactor = 0.0;

	private boolean hasPenalty = false;

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
			this.iAmSpeedingUp = ((SpeedupMessage) message).isSpeedup();
		} else if (message instanceof SpeedupFactorFromNextPartMessage && iAmSpeedingUp) {
			handleSpeedupFactor((SpeedupFactorFromNextPartMessage) message);
		} else if (message instanceof BrakeDownMessage) {
			brakeDown((BrakeDownMessage) message);
		} else if (message instanceof PenaltyMessage && iAmDriving) {
			LOGGER.warn("Driver #" + trackPart.getId() + ": got PENALTY");
			handlePenalty((PenaltyMessage) message);
		}
	}

	private void handlePenalty(PenaltyMessage message) {
		hasPenalty = true;
		currentBrakeDownPower = currentBrakeDownPower.reduce(20);
	}

	private void brakeDown(BrakeDownMessage message) {
		LOGGER.info("Brake down: " + message.getBrakeDownPower());
		setPower(message.getBrakeDownPower());
	}

	private void handleSpeedupFactor(SpeedupFactorFromNextPartMessage message) {
		if (initialDuration == 0)
			initialDuration = message.getLastDuration();
		if (!hasPenalty) {
			this.speedupFactor = calcSpeedupFactor(message.getCurrentDuration());
			LOGGER.info("Speedup at the end of trackpart was: " + this.speedupFactor + " (initialDuration=" + initialDuration + ", currentDuration=" + message.getCurrentDuration()
					+ ")");
		}
	}

	private double calcSpeedupFactor(long currentDuration) {
		return (100 - ((100.0 / initialDuration) * currentDuration)) / 100.0;
	}

	private void evaluateAndSetNewPower() {
		if (iAmSpeedingUp) {
			Power newPower = currentPower.increase(100);
			timeUntilBrake = calculateTimeUntilBrake(currentPower.calcDiffFactor(newPower));
			currentBrakeDownPower = calculateBrakeDownPower();
			currentPower = newPower;
			if (currentPower.getValue() == Power.MAX_POWER)
				stopSpeedup();
		}
		scheduleBrake(timeUntilBrake, currentBrakeDownPower);
		setPower(currentPower);
	}

	private Power calculateBrakeDownPower() {
		Power power = new Power(currentBrakeDownPower.getValue() + (int) (currentBrakeDownPower.getValue() * -speedupFactor));
		LOGGER.info("Calculated brake down power: " + power.getValue());
		return power;
	}

	private long calculateTimeUntilBrake(double powerDiffFactor) {
		long result = (long) (trackPart.getDuration() * 0.5 * powerDiffFactor);
		LOGGER.info("Calculated time until brake: " + result);
		return result;
	}

	private void stopSpeedup() {
		iAmSpeedingUp = false;
		getContext().parent().tell(new SpeedupFinishedMessage(trackPart), getSelf());
	}

	private void scheduleBrake(long timeUntilBrake, Power brakeDownPower) {
		if (brakeDownPower.getValue() == currentPower.getValue())
			return;

		getContext().system().scheduler().scheduleOnce(Duration.create(timeUntilBrake, TimeUnit.MILLISECONDS), new Runnable() {
			@Override
			public void run() {
				getSelf().tell(new BrakeDownMessage(brakeDownPower), getSelf());
			}
		}, getContext().dispatcher());
	}

	private void resetPower() {
		setPower(initialPower);
	}

	private void enterTrackPart(TrackPartEnteredMessage message) {
		if (!isValidDirection(message)) {
			handleLostPosition();
			return;
		}
		iAmDriving = true;
		hasPenalty = false;
		trackPartEntryTime = message.getTimestamp();
		tellParentToPrintPosition();
		evaluateAndSetNewPower();
	}

	private void handleLostPosition() {
		LOGGER.warn("Direction is not correct. Lost position!");
		resetPower();
		sendLostPositionMessage();
	}

	private void setPower(Power power) {
		pilot.tell(new ChangePowerMessage(power), getSelf());
	}

	private void sendLostPositionMessage() {
		getContext().parent().tell(new LostPositionMessage(), getSelf());
	}

	private boolean isValidDirection(TrackPartEnteredMessage message) {
		return message.getTrackPartDirection() == trackPart.getDirection();
	}

	private void leaveTrackPart(DirectionChangedMessage message) {
		long lastDuration = trackPart.getDuration();
		updateTrackPartTimestamps(message.getTimeStamp());
		tellSpeedupFactorToPreviousTrackPart(lastDuration, trackPart.getDuration());
		notifyNextTrackPartActor(message);
		iAmDriving = false;
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
