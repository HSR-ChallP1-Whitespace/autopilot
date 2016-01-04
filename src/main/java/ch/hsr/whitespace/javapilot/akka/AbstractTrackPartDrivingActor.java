package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ChainTrackPartActorsMessage;
import ch.hsr.whitespace.javapilot.akka.messages.ChangePowerMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.LostPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.PrintTrackPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupFactorFromNextPartMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackPartEnteredMessage;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public abstract class AbstractTrackPartDrivingActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(AbstractTrackPartDrivingActor.class);

	protected ActorRef pilot;
	protected ActorRef previousTrackPartActor;
	protected ActorRef nextTrackPartActor;
	protected boolean iAmDriving = false;
	protected boolean iAmSpeedingUp = false;
	protected boolean hasPenalty = false;
	protected boolean drivingWithConstantPower = true;
	protected long trackPartEntryTime = 0;
	protected TrackPart trackPart;
	protected Power initialPower;
	protected long lastDuration;
	protected Power currentPower;

	public static Class<? extends AbstractTrackPartDrivingActor> getDrivingActorClass(Direction direction) {
		if (direction == Direction.STRAIGHT) {
			return StraightDrivingActor.class;
		}
		return CurveDrivingActor.class;
	}

	public AbstractTrackPartDrivingActor(ActorRef pilot, TrackPart trackPart, int currentPower) {
		this.pilot = pilot;
		this.trackPart = trackPart;
		this.initialPower = new Power(currentPower);
		this.currentPower = new Power(currentPower);
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
		}
	}

	protected void evaluateAndSetNewPower() {
		// override in concrete implementation
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

	private void startSpeedingUp(SpeedupMessage message) {
		this.iAmSpeedingUp = message.isSpeedup();
		if (iAmSpeedingUp) {
			drivingWithConstantPower = false;
			LOGGER.info("Driver#" + trackPart.getId() + " is speeding up now ...");
		}
	}

	private boolean isValidDirection(TrackPartEnteredMessage message) {
		return message.getTrackPartDirection() == trackPart.getDirection();
	}

	private void handleLostPosition(TrackPartEnteredMessage message) {
		LOGGER.warn("Direction is not correct. Lost position! (Expected '" + trackPart.getDirection() + "' part now, but detected '" + message.getTrackPartDirection() + "'.)");
		resetPower();
		sendLostPositionMessage(message);
	}

	private void resetPower() {
		Power power = new Power(initialPower.getValue());
		LOGGER.info("Reset-Power: " + power.getValue());
		setPower(power);
	}

	private void sendLostPositionMessage(TrackPartEnteredMessage message) {
		getContext().parent().tell(new LostPositionMessage(message.getTimestamp(), trackPart.getDirection(), message.getTrackPartDirection()), getSelf());
	}

	protected void setPower(Power power) {
		LOGGER.info("Driver#" + trackPart.getId() + " SET POWER TO " + power.getValue());
		pilot.tell(new ChangePowerMessage(power), getSelf());
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

	private void tellSpeedupFactorToPreviousTrackPart(long lastDuration, long currentDuration) {
		previousTrackPartActor.tell(new SpeedupFactorFromNextPartMessage(lastDuration, currentDuration), getSelf());
	}
}
