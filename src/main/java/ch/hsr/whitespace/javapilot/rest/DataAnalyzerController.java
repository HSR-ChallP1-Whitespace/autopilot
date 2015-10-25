package ch.hsr.whitespace.javapilot.rest;

import java.util.Collection;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.hsr.whitespace.javapilot.model.data.analysis.GyrZGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.GyrZGraphValue;
import ch.hsr.whitespace.javapilot.model.data.analysis.RoundTimeGraph;
import ch.hsr.whitespace.javapilot.model.data.analysis.RoundTimeValue;

@RestController
@RequestMapping("/data/")
public class DataAnalyzerController {

	private GyrZGraph gyrzGraph;
	private RoundTimeGraph roundTimeGraph;

	public DataAnalyzerController() {
		gyrzGraph = GyrZGraph.instance();
		roundTimeGraph = RoundTimeGraph.instance();
	}

	@RequestMapping(value = "/gyrz", method = RequestMethod.GET, produces = "application/json")
	public Collection<GyrZGraphValue> getG2ZValues() {
		return gyrzGraph.getData();
	}

	@RequestMapping(value = "/roundtimes", method = RequestMethod.GET, produces = "application/json")
	public Collection<RoundTimeValue> getRoundTimeValues() {
		return roundTimeGraph.getData();
	}

}
