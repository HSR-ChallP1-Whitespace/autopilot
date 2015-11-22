package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.MatchingTrackPatternResponseMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackRecognitionFinished;
import ch.hsr.whitespace.javapilot.algorithms.pattern_matching.TrackPartPatternMatcher;
import ch.hsr.whitespace.javapilot.algorithms.pattern_matching.impl.TrackPartPatternMatcherImpl;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrack;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionVelocityBarrier;
import ch.hsr.whitespace.javapilot.model.track.recognition.matching.PossibleTrackMatch;

public class TrackRecognizerActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(TrackRecognizerActor.class);

	private boolean hasMatched = false;
	private long startTime;
	private RecognitionTrack recognizedTrack;
	private long lastDirectionChangeTimeStamp;
	private List<RecognitionVelocityBarrier> tempVelocityBarriers;
	private Direction lastDirection;
	private List<ActorRef> childActors;

	public TrackRecognizerActor() {
		recognizedTrack = new RecognitionTrack();
		tempVelocityBarriers = new ArrayList<>();
		childActors = new ArrayList<>();
		LOGGER.info("TrackRecognizer initialized");
	}

	public static Props props(ActorRef pilot) {
		return Props.create(TrackRecognizerActor.class, () -> new TrackRecognizerActor());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (!hasMatched) {
			forwardMessage(message);
			if (message instanceof SensorEvent) {
				if (startTime == 0) {
					setStartTime((SensorEvent) message);
				}
			} else if (message instanceof VelocityMessage) {
				handleVelocityMessage((VelocityMessage) message);
			} else if (message instanceof DirectionChangedMessage) {
				handleDirectionChanged((DirectionChangedMessage) message);
			} else if (message instanceof MatchingTrackPatternResponseMessage) {
				handleTrackPatternResponse((MatchingTrackPatternResponseMessage) message);
			}
		}
	}

	private void handleTrackPatternResponse(MatchingTrackPatternResponseMessage message) {
		if (message.getPatternConfirmed())
			confirmTrackMatch(message);
		else
			removeTrackPatternMatchingActor(getSender());
	}

	private void confirmTrackMatch(MatchingTrackPatternResponseMessage message) {
		hasMatched = true;
		tellTrackRecognitionFinished(message.getConfirmedMatch());
		LOGGER.info("Found track pattern!");
		printTrack(message.getConfirmedMatch());
	}

	private void handleVelocityMessage(VelocityMessage message) {
		RecognitionVelocityBarrier barrier = new RecognitionVelocityBarrier();
		barrier.setTimestamp(message.getTimeStamp() - startTime);
		barrier.setVelocity(message.getVelocity());
		tempVelocityBarriers.add(barrier);
	}

	private void setStartTime(SensorEvent message) {
		startTime = message.getTimeStamp();
		lastDirectionChangeTimeStamp = startTime;
	}

	private void handleDirectionChanged(DirectionChangedMessage message) {
		if (isFirstDirectionChange()) {
			lastDirection = message.getNewDirection();
		} else {
			saveTrackPart(message);
			search4PossibleTrackMatches();
			lastDirection = message.getNewDirection();
		}
	}

	private boolean isFirstDirectionChange() {
		return lastDirection == null;
	}

	private void tellTrackRecognitionFinished(PossibleTrackMatch match) {
		ActorRef whitespacePilot = getContext().parent();
		whitespacePilot.tell(new TrackRecognitionFinished(match.getTrackParts()), getSelf());
	}

	private void saveTrackPart(DirectionChangedMessage message) {
		long start = lastDirectionChangeTimeStamp - startTime;
		long end = message.getTimeStamp() - startTime;
		RecognitionTrackPart part = createTrackPart(lastDirection, start, end);
		LOGGER.info(part.toString());
		recognizedTrack.addPart(part);
		lastDirectionChangeTimeStamp = message.getTimeStamp();
	}

	private void search4PossibleTrackMatches() {
		// track contains at least 4 track parts (4 parts * 2 rounds ==> at
		// least 8 parts)
		if (recognizedTrack.getParts().size() < 8)
			return;

		TrackPartPatternMatcher matcher = new TrackPartPatternMatcherImpl(recognizedTrack.getParts());
		int failure = matcher.match();
		if (failure == 0) {
			testPossibleMatch(matcher.getPossibleMatches().get(1));
		} else if (failure >= 0 && failure <= 2) {
			for (PossibleTrackMatch possibleMatch : matcher.getPossibleMatches()) {
				testPossibleMatch(possibleMatch);
			}
		}
	}

	private void testPossibleMatch(PossibleTrackMatch possibleMatch) {
		LOGGER.info((char) 27 + "[33mCheck possible pattern: " + (char) 27 + "[0m");
		printTrack(possibleMatch);
		createActorToCheckPossibleMatch(possibleMatch);
	}

	private void createActorToCheckPossibleMatch(PossibleTrackMatch match) {
		childActors.add(getContext().actorOf(Props.create(MatchingTrackPatternActor.class, match)));
	}

	private void removeTrackPatternMatchingActor(ActorRef sender) {
		getContext().stop(sender);
		childActors.remove(sender);
	}

	private void printTrack(PossibleTrackMatch possibleMatch) {
		for (RecognitionTrackPart trackPart : possibleMatch.getTrackParts()) {
			LOGGER.info((char) 27 + "[33m" + trackPart + (char) 27 + "[0m");
		}
	}

	private RecognitionTrackPart createTrackPart(Direction direction, long startTime, long endTime) {
		RecognitionTrackPart trackPart = new RecognitionTrackPart(direction, startTime, endTime);
		addVelocityBarriersToTrackPart(trackPart);
		return trackPart;
	}

	private void addVelocityBarriersToTrackPart(RecognitionTrackPart trackPart) {
		for (RecognitionVelocityBarrier barrier : tempVelocityBarriers) {
			trackPart.addVelocityBarrier(barrier);
		}
		tempVelocityBarriers.clear();
	}

	private void forwardMessage(Object message) {
		for (ActorRef child : childActors) {
			child.tell(message, getSelf());
		}
	}

}