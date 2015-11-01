package ch.hsr.whitespace.javapilot.model.converter;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.driving.DrivingTrackPart;
import ch.hsr.whitespace.javapilot.model.track.driving.DrivingVelocityBarrier;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionVelocityBarrier;

public class TrackPartConverter {

	public static List<DrivingTrackPart> convertTrackParts(List<RecognitionTrackPart> recognizerParts, int initialPower) {
		List<DrivingTrackPart> parts4PositionDetector = new ArrayList<>();
		int idCounter = 1;
		for (RecognitionTrackPart part : recognizerParts) {
			DrivingTrackPart partCopy = new DrivingTrackPart(idCounter, part.getDirection());
			partCopy.setCurrentPower(new Power(initialPower));
			partCopy.setStartTime(part.getStartTime());
			partCopy.setEndTime(part.getEndTime());
			partCopy.setVelocityBarriers(convertVelocityBarriers(part.getVelocityBarriers()));
			parts4PositionDetector.add(partCopy);
			idCounter++;
		}
		return parts4PositionDetector;
	}

	private static List<DrivingVelocityBarrier> convertVelocityBarriers(List<RecognitionVelocityBarrier> barriers) {
		List<DrivingVelocityBarrier> drivingBarriers = new ArrayList<>();
		for (RecognitionVelocityBarrier barrier : barriers) {
			drivingBarriers.add(new DrivingVelocityBarrier(barrier));
		}
		return drivingBarriers;
	}

}
