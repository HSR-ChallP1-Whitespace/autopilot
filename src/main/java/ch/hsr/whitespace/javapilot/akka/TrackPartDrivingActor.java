package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ChangePowerMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.LostPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.PrintTrackPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SetNextTrackPartActorMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackPartEnteredMessage;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class TrackPartDrivingActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(TrackPartDrivingActor.class);
	private static final long MIN_STRAIGHT_DURATION_FOR_SPEEDUP = 600;

	private ActorRef pilot;
	private TrackPart trackPart;
	private ActorRef nextTrackPartActor;
	private Power currentPower;

	private boolean iAmDriving = false;
	private long trackPartEntryTime = 0;

	public static Props props(ActorRef pilot, TrackPart trackPart, int currentPower) {
		return Props.create(TrackPartDrivingActor.class, () -> new TrackPartDrivingActor(pilot, trackPart, currentPower));
	}

	public TrackPartDrivingActor(ActorRef pilot, TrackPart trackPart, int currentPower) {
		this.pilot = pilot;
		this.trackPart = trackPart;
		this.currentPower = new Power(currentPower);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof TrackPartEnteredMessage) {
			enterTrackPart((TrackPartEnteredMessage) message);
		} else if (message instanceof DirectionChangedMessage && iAmDriving) {
			leaveTrackPart((DirectionChangedMessage) message);
		} else if (message instanceof SetNextTrackPartActorMessage) {
			this.nextTrackPartActor = ((SetNextTrackPartActorMessage) message).getNextTrackPartActorRef();
		}
	}

	private Power evaluateNewPower() {
		if (trackPart.getDirection() == Direction.STRAIGHT && trackPart.getDuration() > MIN_STRAIGHT_DURATION_FOR_SPEEDUP)
			return currentPower.increase(10);
		return currentPower;
	}

	private void enterTrackPart(TrackPartEnteredMessage message) {
		if (!isValidDirection(message)) {
			LOGGER.warn("Direction is not correct. Lost position!");
			sendLostPositionMessage();
			return;
		}
		iAmDriving = true;
		trackPartEntryTime = message.getTimestamp();
		tellParentToPrintPosition();
		setPower(evaluateNewPower());
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
