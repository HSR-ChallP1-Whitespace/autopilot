package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ChainTrackPartActorsMessage;
import ch.hsr.whitespace.javapilot.akka.messages.ChangePowerMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.LostPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.PrintTrackPositionMessage;
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

	private boolean iAmDriving = false;
	private boolean iAmSpeedingUp = false;
	private long trackPartEntryTime = 0;

	public static Props props(ActorRef pilot, TrackPart trackPart, int currentPower) {
		return Props.create(TrackPartDrivingActor.class, () -> new TrackPartDrivingActor(pilot, trackPart, currentPower));
	}

	public TrackPartDrivingActor(ActorRef pilot, TrackPart trackPart, int currentPower) {
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
			this.iAmSpeedingUp = ((SpeedupMessage) message).isSpeedup();
		}
	}

	private Power evaluateNewPower() {
		if (iAmSpeedingUp)
			return currentPower.increase(10);
		return currentPower;
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
		trackPartEntryTime = message.getTimestamp();
		tellParentToPrintPosition();
		setPower(evaluateNewPower());
	}

	private void handleLostPosition() {
		LOGGER.warn("Direction is not correct. Lost position!");
		resetPower();
		sendLostPositionMessage();
	}

	private void setPower(Power power) {
		currentPower = power;
		pilot.tell(new ChangePowerMessage(currentPower), getSelf());
	}

	private void sendLostPositionMessage() {
		getContext().parent().tell(new LostPositionMessage(), getSelf());
	}

	private boolean isValidDirection(TrackPartEnteredMessage message) {
		return message.getTrackPartDirection() == trackPart.getDirection();
	}

	private void leaveTrackPart(DirectionChangedMessage message) {
		LOGGER.info("Direction changed: " + message.getNewDirection() + " (leave track-part)");
		updateTrackPartTimestamps(message.getTimeStamp());
		notifyNextTrackPartActor(message);
		iAmDriving = false;
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
