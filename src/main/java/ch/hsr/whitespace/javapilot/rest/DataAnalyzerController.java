package ch.hsr.whitespace.javapilot.rest;

import java.util.Collection;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.hsr.whitespace.javapilot.model.data_analysis.GyrZGraph;
import ch.hsr.whitespace.javapilot.model.data_analysis.GyrZGraphValue;

@RestController
@RequestMapping("/data/")
public class DataAnalyzerController {

	private GyrZGraph graph;

	public DataAnalyzerController() {
		graph = GyrZGraph.instance();
	}

	@RequestMapping(value = "/gyrz", method = RequestMethod.GET, produces = "application/json")
	public Collection<GyrZGraphValue> getG2ZValues() {
		return graph.getData();
	}

}
