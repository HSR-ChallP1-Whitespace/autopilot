package ch.hsr.whitespace.javapilot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.hsr.whitespace.javapilot.model.data.store.Race;

public class JSONSerializer {

	private final Logger LOGGER = LoggerFactory.getLogger(JSONSerializer.class);
	public static final String RACE_DATA_FOLDER_NAME = "race_data";

	private ObjectMapper mapper;

	public JSONSerializer() {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		mapper.setDateFormat(new SimpleDateFormat("dd.MM.yyyy"));
		mapper.setSerializationInclusion(Include.NON_EMPTY);
	}

	public void serializeRace(Race race, File file) {
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

	public Race deserializeRace(File file) {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			return mapper.readValue(fileInputStream, Race.class);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}

}
