package com.headwire.aem.samples.workflow;

import com.day.cq.dam.api.Asset;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.headwire.aem.samples.config.Im2txtConfigService;
import com.headwire.aem.samples.util.RequestHelper;
import org.apache.felix.scr.annotations.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Workflow to train the inception model with the image captions
 */
@Component(label = "Im2txt Train Inception Model Step", metatype = false, immediate = true)
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "Im2txt Train Inception Model Workflow"),
		@Property(name = Constants.SERVICE_VENDOR, value = "Headwire Inc."),
		@Property(name = "process.label", value = "Im2txt Train the Inception Model Step") })
@Service(value = WorkflowProcess.class)
public class Im2txtTrainStep implements WorkflowProcess {

	private static final Logger LOG = LoggerFactory.getLogger(Im2txtTrainStep.class);
	private static final String IMG_FORMAT = "JPG";

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private Im2txtConfigService im2txtConfigService;

	public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap args) throws WorkflowException {

		try {

			final Map<String, Object> map = new HashMap<String, Object>();
			map.put("user.jcr.session", wfSession.getSession());

			final Asset asset = RequestHelper.getAssetFromPayload(resolverFactory, workItem, wfSession.getSession());

			String description = retrieveMetadataValue("dc:description", asset);

			if(description == null || description.length() == 0){
				return;
			}

			//Add Captions to JCR Node
			trainModel(asset, description);

		} catch (Exception e) {

			LOG.error("Something wrong happened with exception", e);

		}
	}

	private String retrieveMetadataValue(String str, Asset asset) {

		String descProp = null;

		try {

			// Target Node
			Node assetNode = asset.adaptTo(Node.class);
			Node tNode = assetNode.getNode("jcr:content/metadata");

			if (tNode.hasProperty("dc:description")) {
				descProp = tNode.getProperty("dc:description").getString();
			}

		} catch (RepositoryException e) {

			LOG.error("Something wrong happened with RepositoryException", e);
		}

		return descProp;

	}

	private void trainModel(Asset asset, String description) {

			String trainingEndpoint = im2txtConfigService.getIm2txtURL() + im2txtConfigService.getIm2txtTrain();
			HttpPost post = new HttpPost(trainingEndpoint);
			post.addHeader("enctype", "multipart/form-data");

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

			builder.addTextBody("captions", description, ContentType.TEXT_PLAIN);

			InputStream is = asset.getOriginal().getStream();
			File f = RequestHelper.createTempFileFromStream(is, asset.getName(), IMG_FORMAT);

			builder.addBinaryBody("file", f, ContentType.DEFAULT_BINARY, asset.getName());
			HttpEntity entity = builder.build();
			post.setEntity(entity);

			String responseString = "";
			String[] arr = null;

			RequestHelper.postHttpClient(post);

			LOG.info("Im2Txt Training completed. : [{}]", responseString);

	}


}
