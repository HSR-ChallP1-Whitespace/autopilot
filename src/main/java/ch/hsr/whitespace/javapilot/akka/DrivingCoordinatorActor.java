package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ChainTrackPartActorsMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.InitializePositionDetection;
import ch.hsr.whitespace.javapilot.akka.messages.LostPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.PrintTrackPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.RestartWithTrackRecognitionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupFinishedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.SpeedupMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackPartEnteredMessage;
import ch.hsr.whitespace.javapilot.algorithms.speedup_strategy.SpeedupOrderStrategy;
import ch.hsr.whitespace.javapilot.algorithms.speedup_strategy.SpeedupOrderStrategyFactory;
import ch.hsr.whitespace.javapilot.algorithms.speedup_strategy.SpeedupOrderStrategyFactory.SpeedupOrderStrategyType;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;
import ch.hsr.whitespace.javapilot.model.track.VelocityBarrier;
import ch.hsr.whitespace.javapilot.util.MessageUtil;
import ch.hsr.whitespace.javapilot.util.TrackPartUtil;

public class DrivingCoordinatorActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(DrivingCoordinatorActor.class);

	private static final int MAX_LOSTS_WITHIN_10_SECS = 4;
	private static final SpeedupOrderStrategyType SPEEDUP_STRATEGY = SpeedupOrderStrategyType.ALL_TOGETHER;

	private ActorRef whitespacePilot;
	private Map<Integer, TrackPart> trackParts;
	private Map<Integer, ActorRef> trackPartActors;
	private List<VelocityBarrier> barriers;
	private Map<Integer, TrackPart> barrierIndexToTrackPartMap;
	private int lastBarrierIndex = 0;
	private boolean lostPosition = false;
	private int initialPower;
	private List<TrackPart> straights;
	private List<LostPositionMessage> lostMessages;
	private SpeedupOrderStrategy speedupStrategy;

	public static Props props(ActorRef pilot, int initialPower) {
		return Props.create(DrivingCoordinatorActor.class, () -> new DrivingCoordinatorActor(pilot, initialPower));
	}

	public DrivingCoordinatorActor(ActorRef whitespacePilot, int initialPower) {
		this.whitespacePilot = whitespacePilot;
		this.initialPower = initialPower;
		this.trackParts = new TreeMap<>();
		this.trackPartActors = new TreeMap<>();
		this.lostMessages = new ArrayList<>();
	}

	@Override
	public void onReceive(Object message) throws Exception {
		forwardMessagesToDriverActors(message);
		if (message instanceof InitializePositionDetection) {
			initializeTrackPartMap(((InitializePositionDetection) message).getTrackParts());
			initializeBarriers();
			trackPartActors.get(1).tell(new TrackPartEnteredMessage(0, trackParts.get(1).getDirection()), getSelf());
			startSpeedingUp();
		} else if (message instanceof VelocityMessage) {
			handleVelocityMessage((VelocityMessage) message);
		} else if (message instanceof PrintTrackPositionMessage) {
			printCurrentPosition(((PrintTrackPositionMessage) message).getCurrentTrackPartId());
		} else if (message instanceof LostPositionMessage) {
			handleLostPosition((LostPositionMessage) message);
		} else if (message instanceof SpeedupFinishedMessage) {
			speedupStrategy.speedupFinished(((SpeedupFinishedMessage) message).getTrackPart());
		}
	}

	public void speedupTrackPartById(int trackPartId) {
		trackPartActors.get(trackPartId).tell(new SpeedupMessage(true), getSelf());
	}

	private void handleLostPosition(LostPositionMessage message) {
		lostPosition = true;
		this.whitespacePilot.tell(message, getSender());
		if (!isDetectedDirectionPartOfTrackPart(message.getDetectedDirection())) {
			restart();
			LOGGER.warn("The direction '" + message.getDetectedDirection() + "' is not part of our pattern. :-/ Start over...");
		}
		if (doWeHaveTooManyLosts(message)) {
			restart();
			LOGGER.warn("We have to many losts :-/ Start over...");
		}
	}

	private boolean doWeHaveTooManyLosts(LostPositionMessage message) {
		lostMessages.add(message);
		long currentTimeStamp = message.getTimeStamp();
		long currentMinus10SecsTimeStamp = currentTimeStamp - 10000;
		List<LostPositionMessage> lostsInLast10Seconds = lostMessages.stream().filter(l -> l.getTimeStamp() > currentMinus10SecsTimeStamp && l.getTimeStamp() <= currentTimeStamp)
				.collect(Collectors.toList());
		return lostsInLast10Seconds.size() > MAX_LOSTS_WITHIN_10_SECS;
	}

	private void restart() {
		for (ActorRef actor : trackPartActors.values()) {
			getContext().stop(actor);
		}
		whitespacePilot.tell(new RestartWithTrackRecognitionMessage(), getSelf());
	}

	private boolean isDetectedDirectionPartOfTrackPart(Direction detectedDirection) {
		for (TrackPart trackPart : trackParts.values()) {
			if (trackPart.getDirection() == detectedDirection)
				return true;
		}
		return false;
	}

	private void startSpeedingUp() {
		this.straights = TrackPartUtil.getStraightPartsByDuration(trackParts.values());
		speedupStrategy = new SpeedupOrderStrategyFactory(straights, this).createStrategy(SPEEDUP_STRATEGY);
		speedupStrategy.startSpeedup();
	}

	private void forwardMessagesToDriverActors(Object message) {
		if (!MessageUtil.isMessageForwardNeeded(message, new Class[] { DirectionChangedMessage.class, PenaltyMessage.class }))
			return;
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
		trackPartActors.get(currentTrackPartId).tell(new TrackPartEnteredMessage(0, trackParts.get(currentTrackPartId).getDirection(), true), getSelf());
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
			int previousId = i - 1;
			int nextId = i + 1;
			if (i == 1) {
				previousId = trackPartActors.size();
			} else if (i == trackPartActors.size()) {
				nextId = 1;
			}
			LOGGER.info("Chain trackpart-actor with id '" + i + "': previous='" + previousId + "', next='" + nextId + "'");
			trackPartActors.get(i).tell(new ChainTrackPartActorsMessage(trackPartActors.get(previousId), trackPartActors.get(nextId)), getSelf());
		}
	}

	private void createTrackPartActor(int idCounter, TrackPart trackPart) {
		ActorRef actor = getContext().actorOf(Props.create(AbstractTrackPartDrivingActor.getDrivingActorClass(trackPart.getDirection()), whitespacePilot, trackPart, initialPower));
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
