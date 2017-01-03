package com.headwire.aem.samples.config;

import org.apache.felix.scr.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component(
		name = "Im2txt Configuration Service",
		description = "Im2txt Service for configuring settings",
		label = "Im2txt Configuration Service",
		immediate = true,
		metatype = true)
@Service(Im2txtConfigService.class)
@Properties({ @Property(
		name = "im2txt.service.endpoint",
		label = "The Im2txt Endpoint") })
public class Im2txtConfigService {
	private static final Logger LOG = LoggerFactory.getLogger(Im2txtConfigService.class);

	private String im2txtURL;
	public static final String IM2TXT_ENDPOINT = "im2txt.service.endpoint";

	public String getIm2txtURL() {
		LOG.info("Returning Im2txtURL : " + im2txtURL);
		return im2txtURL;
	}

	@Activate protected void activate(final Map<String, Object> config) throws Exception {
		resetService(config);
	}

	@Modified protected void modified(final Map<String, Object> config) {
		resetService(config);
	}

	private synchronized void resetService(final Map<String, Object> config) {
		im2txtURL = config.containsKey(IM2TXT_ENDPOINT) ?
				(String) config.get(IM2TXT_ENDPOINT) :
				"http://192.168.42.70:5000/im2txt";

		LOG.info("New location of IM2TXT_ENDPOINT: " + IM2TXT_ENDPOINT);
	}
}

