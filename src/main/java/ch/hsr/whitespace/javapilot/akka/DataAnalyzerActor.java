package ch.hsr.whitespace.javapilot.akka;

import com.zuehlke.carrera.relayapi.messages.RaceStartMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.algorithms.MovingAverages;
import ch.hsr.whitespace.javapilot.model.data.analysis.GyrZGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.RoundTimeGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.VelocityGraph;

public class DataAnalyzerActor extends UntypedActor {

	private MovingAverages movingAverages;

	public DataAnalyzerActor() {
		movingAverages = new MovingAverages();
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
		} else if (message instanceof VelocityMessage) {
			handleVelocityMessage((VelocityMessage) message);
		}
	}

	private void handleVelocityMessage(VelocityMessage message) {
		VelocityGraph.liveInstance().storeVelocity(message.getTimeStamp(), message.getVelocity());
	}

	private void handleRoundTimeMessage(RoundTimeMessage message) {
		RoundTimeGraph.liveInstance().storeRoundTime(message.getRoundDuration());
	}

	private void handleRaceStart(RaceStartMessage message) {
		GyrZGraph.liveInstance().reset();
		RoundTimeGraph.liveInstance().reset();
	}

	private void handleSensorEvent(SensorEvent event) {
		double gyrZ = event.getG()[2];
		movingAverages.shift(gyrZ);
		GyrZGraph.liveInstance().storeValues(event.getTimeStamp(), gyrZ, movingAverages);
	}

}
