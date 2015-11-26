package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.InitializePositionDetection;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;
import ch.hsr.whitespace.javapilot.model.track.VelocityBarrier;

public class PositionDetectorActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(PositionDetectorActor.class);

	private Map<Integer, TrackPart> trackParts;
	private TrackPart currentTrackPart;
	private int currentTrackPartId = 1;
	private List<VelocityBarrier> barriers;
	private Map<Integer, TrackPart> barrierIndexToTrackPartMap;
	private int lastBarrierIndex = 0;

	private long trackPartEntryTime = 0;

	public static Props props(ActorRef pilot) {
		return Props.create(PositionDetectorActor.class, () -> new PositionDetectorActor());
	}

	public PositionDetectorActor() {
		trackParts = new TreeMap<>();
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof InitializePositionDetection) {
			initializeTrackPartMap(((InitializePositionDetection) message).getTrackParts());
			initializeBarriers();
			currentTrackPart = trackParts.get(currentTrackPartId);
		} else if (message instanceof DirectionChangedMessage && trackParts != null) {
			handleDirectionChangedEvent((DirectionChangedMessage) message);
		} else if (message instanceof VelocityMessage) {
			handleVelocityMessage((VelocityMessage) message);
		}
	}

	private void handleVelocityMessage(VelocityMessage message) {
		currentTrackPart = barrierIndexToTrackPartMap.get(lastBarrierIndex);
		LOGGER.info("Passed barrier in trackpart '" + currentTrackPartId + "'");
		currentTrackPartId = currentTrackPart.getId();
		incrementBarrierIndex();
	}

	private void incrementBarrierIndex() {
		if (lastBarrierIndex == (barriers.size() - 1)) {
			lastBarrierIndex = 0;
		} else {
			lastBarrierIndex++;
		}
	}

	private void handleDirectionChangedEvent(DirectionChangedMessage message) {
		updateTrackPartTimestamps(message.getTimeStamp(), System.currentTimeMillis());
		incrementCurrentTrack(message.getNewDirection());
		handleDirectionChange();
	}

	private void updateTrackPartTimestamps(long currentTimeStamp, long currentTimeStampLocal) {
		if (trackPartEntryTime != 0) {
			currentTrackPart.setStartTime(trackPartEntryTime);
			currentTrackPart.setEndTime(currentTimeStamp);
		}
		trackPartEntryTime = currentTimeStamp;
	}

	private void handleDirectionChange() {
		LOGGER.info("Position: " + getCurrentPositionString());
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
			sb.append(trackParts.get(i).getDirection().toShortString());
			sb.append("(" + i + ")");
			if (i == currentTrackPartId)
				sb.append((char) 27 + "[0m");
			sb.append("-");
		}
		return sb.toString();
	}

	private void initializeTrackPartMap(List<TrackPart> trackParts) {
		int idCounter = 1;
		for (TrackPart trackPart : trackParts) {
			trackPart.setId(idCounter);
			this.trackParts.put(trackPart.getId(), trackPart);
			idCounter++;
		}
	}

	private void initializeBarriers() {
		barriers = new ArrayList<>();
		barrierIndexToTrackPartMap = new TreeMap<>();
		int index = 0;
		for (TrackPart trackPart : trackParts.values()) {
			for (VelocityBarrier barrier : trackPart.getVelocityBarriers()) {
				barriers.add(barrier);
				barrierIndexToTrackPartMap.put(index, trackPart);
				index++;
			}
		}
	}
}
