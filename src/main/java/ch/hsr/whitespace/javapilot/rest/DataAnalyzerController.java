package ch.hsr.whitespace.javapilot.rest;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.hsr.whitespace.javapilot.model.data.analysis.GyrZGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.GyrZGraphValue;
import ch.hsr.whitespace.javapilot.model.data.analysis.RoundTimeGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.RoundTimeValue;
import ch.hsr.whitespace.javapilot.model.data.analysis.VelocityGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.VelocityValue;
import ch.hsr.whitespace.javapilot.model.data.analysis.converter.GyrZGraphFromFileConverter;
import ch.hsr.whitespace.javapilot.model.data.store.Race;
import ch.hsr.whitespace.javapilot.persistance.JSONSerializer;

@RestController
@RequestMapping("/data/")
public class DataAnalyzerController {

	private GyrZGraph gyrzLiveGraph;
	private RoundTimeGraph roundTimeLiveGraph;
	private VelocityGraph velocityGraph;
	private JSONSerializer jsonSerializer;

	public DataAnalyzerController() {
		gyrzLiveGraph = GyrZGraph.liveInstance();
		roundTimeLiveGraph = RoundTimeGraph.liveInstance();
		velocityGraph = VelocityGraph.liveInstance();
		jsonSerializer = new JSONSerializer();
	}

	@RequestMapping(value = "/live/gyrz", method = RequestMethod.GET, produces = "application/json")
	public Collection<GyrZGraphValue> getG2ZValues() {
		return gyrzLiveGraph.getData();
	}

	@RequestMapping(value = "/live/roundtimes", method = RequestMethod.GET, produces = "application/json")
	public Collection<RoundTimeValue> getRoundTimeValues() {
		return roundTimeLiveGraph.getData();
	}

	@RequestMapping(value = "/live/velocity", method = RequestMethod.GET, produces = "application/json")
	public Collection<VelocityValue> getVelocityValues() {
		return velocityGraph.getData();
	}

	@RequestMapping(value = "/file/{fileName}/gyrz", method = RequestMethod.GET, produces = "application/json")
	public Collection<GyrZGraphValue> getG2ZValuesFromFile(@PathVariable String fileName) {
		Race race = jsonSerializer.deserializeRace(new File(JSONSerializer.RACE_DATA_FOLDER_NAME, fileName));
		GyrZGraphFromFileConverter converter = new GyrZGraphFromFileConverter(race);
		return converter.getGyrZGraph().getData();
	}

	@RequestMapping(value = "/file/{fileName}/roundtimes", method = RequestMethod.GET, produces = "application/json")
	public Collection<RoundTimeValue> getRoundTimeValuesFromFile(@PathVariable String fileName) {
		Race race = jsonSerializer.deserializeRace(new File(JSONSerializer.RACE_DATA_FOLDER_NAME, fileName));
		GyrZGraphFromFileConverter converter = new GyrZGraphFromFileConverter(race);
		return converter.getRoundTimeGraph().getData();
	}

	@RequestMapping(value = "/file/{fileName}/velocity", method = RequestMethod.GET, produces = "application/json")
	public Collection<VelocityValue> getVelocityValuesFromFile(@PathVariable String fileName) {
		Race race = jsonSerializer.deserializeRace(new File(JSONSerializer.RACE_DATA_FOLDER_NAME, fileName));
		GyrZGraphFromFileConverter converter = new GyrZGraphFromFileConverter(race);
		return converter.getVelocityGraph().getData();
	}

	@RequestMapping(value = "/source-urls", method = RequestMethod.GET, produces = "application/json")
	public Map<String, String> getDataSourceUrls() {
		Map<String, String> urlMap = new TreeMap<>();
		urlMap.put("Live", "/live");
		Iterator<File> fileIterator = FileUtils.iterateFiles(new File(JSONSerializer.RACE_DATA_FOLDER_NAME), new SuffixFileFilter(".json"), null);
		while (fileIterator.hasNext()) {
			File file = fileIterator.next();
			urlMap.put(file.getName(), "/file/" + file.getName());
		}
		return urlMap;
	}

}
