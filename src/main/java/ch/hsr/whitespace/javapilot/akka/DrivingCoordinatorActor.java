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
import ch.hsr.whitespace.javapilot.akka.messages.InitializePositionDetection;
import ch.hsr.whitespace.javapilot.akka.messages.LostPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.PrintTrackPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SetNextTrackPartActorMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackPartEnteredMessage;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;
import ch.hsr.whitespace.javapilot.model.track.VelocityBarrier;

public class DrivingCoordinatorActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(DrivingCoordinatorActor.class);

	private Map<Integer, TrackPart> trackParts;
	private Map<Integer, ActorRef> trackPartActors;
	private List<VelocityBarrier> barriers;
	private Map<Integer, TrackPart> barrierIndexToTrackPartMap;
	private int lastBarrierIndex = 0;
	private boolean lostPosition = false;

	public static Props props(ActorRef pilot) {
		return Props.create(DrivingCoordinatorActor.class, () -> new DrivingCoordinatorActor());
	}

	public DrivingCoordinatorActor() {
		trackParts = new TreeMap<>();
		trackPartActors = new TreeMap<>();
	}

	@Override
	public void onReceive(Object message) throws Exception {
		forwardMessagesToDriverActors(message);
		if (message instanceof InitializePositionDetection) {
			initializeTrackPartMap(((InitializePositionDetection) message).getTrackParts());
			initializeBarriers();
			trackPartActors.get(1).tell(new TrackPartEnteredMessage(0, trackParts.get(1).getDirection()), getSelf());
		} else if (message instanceof VelocityMessage) {
			handleVelocityMessage((VelocityMessage) message);
		} else if (message instanceof PrintTrackPositionMessage) {
			printCurrentPosition(((PrintTrackPositionMessage) message).getCurrentTrackPartId());
		} else if (message instanceof LostPositionMessage) {
			lostPosition = true;
		}
	}

	private void forwardMessagesToDriverActors(Object message) {
		for (ActorRef actor : trackPartActors.values()) {
			actor.tell(message, getSender());
		}
	}

	private void handleVelocityMessage(VelocityMessage message) {
		int trackPartId = barrierIndexToTrackPartMap.get(lastBarrierIndex).getId();
		LOGGER.info("Passed barrier in trackpart '" + trackPartId + "'");
		if (lostPosition)
			correctPositionWithLightBarrier(trackPartId);
		incrementBarrierIndex();
	}

	private void correctPositionWithLightBarrier(int currentTrackPartId) {
		trackPartActors.get(currentTrackPartId).tell(new TrackPartEnteredMessage(0, trackParts.get(currentTrackPartId).getDirection()), getSelf());
		lostPosition = false;
	}

	private void incrementBarrierIndex() {
		if (lastBarrierIndex == (barriers.size() - 1)) {
			lastBarrierIndex = 0;
		} else {
			lastBarrierIndex++;
		}
	}

	private void printCurrentPosition(int currentTrackPartId) {
		LOGGER.info("Position: " + getCurrentPositionString(currentTrackPartId));
	}

	private String getCurrentPositionString(int currentTrackPartId) {
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
			createTrackPartActor(idCounter, trackPart);
			idCounter++;
		}
		initializeTrackPartActorList();
	}

	private void initializeTrackPartActorList() {
		for (int i = 1; i <= trackPartActors.size(); i++) {
			if (i == (trackPartActors.size()))
				trackPartActors.get(i).tell(new SetNextTrackPartActorMessage(trackPartActors.get(1)), getSelf());
			else
				trackPartActors.get(i).tell(new SetNextTrackPartActorMessage(trackPartActors.get(i + 1)), getSelf());
		}
	}

	private void createTrackPartActor(int idCounter, TrackPart trackPart) {
		ActorRef actor = getContext().actorOf(Props.create(TrackPartDrivingActor.class, trackPart));
		trackPartActors.put(idCounter, actor);
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
