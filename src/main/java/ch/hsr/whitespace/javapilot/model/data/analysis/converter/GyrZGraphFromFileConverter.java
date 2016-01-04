package ch.hsr.whitespace.javapilot.model.data.analysis.converter;

import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import ch.hsr.whitespace.javapilot.algorithms.MovingAverages;
import ch.hsr.whitespace.javapilot.model.data.analysis.GyrZGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.RoundTimeGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.VelocityGraph;
import ch.hsr.whitespace.javapilot.model.data.store.Race;

public class GyrZGraphFromFileConverter {

	private Race race;

	public GyrZGraphFromFileConverter(Race raceFromFile) {
		this.race = raceFromFile;
	}

	public GyrZGraph getGyrZGraph() {
		GyrZGraph graph = GyrZGraph.createInstance();
		MovingAverages averages = new MovingAverages();
		for (SensorEvent sensorEvent : race.getSensorEvents()) {
			double value = sensorEvent.getG()[2];
			averages.shift(value);
			graph.storeValues(sensorEvent.getTimeStamp(), value, averages);
		}
		return graph;
	}

	public RoundTimeGraph getRoundTimeGraph() {
		RoundTimeGraph graph = RoundTimeGraph.createInstance();
		for (RoundTimeMessage roundTimeMessage : race.getRoundTimes()) {
			graph.storeRoundTime(roundTimeMessage.getRoundDuration());
		}
		return graph;
	}

	public VelocityGraph getVelocityGraph() {
		VelocityGraph graph = VelocityGraph.createInstance();
		for (VelocityMessage velocityMessage : race.getVelocities()) {
			graph.storeVelocity(velocityMessage.getTimeStamp(), velocityMessage.getVelocity());
		}
		return graph;
	}

}
