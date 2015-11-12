package ch.hsr.whitespace.javapilot.akka;

import com.zuehlke.carrera.relayapi.messages.RaceStartMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.timeseries.FloatingHistory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.model.data.analysis.GyrZGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.RoundTimeGraph;

public class DataAnalyzerActor extends UntypedActor {

	private FloatingHistory smoothedValues;

	public DataAnalyzerActor() {
		smoothedValues = new FloatingHistory(3);
	}

	public static Props props(ActorRef pilot) {
		return Props.create(DataAnalyzerActor.class, () -> new DataAnalyzerActor());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof SensorEvent) {
			handleSensorEvent((SensorEvent) message);
		} else if (message instanceof RaceStartMessage) {
			handleRaceStart((RaceStartMessage) message);
		} else if (message instanceof RoundTimeMessage) {
			handleRoundTimeMessage((RoundTimeMessage) message);
		}
	}

	private void handleRoundTimeMessage(RoundTimeMessage message) {
		RoundTimeGraph.instance().storeRoundTime(message.getRoundDuration());
	}

	private void handleRaceStart(RaceStartMessage message) {
		GyrZGraph.instance().reset();
		RoundTimeGraph.instance().reset();
	}

	private void handleSensorEvent(SensorEvent event) {
		double gyrZ = event.getG()[2];
		smoothedValues.shift(gyrZ);

		GyrZGraph.instance().storeValue(event.getTimeStamp(), gyrZ);
		GyrZGraph.instance().storeValueSmoothed(event.getTimeStamp(), smoothedValues.currentMean());
		GyrZGraph.instance().storeValueStdDev(event.getTimeStamp(), smoothedValues.currentStDev());
	}

}
