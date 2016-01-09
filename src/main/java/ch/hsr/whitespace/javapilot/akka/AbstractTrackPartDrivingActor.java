package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ChainTrackPartActorsMessage;
import ch.hsr.whitespace.javapilot.akka.messages.ChangePowerMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DurationFromNextPartMessage;
import ch.hsr.whitespace.javapilot.akka.messages.LostPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.PrintTrackPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupMessage;
import ch.hsr.whitespace.javapilot.akka.messages.StartDrivingMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackPartEnteredMessage;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public abstract class AbstractTrackPartDrivingActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(AbstractTrackPartDrivingActor.class);

	protected ActorRef pilot;
	protected TrackPart trackPart;
	protected TrackPart previousTrackPart;
	protected ActorRef previousTrackPartActor;
	protected TrackPart nextTrackPart;
	protected ActorRef nextTrackPartActor;
	protected boolean trackPartEntered = false;
	protected boolean iAmDriving = false;
	protected boolean iAmSpeedingUp = false;
	protected boolean drivingWithConstantPower = true;
	protected long trackPartEntryTime = 0;
	protected Power initialPower;
	protected Power currentPower;
	protected int roundCounter = 0;

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
		} else if (message instanceof DirectionChangedMessage && trackPartEntered) {
			leaveTrackPart((DirectionChangedMessage) message);
		} else if (message instanceof StartDrivingMessage) {
			startDriving();
		} else if (message instanceof ChainTrackPartActorsMessage) {
			handleTrackPartChaining((ChainTrackPartActorsMessage) message);
		} else if (message instanceof SpeedupMessage) {
			startSpeedingUp((SpeedupMessage) message);
		}
	}

	protected void evaluateAndSetNewPower() {
		// override in concrete implementation
	}

	protected void enterTrackPart(TrackPartEnteredMessage message) {
		LOGGER.info("Driver#" + trackPart.getId() + " got TrackPartEnteredMessage");
		if (!isValidDirection(message)) {
			handleLostPosition(message);
			return;
		}
		trackPartEntered = true;
		trackPartEntryTime = message.getTimestamp();
		tellParentToPrintPosition();
		if (!message.isPositionCorrectionMessage())
			startDriving();
	}

	protected void startDriving() {
		if (!iAmDriving) {
			roundCounter++;
			iAmDriving = true;
			evaluateAndSetNewPower();
		}
	}

	protected void stopDriving() {
		iAmDriving = false;
	}

	protected void leaveTrackPart(DirectionChangedMessage message) {
		LOGGER.info("Driver#" + trackPart.getId() + " leave TrackPart now");
		trackPartEntered = false;
		stopDriving();
		enterNextTrackPart(message);
		tellNextTrackPartToDrive();
		updateTrackPartTimestamps(message.getTimeStamp());
		tellSpeedupFactorToPreviousTrackPart(trackPart.getDuration());
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
		LOGGER.warn("Driver#" + trackPart.getId() + " Direction is not correct. Lost position! (Expected '" + trackPart.getDirection() + "' part now, but detected '"
				+ message.getTrackPartDirection() + "'.)");
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

	private void enterNextTrackPart(DirectionChangedMessage message) {
		nextTrackPartActor.tell(new TrackPartEnteredMessage(message.getTimeStamp(), message.getNewDirection()), getSelf());
	}

	protected void tellNextTrackPartToDrive() {
		nextTrackPartActor.tell(new StartDrivingMessage(), getSelf());
	}

	private void updateTrackPartTimestamps(long currentTimeStamp) {
		if (trackPartEntryTime != 0) {
			trackPart.setStartTime(trackPartEntryTime);
			trackPart.setEndTime(currentTimeStamp);
			LOGGER.info("Driver#" + trackPart.getId() + " updated time-stamps (start=" + trackPart.getStartTime() + ", end=" + trackPart.getEndTime() + ", duration="
					+ trackPart.getDuration() + ")");
		}
	}

	private void tellSpeedupFactorToPreviousTrackPart(long currentDuration) {
		previousTrackPartActor.tell(new DurationFromNextPartMessage(currentDuration), getSelf());
	}

	private void handleTrackPartChaining(ChainTrackPartActorsMessage message) {
		this.previousTrackPart = message.getPreviousTrackPart();
		this.previousTrackPartActor = message.getPreviousTrackPartActorRef();
		this.nextTrackPart = message.getNextTrackPart();
		this.nextTrackPartActor = message.getNextTrackPartActorRef();
	}
}
