package ch.hsr.whitespace.javapilot.akka;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

public class DataSerializerActor extends UntypedActor {

	private static final String RACE_DATA_FOLDER_NAME = "race_data";

	private final Logger LOGGER = LoggerFactory.getLogger(DataSerializerActor.class);

	private Race race;

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
		File dataFile = new File(RACE_DATA_FOLDER_NAME, "race-data_" + timeFormat.format(race.getStartTime()) + ".json");
		LOGGER.info("Stop recording data. Write race data to JSON file... (" + dataFile.getAbsolutePath() + ")");
		race.setEndTime(message.getTimestamp());
		serializeData(dataFile);
	}

	private void createFolder() {
		File folder = new File(RACE_DATA_FOLDER_NAME);
		if (!folder.exists())
			folder.mkdirs();
	}

	private void serializeData(File file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		mapper.setDateFormat(dateFormat);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			mapper.writeValue(fileOutputStream, race);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}

}
