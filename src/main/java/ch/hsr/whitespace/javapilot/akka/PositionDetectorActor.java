package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import com.zuehlke.carrera.timeseries.FloatingHistory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChanged;
import ch.hsr.whitespace.javapilot.akka.messages.InitializePositionDetection;
import ch.hsr.whitespace.javapilot.akka.messages.PowerChangeMessage;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.driving.DrivingTrackPart;
import ch.hsr.whitespace.javapilot.model.track.driving.DrivingVelocityBarrier;
import ch.hsr.whitespace.javapilot.model.track.driving.WrongTrackPartException;

public class PositionDetectorActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(PositionDetectorActor.class);

	private Map<Integer, DrivingTrackPart> trackParts;
	private DrivingTrackPart currentTrackPart;
	private int currentTrackPartId = 1;
	private FloatingHistory smoothedValues;
	private List<DrivingVelocityBarrier> barriers;
	private Map<Integer, DrivingTrackPart> barrierIndexToTrackPartMap;
	private int lastBarrierIndex = 0;

	private long trackPartEntryTime = 0;
	private long trackPartEntryTimeLocal = 0;

	public static Props props(ActorRef pilot) {
		return Props.create(PositionDetectorActor.class, () -> new PositionDetectorActor());
	}

	public PositionDetectorActor() {
		smoothedValues = new FloatingHistory(8);
		trackParts = new TreeMap<>();
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof InitializePositionDetection) {
			initializeTrackPartMap(((InitializePositionDetection) message).getTrackParts());
			initializeBarriers();
			currentTrackPart = trackParts.get(currentTrackPartId);
		} else if (message instanceof PowerChangeMessage) {
			handleAccelerateMessage((PowerChangeMessage) message);
		} else if (message instanceof SensorEvent && trackParts != null) {
			handleSensorEvent((SensorEvent) message);
		} else if (message instanceof VelocityMessage) {
			handleVelocityMessage((VelocityMessage) message);
		} else if (message instanceof PenaltyMessage) {
			handlePenalty((PenaltyMessage) message);
		}
	}

	private void handleVelocityMessage(VelocityMessage message) {
		try {
			currentTrackPart = barrierIndexToTrackPartMap.get(lastBarrierIndex);
			currentTrackPartId = currentTrackPart.getId();
			incrementBarrierIndex();
			currentTrackPart.passedVelocityBarrier();
		} catch (WrongTrackPartException e) {
			LOGGER.error(e.getMessage());
		}
	}

	private void incrementBarrierIndex() {
		if (lastBarrierIndex == (barriers.size() - 1)) {
			lastBarrierIndex = 0;
		} else {
			lastBarrierIndex++;
		}
	}

	private void handlePenalty(PenaltyMessage message) {
		LOGGER.warn("PENALTY[" + System.currentTimeMillis() + "]: actualSpeed=" + message.getActualSpeed() + ", speedlimit=" + message.getSpeedLimit() + ", penalty_ms="
				+ message.getPenalty_ms() + ", racetrack=" + message.getRaceTrack() + ", barrier=" + message.getBarrier());
		currentTrackPart.handlePenalty(message.getActualSpeed(), message.getSpeedLimit());
	}

	private void handleAccelerateMessage(PowerChangeMessage accelerateMessage) {
		trackParts.get(accelerateMessage.getTrackPartId()).setCurrentPower(accelerateMessage.getPower());
	}

	private void handleSensorEvent(SensorEvent message) {
		smoothedValues.shift(message.getG()[2]);
		Direction newDirection = Direction.getNewDirection(currentTrackPart.getDirection(), smoothedValues.currentMean(), smoothedValues.currentStDev());
		if (hasDirectionChanged(newDirection)) {
			updateTrackPartTimestamps(message.getTimeStamp(), System.currentTimeMillis());
			incrementCurrentTrack(newDirection);
			handleDirectionChange();
		}
	}

	private void updateTrackPartTimestamps(long currentTimeStamp, long currentTimeStampLocal) {
		if (trackPartEntryTime != 0) {
			currentTrackPart.setStartTime(trackPartEntryTime);
			currentTrackPart.setEndTime(currentTimeStamp);
		}
		if (trackPartEntryTimeLocal != 0) {
			currentTrackPart.setLocalStartTime(trackPartEntryTimeLocal);
			currentTrackPart.setLocalEndTime(currentTimeStampLocal);
		}
		trackPartEntryTime = currentTimeStamp;
		trackPartEntryTimeLocal = currentTimeStampLocal;
	}

	private void handleDirectionChange() {
		LOGGER.info("Position: " + getCurrentPositionString());
		getContext().parent().tell(createDirectionChangedEvent(), getSelf());
	}

	private DirectionChanged createDirectionChangedEvent() {
		return new DirectionChanged(currentTrackPart, trackParts.get(getNextTrackPartId()));
	}

	private boolean hasDirectionChanged(Direction newDirection) {
		return newDirection != currentTrackPart.getDirection();
	}

	private void incrementCurrentTrack(Direction newDirection) {
		currentTrackPartId = getNextTrackPartId();
		currentTrackPart = trackParts.get(currentTrackPartId);
		if (currentTrackPart.getDirection() != newDirection)
			incrementCurrentTrack(newDirection);
	}

	private int getNextTrackPartId() {
		if (currentTrackPartId == trackParts.size())
			return 1;
		else
			return currentTrackPartId + 1;
	}

	private String getCurrentPositionString() {
		StringBuffer sb = new StringBuffer();
		sb.append("-");
		for (int i = 1; i <= trackParts.size(); i++) {
			if (i == currentTrackPartId)
				sb.append((char) 27 + "[35m");
			sb.append(getDirectionShortCut(trackParts.get(i).getDirection()));
			sb.append("(" + i + ")");
			if (i == currentTrackPartId)
				sb.append((char) 27 + "[0m");
			sb.append("-");
		}
		return sb.toString();
	}

	private String getDirectionShortCut(Direction direction) {
		switch (direction) {
		case LEFT:
			return "L";
		case RIGHT:
			return "R";
		default:
			return "S";
		}
	}

	private void initializeTrackPartMap(List<DrivingTrackPart> trackParts) {
		for (DrivingTrackPart trackPart : trackParts) {
			this.trackParts.put(trackPart.getId(), trackPart);
		}
	}

	private void initializeBarriers() {
		barriers = new ArrayList<>();
		barrierIndexToTrackPartMap = new TreeMap<>();
		int index = 0;
		for (DrivingTrackPart trackPart : trackParts.values()) {
			for (DrivingVelocityBarrier barrier : trackPart.getVelocityBarriers()) {
				barriers.add(barrier);
				barrierIndexToTrackPartMap.put(index, trackPart);
				index++;
			}
		}
	}
}
