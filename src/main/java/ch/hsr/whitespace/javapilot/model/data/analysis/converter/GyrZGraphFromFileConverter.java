package ch.hsr.whitespace.javapilot.model.data.analysis.converter;

import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

import ch.hsr.whitespace.javapilot.model.data.analysis.GyrZGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.RoundTimeGraph;
import ch.hsr.whitespace.javapilot.model.data.store.Race;

public class GyrZGraphFromFileConverter {

	private Race race;

	public GyrZGraphFromFileConverter(Race raceFromFile) {
		this.race = raceFromFile;
	}

	public GyrZGraph getGyrZGraph() {
		GyrZGraph graph = GyrZGraph.createInstance();
		for (SensorEvent sensorEvent : race.getSensorEvents()) {
			graph.storeValue(sensorEvent.getTimeStamp(), sensorEvent.getG()[2]);
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

}
