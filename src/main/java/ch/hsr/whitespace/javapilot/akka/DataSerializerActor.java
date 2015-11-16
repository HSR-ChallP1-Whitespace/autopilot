package ch.hsr.whitespace.javapilot.akka;

import java.io.File;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.RaceStartMessage;
import com.zuehlke.carrera.relayapi.messages.RaceStopMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.PowerChangeMessage;
import ch.hsr.whitespace.javapilot.model.data.store.Race;
import ch.hsr.whitespace.javapilot.persistance.JSONSerializer;

public class DataSerializerActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(DataSerializerActor.class);

	private Race race;
	private JSONSerializer serializer;

	public DataSerializerActor() {
		race = new Race();
		serializer = new JSONSerializer();
	}

	public static Props props(ActorRef pilot) {
		return Props.create(DataSerializerActor.class, () -> new DataSerializerActor());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof RaceStartMessage) {
			initializeRace((RaceStartMessage) message);
		} else if (message instanceof RaceStopMessage) {
			stopAndSaveRace((RaceStopMessage) message);
		} else if (message instanceof SensorEvent) {
			if (race.getStartTime() == 0)
				race.setStartTime(((SensorEvent) message).getTimeStamp());
			race.getSensorEvents().add((SensorEvent) message);
		} else if (message instanceof PowerChangeMessage) {
			race.getPowerChanges().add((PowerChangeMessage) message);
		} else if (message instanceof PenaltyMessage) {
			race.getPenalties().add((PenaltyMessage) message);
		} else if (message instanceof VelocityMessage) {
			race.getVelocities().add((VelocityMessage) message);
		} else if (message instanceof RoundTimeMessage) {
			race.getRoundTimes().add((RoundTimeMessage) message);
		}
	}

	private void initializeRace(RaceStartMessage message) {
		LOGGER.info("Start recording data ...");
		this.race = new Race();
	}

	private void stopAndSaveRace(RaceStopMessage message) {
		SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
		createFolder();
		File dataFile = new File(JSONSerializer.RACE_DATA_FOLDER_NAME, "race-data_" + timeFormat.format(race.getStartTime()) + ".json");
		LOGGER.info("Stop recording data. Write race data to JSON file... (" + dataFile.getAbsolutePath() + ")");
		race.setEndTime(message.getTimestamp());
		serializer.serializeRace(race, dataFile);
	}

	private void createFolder() {
		File folder = new File(JSONSerializer.RACE_DATA_FOLDER_NAME);
		if (!folder.exists())
			folder.mkdirs();
	}

}
