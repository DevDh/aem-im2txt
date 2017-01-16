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
		label = "The Im2txt Endpoint",
		value = {"http://0.0.0.0:5000"}),
		@Property(
		name = "im2txt.train.uri",
		label = "The Im2txt Training URI",
		value = {"/train"}
		),
		@Property(
		name = "im2txt.process.image.uri",
		label = "The Im2txt Process Image URI",
		value = "/im2txt")
	})
public class Im2txtConfigService {
	private static final Logger LOG = LoggerFactory.getLogger(Im2txtConfigService.class);

	private String im2txtURL;
	private String im2txtTrainUri;
	private String im2txtProcessImageUri;

	private String defaultIm2txtURL = "http://0.0.0.0:5000";
	private String defaultIm2txtTrainUri = "/train";
	private String defaultIm2txtProcessImageUri = "/im2txt";

	public static final String IM2TXT_ENDPOINT = "im2txt.service.endpoint";

	public static final String IM2TXT_TRAIN_URI = "im2txt.train.uri";

	public static final String IM2TXT_PROCESS_IMAGE_URI = "im2txt.process.image.uri";

	public String getIm2txtURL() {
		LOG.debug("Returning Im2txtURL : " + im2txtURL);
		return im2txtURL;
	}

	public String getIm2txtTrain() {
		LOG.debug("Returning im2txtTrainUri : " + im2txtTrainUri);
		return im2txtTrainUri;
	}

	public String getIm2txtProcessImage() {
		LOG.debug("Returning im2txtProcessImageUri : " + im2txtProcessImageUri);
		return im2txtProcessImageUri;
	}

	@Activate protected void activate(final Map<String, Object> config) throws Exception {
		resetService(config);
	}

	@Modified protected void modified(final Map<String, Object> config) {
		resetService(config);
	}

	private synchronized void resetService(final Map<String, Object> config) {
		im2txtURL = config.containsKey(IM2TXT_ENDPOINT) ?
				(String) config.get(IM2TXT_ENDPOINT) : defaultIm2txtURL;

		im2txtTrainUri = config.containsKey(IM2TXT_TRAIN_URI) ?
				(String) config.get(IM2TXT_TRAIN_URI) :
				defaultIm2txtTrainUri;

		im2txtProcessImageUri = config.containsKey(IM2TXT_PROCESS_IMAGE_URI) ?
				(String) config.get(IM2TXT_PROCESS_IMAGE_URI) :
				defaultIm2txtProcessImageUri;

		LOG.info("New location of IM2TXT_ENDPOINT: " + IM2TXT_ENDPOINT);
	}
}

