package ch.hsr.whitespace.javapilot.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.hsr.whitespace.javapilot.services.PilotService;

@RestController
@RequestMapping("/api/")
public class RestApiController {

	public PilotService service;

	@RequestMapping(value = "/up", method = RequestMethod.GET, produces = "application/json")
	public String getTrack() {
		return "ok";
	}

}
